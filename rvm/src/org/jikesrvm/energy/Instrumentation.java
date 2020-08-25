package org.jikesrvm.energy;

import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.jikesrvm.VM;

import static org.jikesrvm.compilers.opt.ir.Operators.RETURN_opcode;

import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.common.CompiledMethods;
import org.jikesrvm.compilers.opt.ir.BasicBlock;
import org.jikesrvm.compilers.opt.ir.Call;
import org.jikesrvm.compilers.opt.ir.IR;
import org.jikesrvm.compilers.opt.ir.IRTools;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.compilers.opt.ir.operand.IntConstantOperand;
import org.jikesrvm.compilers.opt.ir.operand.MethodOperand;
import org.jikesrvm.compilers.opt.ir.operand.Operand;
import org.jikesrvm.compilers.opt.ir.operand.RegisterOperand;
import org.jikesrvm.compilers.opt.ir.operand.StringConstantOperand;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.adaptive.controller.Controller;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

import static org.jikesrvm.compilers.opt.ir.Operators.CALL;
import static org.jikesrvm.compilers.opt.driver.OptConstants.RUNTIME_SERVICES_BCI;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_LONG_ALOAD_BARRIER;

public class Instrumentation {
	IR ir;
	NormalMethod method;
	RVMClass cls;

	public Instrumentation(IR _ir) {
		ir = _ir;
		method = ir.getMethod();
		cls = method.getDeclaringClass();
	}


//	public void instrumentIO(){
//		if (!Util.isJavaIO(cls))
//			return;
//		NormalMethod ioArgSampling = Entrypoints.ioArgSampling;
//		Instruction start;
//		Enumeration<Operand> operands = ir.getParameters();
//		while (operands.hasMoreElements()){
//			Operand opr = operands.nextElement();
//			if (opr.getType().isArrayType()){
//				start = Call.create1(CALL, null, IRTools.AC(ioArgSampling.getOffset()),
//						MethodOperand.STATIC(ioArgSampling), opr);
//				start.position = ir.firstInstructionInCodeOrder().position;
//				start.bcIndex = RUNTIME_SERVICES_BCI;
//				ir.firstBasicBlockInCodeOrder()
//						.prependInstructionRespectingPrologue(start);
//			}
//		}
//
//	}

	public void perform() {
		try {
			if (Util.irrelevantType(cls) || Util.isJavaClass(cls)) {
				return;
			}

//			VM.sysWriteln("class name:" + cls.toString() + " method name: " + method.getName().toString() 
//					+ " method Id:" + method.methodID);
//			Service.methodCount[0] = 100;
//			System.out.println(Service.methodCount[0]);
//			VM.sysWriteln("methodCount.length");
//			VM.sysWriteln(Service.methodCount.length);
			
			if (method.methodID == -1){
				
				method.methodID = Service.addMethodEntry(cls.toString(), method.getName().toString());
			} 

			Instruction changeUserSpaceFreqInst = null;
			Instruction changeOnDemandFreqInst = null;
			Instruction startProfInst = null;
			Instruction endProfInst = null;
			NormalMethod changeUserSpaceFreqMtd = Entrypoints.changeUserSpaceFreq;
			NormalMethod changeOnDemandFreqMtd = Entrypoints.changeOnDemandFreq;
			NormalMethod startProfileMtd = Entrypoints.startProfile;
			NormalMethod endProfileMtd = Entrypoints.endProfile;

			StringConstantOperand clsName = new StringConstantOperand(
				cls.toString(), Offset.fromIntSignExtend(cls
				.getDescriptor().getStringLiteralOffset()));

			startProfInst = Call
					.create1(CALL, null,
							IRTools.AC(startProfileMtd.getOffset()),
							MethodOperand.STATIC(startProfileMtd), clsName,
							new IntConstantOperand(method.methodID));

			// Set userspace frequency for the specific method.
			if (cls.toString().equals(Controller.options.DVFS_CLS) && method.getName().toString().equals(Controller.options.DVFS_MTH)) {
				VM.sysWriteln("DVFS class name is invoked " + cls.toString() + " method is: " + method.getName());
				
				changeUserSpaceFreqInst = Call
						.create1(CALL, null,
								IRTools.AC(changeUserSpaceFreqMtd.getOffset()),
								MethodOperand.STATIC(changeUserSpaceFreqMtd), clsName,
								new IntConstantOperand((int)Controller.options.FREQUENCY_TO_BE_PRINTED));

				changeUserSpaceFreqInst.position = ir.firstInstructionInCodeOrder().position;
				changeUserSpaceFreqInst.bcIndex = RUNTIME_SERVICES_BCI;
				// Insert the DVFS instrument to the beginning of candidate method 
				ir.firstBasicBlockInCodeOrder()
						.prependInstructionRespectingPrologue(changeUserSpaceFreqInst);
			
				//startProfInst.position = changeUserSpaceFreqInst.nextInstructionInCodeOrder().position;
				//startProfInst.bcIndex = RUNTIME_SERVICES_BCI;

				// Insert start profile after change user space frequency instruction
				changeUserSpaceFreqInst.insertAfter(startProfInst);

			//	ir.firstBasicBlockInCodeOrder()
			//			.prependInstructionRespectingPrologue(startProfInst);
				
				// traverse the instruction starting from start profile inst (after change user space frequency inst).
				for (Instruction inst = startProfInst.nextInstructionInCodeOrder(); inst != null; inst = inst
						.nextInstructionInCodeOrder()) {
					
	//				VM.sysWriteln("Ir method: " + ir.getMethod().getName().toString() + " operator: "+ inst.operator.toString() + " opcode: " + (int)inst.getOpcode() + " RETURN_opcode: " + (int)RETURN_opcode);
	//				if (inst.operator.toString().equalsIgnoreCase("return")) {
					if (inst.getOpcode() == RETURN_opcode) {
						endProfInst = Call.create1(CALL, null,
								IRTools.AC(endProfileMtd.getOffset()),
								MethodOperand.STATIC(endProfileMtd),
								new IntConstantOperand(method.methodID));
						changeOnDemandFreqInst = Call.create1(CALL, null,
								IRTools.AC(changeOnDemandFreqMtd.getOffset()),
								MethodOperand.STATIC(changeOnDemandFreqMtd),
								new IntConstantOperand((int)Controller.options.FREQUENCY_TO_BE_PRINTED));
						// Insert change ondemand governor before return.
						//changeOnDemandFreqInst.position = inst.position;
						//changeOnDemandFreqInst.bcIndex = RUNTIME_SERVICES_BCI;
						inst.insertBefore(changeOnDemandFreqInst);

						// Insert end profile before change ondemand governor
						endProfInst.position = changeOnDemandFreqInst.position;
						endProfInst.bcIndex = RUNTIME_SERVICES_BCI;
						changeOnDemandFreqInst.insertBefore(endProfInst);
					}
				}

				return;
			}

			// If no method can be matched, just insert start profile and end profile method then.
			startProfInst.position = ir.firstInstructionInCodeOrder().position;
			startProfInst.bcIndex = RUNTIME_SERVICES_BCI;
			ir.firstBasicBlockInCodeOrder()
					.prependInstructionRespectingPrologue(startProfInst);
			for (Instruction inst = startProfInst.nextInstructionInCodeOrder(); inst != null; inst = inst
					.nextInstructionInCodeOrder()) {
				
//				VM.sysWriteln("Ir method: " + ir.getMethod().getName().toString() + " operator: "+ inst.operator.toString() + " opcode: " + (int)inst.getOpcode() + " RETURN_opcode: " + (int)RETURN_opcode);
//				if (inst.operator.toString().equalsIgnoreCase("return")) {
				if (inst.getOpcode() == RETURN_opcode) {
					endProfInst = Call.create1(CALL, null,
							IRTools.AC(endProfileMtd.getOffset()),
							MethodOperand.STATIC(endProfileMtd),
							new IntConstantOperand(method.methodID));
					//endProfInst.position = inst.position;
					//endProfInst.bcIndex = RUNTIME_SERVICES_BCI;
					inst.insertBefore(endProfInst);
				}
			}

		} catch (UTFDataFormatException e) {
			e.printStackTrace();
		}
	}
}


/* =====================================================
 * method/class name example:
 * =====================================================
 *
 * VM.write(method.getName().toString()); //main VM.writeln();
 * VM.write(method.getDescriptor().toString()); //L()V
 * VM.writeln();
 * VM.write(method.toString()); //<SystemAppCL,Lenergy/test/LoopTest; >.main ([Ljava/lang/String;)V
 * VM.writeln();
 *
 * VM.write(cls.toString());//energy.test.LoopTest VM.writeln();
 */
