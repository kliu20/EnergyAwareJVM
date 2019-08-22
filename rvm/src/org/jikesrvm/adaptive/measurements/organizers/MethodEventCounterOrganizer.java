package org.jikesrvm.adaptive.measurements.organizers;

import org.jikesrvm.VM;
import org.jikesrvm.adaptive.controller.Controller;
import org.jikesrvm.adaptive.controller.HotMethodRecompilationEvent;
import org.jikesrvm.adaptive.measurements.RuntimeMeasurements;
import org.jikesrvm.adaptive.measurements.listeners.MethodEventCounterListener;
import org.jikesrvm.adaptive.measurements.listeners.MethodListener;
import org.jikesrvm.adaptive.util.AOSLogging;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.common.CompiledMethods;
import org.jikesrvm.compilers.opt.runtimesupport.OptCompiledMethod;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.NonMoving;

@NonMoving
public final class MethodEventCounterOrganizer extends Organizer {
	  @Override
	  public void initialize() {
	    int numSamples = Controller.options.METHOD_SAMPLE_SIZE * RVMThread.availableProcessors;
	    if (Controller.options.mlCBS()) {
	      numSamples *= VM.CBSMethodSamplesPerTick;
	    }
	    MethodEventCounterListener methodEventCounterListener = new MethodEventCounterListener(numSamples);
	    listener = methodEventCounterListener;
	    listener.setOrganizer(this);

	    //install energy method listener set.
	    if (Controller.options.mlTimer() || Controller.options.mlCBS()) {
	    	//Cannot initialize energyMethodListeners in RuntimeMeasurements class file by unknown reasons.
	      RuntimeMeasurements.initEventCounterMethodListener();
	      RuntimeMeasurements.installEventCounterMethodListener(methodEventCounterListener);
	    } else {
	      if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED, "Unexpected value of method_listener_trigger");
	    }
	  }

	  @Override
	  void thresholdReached() {
	    AOSLogging.logger.organizerThresholdReached();

	    int numSamples = ((MethodListener) listener).getNumSamples();
	    int[] samples = ((MethodListener) listener).getSamples();

	    // (1) Update the global (cumulative) sample data
//	    Controller.methodSamples.update(samples, numSamples);

	    // (2) Remove duplicates from samples buffer.
	    //     NOTE: This is a dirty trick and may be ill-advised.
	    //     Rather than copying the unique samples into a different buffer
	    //     we treat samples as if it was a scratch buffer.
	    //     NOTE: This is worse case O(numSamples^2) but we expect a
	    //     significant number of duplicates, so it's probably better than
	    //     the other obvious alternative (sorting samples).
	    
//	    int uniqueIdx = 1;
//	    outer:
//	    for (int i = 1; i < numSamples; i++) {
//	      int cur = samples[i];
//	      for (int j = 0; j < uniqueIdx; j++) {
//	        if (cur == samples[j]) continue outer;
//	      }
//	      samples[uniqueIdx++] = cur;
//	    }

	    // (3) For all samples in 0...uniqueIdx, if the method represented by
	    //     the sample is compiled at an opt level below filterOptLevel
	    //     then report it to the controller.
	    
//	    for (int i = 0; i < uniqueIdx; i++) {
//	      int cmid = samples[i];
//	      double ns = Controller.methodSamples.getData(cmid);
//	      CompiledMethod cm = CompiledMethods.getCompiledMethod(cmid);
//	      if (cm != null) {         // not already obsoleted
//	        int compilerType = cm.getCompilerType();
//	        // Enqueue it unless it's either a trap method or already opt
//	        // compiled at filterOptLevel or higher.
//	        if (!(compilerType == CompiledMethod.TRAP ||
//	              (compilerType == CompiledMethod.OPT &&
//	               (((OptCompiledMethod) cm).getOptLevel() >= filterOptLevel)))) {
//	          HotMethodRecompilationEvent event = new HotMethodRecompilationEvent(cm, ns);
//	          
//	          Controller.controllerInputQueue.insert(ns, event);
//	          AOSLogging.logger.controllerNotifiedForHotness(cm, ns);
//	        }
//	      }
//	    }
	  }
}
