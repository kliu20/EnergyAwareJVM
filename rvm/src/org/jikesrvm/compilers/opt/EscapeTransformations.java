/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.compilers.opt;

import static org.jikesrvm.compilers.opt.ir.Operators.MONITORENTER;
import static org.jikesrvm.compilers.opt.ir.Operators.MONITOREXIT;
import static org.jikesrvm.compilers.opt.ir.Operators.NEWARRAY_opcode;
import static org.jikesrvm.compilers.opt.ir.Operators.NEW_opcode;

import org.jikesrvm.classloader.VM_Type;
import org.jikesrvm.compilers.opt.driver.CompilerPhase;
import org.jikesrvm.compilers.opt.ir.Call;
import org.jikesrvm.compilers.opt.ir.IR;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.compilers.opt.ir.New;
import org.jikesrvm.compilers.opt.ir.NewArray;
import org.jikesrvm.compilers.opt.ir.Register;
import org.jikesrvm.compilers.opt.ir.operand.MethodOperand;
import org.jikesrvm.compilers.opt.ir.operand.Operand;
import org.jikesrvm.compilers.opt.ir.operand.RegisterOperand;

/**
 * Transformations that use escape analysis.
 * <ul>
 *  <li> 1. synchronization removal
 *  <li> 2. scalar replacement of aggregates and short arrays
 * </ul>
 */
public class EscapeTransformations extends CompilerPhase {

  /**
   * Return this instance of this phase. This phase contains no
   * per-compilation instance fields.
   * @param ir not used
   * @return this
   */
  public CompilerPhase newExecution(IR ir) {
    return this;
  }

  public final boolean shouldPerform(OptOptions options) {
    return options.MONITOR_REMOVAL || options.SCALAR_REPLACE_AGGREGATES;
  }

  public final String getName() {
    return "Escape Transformations";
  }

  public final boolean printingEnabled(OptOptions options, boolean before) {
    return false;
  }

  /**
   * Perform the transformations
   *
   * @param ir IR for the target method
   */
  public void perform(IR ir) {
    // perform simple optimizations to increase efficacy
    DefUse.computeDU(ir);
    DefUse.recomputeSSA(ir);
    SimpleEscape analyzer = new SimpleEscape();
    FI_EscapeSummary summary = analyzer.simpleEscapeAnalysis(ir);
    // pass through registers. look for registers that point
    // to objects that do not escape. When found,
    // perform the transformations
    for (Register reg = ir.regpool.getFirstSymbolicRegister(); reg != null; reg = reg.getNext()) {
      // check if register is SSA
      if (!reg.isSSA()) {
        continue;
      }
      // The following can occur for guards. Why?
      if (reg.defList == null) {
        continue;
      }
      // *********************************************************
      // check if def is allocation. If so, try scalar replacement
      // of aggregates
      // *********************************************************
      Instruction def = reg.defList.instruction;
      if (ir.options.SCALAR_REPLACE_AGGREGATES && summary.isMethodLocal(reg)) {
        AggregateReplacer s = null;
        if ((def.getOpcode() == NEW_opcode) || (def.getOpcode() == NEWARRAY_opcode)) {
          s = getAggregateReplacer(def, ir);
        }
        if (s != null) {
          // VM.sysWrite("Scalar replacing "+def+" in "+ir.method+"\n");
          s.transform();
        }
      }
      // *********************************************************
      // Now remove synchronizations
      // *********************************************************
      if (ir.options.MONITOR_REMOVAL && summary.isThreadLocal(reg)) {
        UnsyncReplacer unsync = null;
        if ((def.getOpcode() == NEW_opcode) || (def.getOpcode() == NEWARRAY_opcode)) {
          unsync = getUnsyncReplacer(reg, def, ir);
        }
        if (unsync != null) {
          // VM.sysWrite("Removing synchronization on "+def+" in "+ir.method+"\n");
          unsync.transform();
        }
      }
    }
  }

  /**
   * Generate an object which transforms defs & uses of "synchronized"
   * objects to defs & uses of "unsynchronized" objects
   *
   * <p> PRECONDITION: objects pointed to by reg do NOT escape
   *
   * @param reg the pointer whose defs and uses should be transformed
   * @param inst the allocation site
   * @param ir controlling ir
   * @return an UnsyncReplacer specialized to the allocation site,
   *            null if no legal transformation found
   */
  private UnsyncReplacer getUnsyncReplacer(Register reg, Instruction inst, IR ir) {
    if (!synchronizesOn(ir, reg)) {
      return null;
    }
    return UnsyncReplacer.getReplacer(inst, ir);
  }

  /**
   * Is there an instruction in this IR which causes synchronization
   * on an object pointed to by a particular register?
   * PRECONDITION: register lists computed and valid
   */
  private static boolean synchronizesOn(IR ir, Register r) {
    // walk through uses of r
    for (RegisterOperand use = r.useList; use != null; use = use.getNext()) {
      Instruction s = use.instruction;
      if (s.operator == MONITORENTER) {
        return true;
      }
      if (s.operator == MONITOREXIT) {
        return true;
      }
      // check if this instruction invokes a synchronized method on the
      // object
      // we must pass the following conditions:
      //        1. the method is not static
      //        2. it is actually invoked on the register operand in question
      //        3. the method is synchronized
      if (Call.conforms(s)) {
        MethodOperand mo = Call.getMethod(s);
        if (!mo.isStatic()) {
          Operand invokee = Call.getParam(s, 0);
          if (invokee == use) {
            if (!mo.hasPreciseTarget()) return true; // if I don't know exactly what is called, assume the worse
            if (mo.getTarget().isSynchronized()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * Generate an object which will perform scalar replacement of
   * an aggregate allocated by a given instruction
   *
   * <p> PRECONDITION: objects returned by this allocation site do NOT escape
   *                 the current method
   *
   * @param inst the allocation site
   * @param ir controlling ir
   * @return an AggregateReplacer specialized to the allocation site,
   *            null if no legal transformation found
   */
  private AggregateReplacer getAggregateReplacer(Instruction inst, IR ir) {
    OptOptions options = ir.options;
    VM_Type t = null;
    if (inst.getOpcode() == NEW_opcode) {
      t = New.getType(inst).getVMType();
    } else if (inst.getOpcode() == NEWARRAY_opcode) {
      t = NewArray.getType(inst).getVMType();
    } else {
      throw new OptimizingCompilerException("Logic Error in EscapeTransformations");
    }

    // first attempt to perform scalar replacement for an object
    if (t.isClassType() && options.SCALAR_REPLACE_AGGREGATES) {
      return ObjectReplacer.getReplacer(inst, ir);
    }
    // attempt to perform scalar replacement on a short array
    if (t.isArrayType() && options.SCALAR_REPLACE_AGGREGATES) {
      return ShortArrayReplacer.getReplacer(inst, ir);
    }
    return null;
  }
}


