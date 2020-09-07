package org.jikesrvm.adaptive.measurements.listeners;

//import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.jikesrvm.VM;
import org.jikesrvm.adaptive.AosEntrypoints;
import org.jikesrvm.adaptive.controller.Controller;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.common.CompiledMethods;
import org.jikesrvm.energy.DataPrinter;
import org.jikesrvm.energy.EnergyCheckUtils;
import org.jikesrvm.energy.ProfileQueue;
import org.jikesrvm.energy.ProfilingTypes;
import org.jikesrvm.energy.AccmProfile;
import org.jikesrvm.energy.ProfileStack;
import org.jikesrvm.energy.Scaler;
import org.jikesrvm.energy.ScalerOptions;
import org.jikesrvm.energy.TimeCheckUtils;
import org.jikesrvm.runtime.SysCall;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.Synchronization;
import org.vmmagic.pragma.Uninterruptible;

@Uninterruptible
public final class MethodEventCounterListener extends Listener implements ProfilingTypes, ScalerOptions {
//public final class MethodEventCounterListener extends Listener {
	
	public static long[][] prologueSamples;
	public static long[][] epilogueSamples;
	
	public static int sampleSize;
	public static boolean isOpenScaleFile = false;
	public static boolean isCloseScaleFile = false;
	public static boolean titleIsPrinted = false;
	 /**
	   * @param sampleSize the initial sampleSize for the listener
	   */
	  public MethodEventCounterListener(int sampleSize) {
	    this.sampleSize = sampleSize;
	    prologueSamples = new long[sampleSize][3];
	    epilogueSamples = new long[sampleSize][3];
	  }

	  /**
	   *
	   * @param cmid the compiled method ID to update
	   * @param whereFrom Was this a yieldpoint in a PROLOGUE, BACKEDGE, or
	   *         EPILOGUE?
	   */
	  public void update(int cmid, int whereFrom, CompiledMethod ypTakenInCM) {
//		  VM.sysWriteln("current method updating");
	    if (VM.UseEpilogueYieldPoints) {
	    	
	      if (whereFrom == RVMThread.PROLOGUE) {
	          recordPrologueCounter(cmid, ypTakenInCM);
	      } else if (whereFrom == RVMThread.EPILOGUE) {
	    	  recordEpilogueCounter(cmid, ypTakenInCM);
	      } else {
	    	  //Loop backedge, drop it.
	      }
	    } else {
	    	VM.sysWriteln("current method updates failed");
	    	//if it is not BuildForAdaptiveSystem, just return.
	    	return;
	    }
	  }

	  /**
	   * This method records a sample containing the {@code CMID} (compiled method ID)
	   * passed.  Since multiple threads may be taking samples concurrently,
	   * we use fetchAndAdd to distribute indices into the buffer AND to record
	   * when a sample is taken.  (Thread 1 may get an earlier index, but complete
	   * the insertion after Thread 2.)
	   *
	   * @param CMID compiled method ID to record
	   */
	  private void recordPrologueCounter(int cmid, CompiledMethod ypTakenInCM) {
//		  long[] eventValue = Scaler.perfCheck();
		  /**Array based**/
		  /*If the event counter hasn't been inserted with a value, store it, return otherwise.**/
//		  if (cmid >= ProfileQueue.eventCounterQueue[L3CACHEMISSES].length || 
//				  ProfileQueue.getEventCounter(cmid, L3CACHEMISSES) == 0) {
//			  double wallClockTime = System.currentTimeMillis();
//			  
//			  //Get value of counters from perf first.
//			  if(!Controller.options.ENABLE_COUNTER_PRINTER) {
//				  for(int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
//					  ProfileQueue.insertToEventCounterQueue(cmid, i, Scaler.perfCheck(i));
////					  ProfileQueue.insertToEventCounterQueue(cmid, i, eventValue[i]);
//				  }
//			  } else {
//				  //If counter printer is enabled, the data would be stored as socket1: hardware counters
//				  //+ energy consumption + socket 2: hardware counters + energy consumption + socket 3: ...
//				  double[] energy = EnergyCheckUtils.getEnergyStats();
//				  int counterIndex = 0;
//				  int enerIndex = 0;
//				  for(int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
//					  if(i < Scaler.perfCounters) {
//						  //Insert hardware counters in the first socket
//						  ProfileQueue.insertToEventCounterQueue(cmid, i, Scaler.perfCheck(counterIndex));
////						  ProfileQueue.insertToEventCounterQueue(cmid, i, eventValue[counterIndex]);
//						  counterIndex++;
//					  } else if((i - Scaler.perfCounters) % (Scaler.perfCounters + EnergyCheckUtils.ENERGY_ENTRY_SIZE) == 0) {
//						  //Insert Energy consumptions of dram/uncore gpu, cpu and package.
//						  for(int j = 0; j < EnergyCheckUtils.ENERGY_ENTRY_SIZE; j++) {
//							  ProfileQueue.insertToEventCounterQueue(cmid, i, energy[enerIndex]);
//							  i++;
//							  enerIndex++;
//						  }
//					  } else {
//						  //Insert hardware counters in the following sockets
//						  ProfileQueue.insertToEventCounterQueue(cmid, i, Scaler.perfCheck(counterIndex));
//						  counterIndex++;
//					  }
//				  }
//			  }
//			  ProfileQueue.insertToEventCounterQueue(cmid, Scaler.getPerfEnerCounterNum() - 1, wallClockTime);
//			  VM.sysWriteln("Prologue: cmid is: " + cmid 
//					  + " last index is: " + (Scaler.getPerfEnerCounterNum() - 1)
//					  + " cache start value: " + ProfileQueue.getEventCounter(cmid, 0)
//					  + " wall clock time: " + ProfileQueue.getEventCounter(cmid, 1));
//			  VM.sysWriteln("in prologue l3 cache misses: " + counters[0]);
//			  ProfileQueue.insertToEventCounterQueue(cmid, WALLCLOCKTIME, wallClockTime);
					
//			}
  /**Stack based
//		  recordEnergy(prologueSamples, energy);
//			System.out.println("**************start sampling**************");
//			long count = ++methodCount[index];
//			long[] energy = EnergyCheckUtils.getEnergyStats();
//			long[] time = TimeCheckUtils.getCurrentThreadTimeInfo();
//			long threadId = Thread.currentThread().getId();
			
//			ProfileStack.Push(COUNT, count); 
//			ProfileStack.Push(WALLCLOCKTIME, System.currentTimeMillis());
			ProfileStack.Push(INDEX, cmid);
//			ProfileStack.Push(USERTIME, time[0]);
//			ProfileStack.Push(CPUTIME, time[1]);
//			ProfileStack.Push(THREADID, threadId);
			
			//push energy information for each socket
			for (int i = 0; i < EnergyCheckUtils.socketNum; i++) {
				round = i * Scaler.cacheTLBEvents.length;
				ProfileStack.Push(L3CACHEMISSES + round, counter[0 + round]);
				ProfileStack.Push(TLBMISS + round, counter[1 + round]);
			}
			**/
		  

	  }
	  
	  private void recordEpilogueCounter(int cmid, CompiledMethod ypTakenInCM) {
//		  double startWallClockTime = 0.0d;
//		  double totalWallClockTime = 0.0d;
//		  double cacheMissRate = 0.0d;
//		  double cacheMissRateByTime = 0.0d;
//		  double branchMissRate = 0.0d;
//		  double branchMissRateByTime = 0.0d;
//		  double tlbMisses = 0.0d;
//		  int offset = 0;
//		  /**Event values for the method*/
//		  double[] eventValues = new double[Scaler.getPerfEnerCounterNum()];
//		  
//		  /**Array Based**/
//		  
//		  //If the event value of prologue for this method is 0, that means sampling just happens
//		  //in epilogue. 
////		  if(ProfileQueue.getEventCounter(cmid, L3CACHEMISSES) == 0) {
////			  return;
////		  }
//		  //If the event counter has been inserted with a value do subtraction and get the rate of events, otherwise, drop it.
//		  if (cmid < ProfileQueue.eventCounterQueue[L3CACHEMISSES].length && 
//				  ProfileQueue.getEventCounter(cmid, L3CACHEMISSES) != 0) {
//			  double wallClockTime = System.currentTimeMillis();
//			  
//			  //Scaler.getPerfEnerCounterNum() - 1 is the index of wallclocktime in eventCounterQueue.
//			  startWallClockTime = ProfileQueue.getEventCounter(cmid, Scaler.getPerfEnerCounterNum() - 1);
//			  totalWallClockTime = wallClockTime - startWallClockTime;
//			  
//			  //The time usage for the method is less than 100, but is considered as hot method by future execution time.
//			  //It's more likely the method is very short but frequently invoked. So we don't need consider
//			  //this method for dynamic profiling and scaling next time.
//			  if(totalWallClockTime < 100) {
//				  ProfileQueue.insertToShortButFreqMethods(cmid);
//				  reInitEventCounterQueue(cmid);
//				  return;
//			  }
//			  
//			  /**Event counter printer object*/
//			  DataPrinter printer = new DataPrinter(EnergyCheckUtils.socketNum, cmid, ypTakenInCM.getMethod().toString(), 
//								  									totalWallClockTime);
//			  if(Controller.options.ENABLE_COUNTER_PRINTER && !titleIsPrinted) {
//				  printer.printEventCounterTitle(EnergyCheckUtils.socketNum);
//				  titleIsPrinted = true;
//			  }
////			  long[] eventValue = Scaler.perfCheck();
//			  if(!Controller.options.ENABLE_COUNTER_PRINTER) {
//				  //Only hardware counters are calculated.
//				  for (int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
////					  eventValues[i] = Scaler.perfCheck(i) - ProfileQueue.getEventCounter(cmid, i);
//					  eventValues[i] = Scaler.perfCheck(i) - ProfileQueue.getEventCounter(cmid, i);
//	//				  VM.sysWriteln("Kenan: cmid is: " + cmid 
//	//						  				+ " cache misses: " + eventValues[i] 
//	//						  				+ " cache misses start: " + ProfileQueue.getEventCounter(cmid, i) 
//	//						  				+ " cache misses start: " + ProfileQueue.eventCounterQueue[i][cmid]
//	//						  				+ " cache misses end: " + Scaler.perfCheck(i) 
//	//						  				+ " wall clock time: " + totalWallClockTime 
//	//						  				+ " start wall clock time: " + startWallClockTime 
//	//						  				+ " end wall clock time: " + wallClockTime
//	//						  				);
//				  }
//			  } else {
//				  
//				  //If counter printer is enabled, the data would be stored as socket1: hardware counters
//				  //+ energy consumption + socket 2: hardware counters + energy consumption + socket 3: ...
//				  double[] energy = EnergyCheckUtils.getEnergyStats();
//				  int enerIndex = 0;
//				  int counterIndex = 0;
//				  for(int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
//					  if(i < Scaler.perfCounters) {
//						  //Insert hardware counters in the first socket
////						  eventValues[i] = Scaler.perfCheck(counterIndex) - ProfileQueue.getEventCounter(cmid, i);
//						  eventValues[i] = Scaler.perfCheck(counterIndex) - ProfileQueue.getEventCounter(cmid, i);
//						  counterIndex++;
////						  ProfileQueue.insertToEventCounterQueue(cmid, i, Scaler.perfCheck(i));
//					  } else if((i - Scaler.perfCounters) % (Scaler.perfCounters + EnergyCheckUtils.ENERGY_ENTRY_SIZE) == 0) {
//						  //Insert Energy consumptions of dram/uncore gpu, cpu and package.
//						  for(int j = 0; j < EnergyCheckUtils.ENERGY_ENTRY_SIZE; j++) {
//							  eventValues[i] = energy[enerIndex] - ProfileQueue.getEventCounter(cmid, i);
////							  ProfileQueue.insertToEventCounterQueue(cmid, i, energy[enerIndex]);
//							  i++;
//							  enerIndex++;
//						  }
//					  } else {
//						  //Insert hardware counters in the following sockets
////						  eventValues[i] = Scaler.perfCheck(counterIndex) - ProfileQueue.getEventCounter(cmid, i);
//						  eventValues[i] = Scaler.perfCheck(counterIndex) - ProfileQueue.getEventCounter(cmid, i);
//						  counterIndex++;
////						  ProfileQueue.insertToEventCounterQueue(cmid, i, Scaler.perfCheck(i));
//					  }
//					  
////					  VM.sysWriteln("Kenan: cmid is: " + cmid 
////							  				+ " cache misses: " + eventValues[i] 
////							  				+ " cache misses start: " + ProfileQueue.getEventCounter(cmid, i) 
////							  				+ " cache misses start: " + ProfileQueue.eventCounterQueue[i][cmid]
////							  				+ " cache misses end: " + Scaler.perfCheck(i) 
////							  				+ " wall clock time: " + totalWallClockTime 
////							  				+ " start wall clock time: " + startWallClockTime 
////							  				+ " end wall clock time: " + wallClockTime
////							  				);
//				  }
//			  }
//			  
//			  for(int i = 1; i <= Scaler.getPerfEnerCounterNum() - 1; i++) {
//				  //If scaling by counters is enabled, we need calculate cache miss rate and TLB misses
//				  //Otherwise, just simply store the perf counters user set from command line.
//				  if(Controller.options.ENABLE_SCALING_BY_COUNTERS && i % Scaler.perfCounters == 0) {
//					  //Move to the next index for L3CACHEMISSRATE event
//					  ++offset;
//
//					  cacheMissRate = ((double)eventValues[i + offset - Scaler.perfCounters] / (double)eventValues[i + offset - Scaler.perfCounters + 1]);
//					  //get TLB misses
//					  tlbMisses = eventValues[i + offset - Scaler.perfCounters + 2] + eventValues[i + offset -Scaler.perfCounters + 3];
//					  ProfileQueue.insertToHotMethodsByEventCounter(cmid, i + offset - 1, (long)cacheMissRate);
//					  //Move to the next index for TLBMISSES
//					  ++offset;
//					  ProfileQueue.insertToHotMethodsByEventCounter(cmid, i + offset - 1, tlbMisses);
//					  continue;
//				  } 
//				  ProfileQueue.insertToHotMethodsByEventCounter(cmid, i + offset - 1, eventValues[i - 1]);
//			  }
//			//Insert power consumption and hardware counter into correlation matrix
////			  if(Controller.options.ENABLE_COUNTER_PRINTER) {
////				  long power = (long)(eventValues[eventValues.length - 2] / (eventValues[eventValues.length - 1] / 1000.0)); 
////				  //Power and event counter pairs
////				  for(int i = 0; i < eventValues.length - 4; i++) {
////					  ProfileQueue.insertToMatrix(0, cmid, power);
////					  ProfileQueue.insertToMatrix(1, cmid, eventValues[i]);
////				  }
////			  }
//			  
////			  new PearsonsCorrelation().computeCorrelationMatrix(ProfileQueue.matrix);
//			  
//			  //Print the profiling information based on event counters
//			  if(Controller.options.ENABLE_COUNTER_PRINTER) {
//				  if(Scaler.perfCounters == 1) {
//					  printer.printEventCounterValues(Controller.options.FREQUENCY_TO_BE_PRINTED, eventValues);
//				  } else if(Scaler.perfCounters >= 4){
//					  //TODO: Assume the first four counters are 'cache-misses' 
//					  //'cache-references' 'dTLB-load-misses' 'iTLB-load-misses'. Too ugly.
//					  cacheMissRate = eventValues[0] / eventValues[1];
//					  cacheMissRateByTime = eventValues[0] / totalWallClockTime;
//					  tlbMisses = eventValues[2] + eventValues[3];
//					  printer.printALl(Controller.options.FREQUENCY_TO_BE_PRINTED, eventValues, cacheMissRate, cacheMissRateByTime, tlbMisses);
//				  } else if(Controller.options.EVENTCOUNTER.equals(Controller.options.CACHE_MISS_RATE)) {
//					  //TODO: Requires the sequence as "cache-misses,cache-references" in arguments. Too ugly. 
//					  cacheMissRate = eventValues[0] / eventValues[1];
//					  cacheMissRateByTime = eventValues[0] / totalWallClockTime;
//					  printer.printProfInfoTwo(Controller.options.FREQUENCY_TO_BE_PRINTED, eventValues, cacheMissRate, cacheMissRateByTime);
//				  } else if(Controller.options.EVENTCOUNTER.equals(Controller.options.BRANCH_MISS_RATE)) {
//					  //TODO: Requires the sequence as "branch-misses,branches" in arguments. Too ugly. 
//					  branchMissRate = eventValues[0] / eventValues[1];
//					  branchMissRateByTime = eventValues[0] / totalWallClockTime;;
//					  printer.printProfInfoTwo(Controller.options.FREQUENCY_TO_BE_PRINTED, eventValues, branchMissRate, branchMissRateByTime);
//				  }
//			  }
//			  
//			  //Last event is always wall clock time.
//			  ProfileQueue.insertToHotMethodsByEventCounter(cmid, Scaler.getTotalEventNum() - 1, totalWallClockTime);
//			  //Reset the current method event counter queue since it has been calculated.
//			  reInitEventCounterQueue(cmid);
			  
//		  }
		  
		  /**Stack based
//		  recordEnergy(epilogueSamples, energy);
//			System.out.println("**************end sampling**************");

//			long[] time = TimeCheckUtils.getCurrentThreadTimeInfo();
//			long count = ProfileStack.Pop(COUNT);
//			long startWCTime = ProfileStack.Pop(WALLCLOCKTIME);
			long popIndex = ProfileStack.Pop(INDEX);
//			VM.sysWriteln("pop method id: " + popIndex);
			VM.sysWriteln("pop method id: " + popIndex + " method name: " + ypTakenInCM.getMethod());
//			long startUsrTime= ProfileStack.Pop(USERTIME);
//			long startCpuTime = ProfileStack.Pop(CPUTIME);
//			long startDramEner = ProfileStack.Pop(DRAMENERGY);
//			long startCpuEner = ProfileStack.Pop(CPUENERGY);
//			long startPkgEner = ProfileStack.Pop(PKGENERGY);
//			long threadId = ProfileStack.Pop(THREADID);
			long startL3CacheMiss = ProfileStack.Pop(L3CACHEMISSES);
			long startTlbMiss = ProfileStack.Pop(TLBMISS);
			
//			VM.sysWriteln("popindex: " + popIndex + " method: " + methodNameList[(int)popIndex] + " current index: " + index + " method: " + methodNameList[index]);
			while(popIndex != cmid && popIndex != -1) {
				VM.sysWriteln("popindex cannot match with current index, keep poping");
				VM.sysWriteln("============dump stack start==========");
				ProfileStack.dump(INDEX);
				VM.sysWriteln("============dump stack end==========");
		
//				time = TimeCheckUtils.getCurrentThreadTimeInfo();
//				count = ProfileStack.Pop(COUNT);
//				startWCTime = ProfileStack.Pop(WALLCLOCKTIME);
				popIndex = ProfileStack.Pop(INDEX);
				VM.sysWriteln("keep poping method id: " + popIndex + " method name: " + ypTakenInCM.getMethod());
//				long startUsrTime= ProfileStack.Pop(USERTIME);
//				long startCpuTime = ProfileStack.Pop(CPUTIME);
				//TODO: Check socket number
//				startDramEner = ProfileStack.Pop(DRAMENERGY);
//				startCpuEner = ProfileStack.Pop(CPUENERGY);
//				startPkgEner = ProfileStack.Pop(PKGENERGY);
				
				startL3CacheMiss = ProfileStack.Pop(L3CACHEMISSES);
				startTlbMiss = ProfileStack.Pop(TLBMISS);
				
			}
			//TODO: Handle ioexception
//			if (cmid != popIndex)
//				VM.sysWriteln("TimeProfiling fail function Pop(0): ");
			
//			if (endWCTime - startWCTime >= THREASHOLD) {
				
				
				VM.sysWriteln("popIndex: " + popIndex + " current method index: " + cmid
						+ " stack size recorded: " + ProfileStack.stackSize[INDEX] +
						" real stack length: " + ProfileStack.stack[INDEX].length);

//				VM.sysWriteln("startPkgEner: " + startPkgEner +
//						" endsampling energy: " + energy[2]
//								+ " energy consumed: " + (energy[2] - startPkgEner) + " index: " + cmid);
				VM.sysWriteln("start sampling cache misses: " + startL3CacheMiss +
						" end sampling cache misses: " + counter[0]
								+ " total cache misses: " + (counter[0] - startL3CacheMiss) + " index: " + cmid);
				
//				AccmProfile.add(WALLCLOCKTIME, cmid, endWCTime - startWCTime);
//				VM.sysWriteln("record time usage: " + (endWCTime - startWCTime));
//				AccmProfile.add(USERTIME, index, time[0] - startUsrTime);
//				AccmProfile.add(CPUTIME, index, time[1] - startCpuTime);
			if (popIndex != -1) {
				AccmProfile.add(L3CACHEMISSES, cmid, counter[0] - startL3CacheMiss);
				AccmProfile.add(TLBMISS, cmid, counter[1] - startTlbMiss);
			}
				
//			}
				
				//When main method be poped
//				if(index == 1) {
//					//TODO: Prints into a file.
//					VM.sysWriteln("main startPkgEner: " + AccmProfile.get(PKGENERGY, 0));
//					VM.sysWriteln("method index: " + index);
//					VM.sysWriteln("package energy consumed" + AccmProfile.get(PKGENERGY, index));
//					VM.sysWriteln("time use: " + AccmProfile.get(WALLCLOCKTIME, index));
//				}
 * 
 */

	  }
	  
	  private void reInitEventCounterQueue(int cmid) {
		  for(int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
			  ProfileQueue.insertToEventCounterQueue(cmid, i, 0);
		  }
		  ProfileQueue.insertToEventCounterQueue(cmid, Scaler.getPerfEnerCounterNum() - 1, 0);
	  }
	  
	  private void recordEnergy(long[][] samples, long[] energy) {
//	     reserve the next available slot
//	    int idx = Synchronization.fetchAndAdd(this, AosEntrypoints.methodListenerNumSamplesField.getOffset(), 1);
//	    // make sure it is valid
//	    if (idx < sampleSize) {
//	      samples[idx] = CMID;
//	    }
//	    if (idx + 1 == sampleSize) {
//	      // The last sample.
//	      activateOrganizer();
//	    }
	  }

	  @Override
	  public void report() { }

	  @Override
	  public void reset() {
//	    numSamples = 0;
	  }
}
