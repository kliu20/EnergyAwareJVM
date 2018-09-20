package org.jikesrvm.energy;

import java.util.List;

import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.adaptive.controller.Controller;
import org.vmmagic.pragma.Entrypoint;

public class Service implements ProfilingTypes {
	public static final long THREASHOLD = 500;
	public static final boolean changed = false;
	public static boolean isJraplInit = false;
	public static boolean isSamplingInit = false;
	public native static int scale(int freq);
	public native static int[] freqAvailable();
	public static final int INIT_SIZE = 500;
	public static boolean titleIsPrinted = false;
	public static String[] clsNameList = new String[INIT_SIZE];
	public static String[] methodNameList = new String[INIT_SIZE];
	public static long[] methodCount = new long[INIT_SIZE];
	
	/**Index is composed by hashcode of "method ID#thread ID" in order to differentiate method invocations by different threads*/
	public static char [] info = {'i','o', '\n'};

	public static int currentPos = 0;
	/**
	 * Get a smaller hashcode value than String.hashcode(). Since we only need calculate hashcode for combination of numbers
	 * and '#' only. It's less likely be collided if we use a smaller primes, and it would save much memory.
	 * @return
	 */
	public static int getHashCode(String key) {
		char[] str = key.toCharArray();
		int hash = 0;
        if (str.length > 0) {

            for (int i = 0; i < str.length; i++) {
            	hash = 7 * hash + str[i];
            }
        }
        return hash;
	}

	public static int addMethodEntry(String cls, String name){
		
		//Check boundaries of arrays
		if (methodCount.length - 1 == currentPos){
			int len = methodCount.length;
			String[] newClsNameList = new String[len * 2];
			String[] newMethodNameList = new String[len * 2];
			long[] newMethodCount = new long[len * 2];
			if (VM.VerifyAssertions)
				VM._assert(clsNameList.length - 1 == currentPos && methodNameList.length - 1== currentPos && methodCount.length - 1== currentPos, "Service error");
			
			System.arraycopy(clsNameList,0,newClsNameList,0,len);
			System.arraycopy(methodNameList,0,newMethodNameList,0,len);
			System.arraycopy(methodCount,0,newMethodCount,0,len);
			clsNameList = newClsNameList;
			methodNameList = newMethodNameList;
			methodCount = newMethodCount;
		}
		methodNameList[currentPos] = name;
		clsNameList[currentPos] = cls;
		methodCount[currentPos] = 0;
		currentPos++;
		return currentPos - 1;
	}

	  private static void reInitEventCounterQueue(int cmid) {
		  for(int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
			  ProfileQueue.insertToEventCounterQueue(cmid, i, 0);
		  }
		  ProfileQueue.insertToEventCounterQueue(cmid, Scaler.getPerfEnerCounterNum() - 1, 0);
	  }
	  
	  public static void getProfileAttrs(Double[] profileAttrs) {
		double perfCounter;
		int eventId = 0;
		//Loop unwinding
		if (Controller.options.ENABLE_COUNTER_PROFILING) {
			for (int i = 0; i < Scaler.perfCounters; i++) {
				perfCounter = Scaler.perfCheck(i);
				profileAttrs[eventId++] = perfCounter;
				
			}
		}

		if (Controller.options.ENABLE_ENERGY_PROFILING) {
			
			double[] energy = EnergyCheckUtils.getEnergyStats();
			
			for (int i = 0; i < EnergyCheckUtils.ENERGY_ENTRY_SIZE; i++) {
				profileAttrs[eventId++] = energy[i];
			}
		}
		
	  }

	  @Entrypoint
	  public static void startProfile(int cmid) {

		RVMThread thread = RVMThread.getCurrentThread();
		//Using sampling based method to profile
		if (thread.energyTimeSliceExpired != 0) {

			thread.energyTimeSliceExpired = 0;

			Double[] profileAttrs = new Double[Scaler.getPerfEnerCounterNum()];
			int threadId = (int)Thread.currentThread().getId();
			// Check if the current method is not short method (long method) or it hasn't been calculated
			Boolean isSkippable = ProfileQueue.isSkippableMethod(cmid);

			if (isSkippable == null || isSkippable != null && !isSkippable) {
			
				double wallClockTime = System.currentTimeMillis();
		
				String key = ProfileMap.createKey(threadId, cmid);
				//Profiling 
				getProfileAttrs(profileAttrs);
		
				/**Preserve for dynamic scaling*/ 
		//		int counterIndex = 0;
		//		double[] energy = EnergyCheckUtils.getEnergyStats();
		//		for (int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
		//			if (i < Scaler.perfCounters) {
		//				perfCounter = Scaler.perfCheck(counterIndex);
		//				// Insert hardware counters in the first socket
		//				// ProfileStack.push(i, (int)threadId, cmid, perfCounter);
		//				ProfileMap.put(i, (int) threadId, cmid, perfCounter);
		//				counterIndex++;
		//			} else {
		//				for (int j = 0; j < EnergyCheckUtils.ENERGY_ENTRY_SIZE; j++) {
		//					// ProfileStack.push(i, (int)threadId, cmid,
		//					// energy[enerIndex]);
		//					ProfileMap.put(i, (int) threadId, cmid, energy[enerIndex]);
		//					i++;
		//					enerIndex++;
		//				}
		//			}
		//		}
		
				// ProfileStack.push(Scaler.getPerfEnerCounterNum() - 1, (int)threadId, cmid, wallClockTime);
				profileAttrs[profileAttrs.length - 1] = wallClockTime;
				ProfileMap.put(key, profileAttrs);
				LogQueue.addStartLogQueue(threadId, cmid, profileAttrs);
			}
		}
	}

	@Entrypoint
	public static void endProfile(int cmid){
		RVMThread thread = RVMThread.getCurrentThread();
		//Using sampling based method to profile
		if (thread.energyTimeSliceExpired != 0) {

			thread.energyTimeSliceExpired = 0;

			double startWallClockTime = 0.0d;
			double totalWallClockTime = 0.0d;
			double tlbMisses = 0.0d;
			double missRate = 0.0d;
			int offset = 0;
			/** Event values for the method */
			Double[] profileAttrs = new Double[Scaler.getPerfEnerCounterNum()];
			int threadId = (int) Thread.currentThread().getId();

			// Check if the current method is not short method (long method) or it hasn't been calculated
			Boolean isSkippable = ProfileQueue.isSkippableMethod(cmid);

			if (isSkippable == null || isSkippable != null && !isSkippable) {
				
				String key = ProfileMap.createKey(threadId, cmid);
				//If the event counter has not been profiled or the previous value is 0, drop this measurement.
				if (ProfileMap.getValue(Scaler.getPerfEnerCounterNum() - 1, key) == 0) {
					return;
				}
				Double[] methodPreamble = ProfileMap.remove(key);
				// Over recursive call threshold check. If it's null, that means the current method
				// is invoked itself more than a certain threshold times. We only need profile the information of the whole
				// recursive calls
				if (methodPreamble == null) {
					return;
				}
				double wallClockTime = System.currentTimeMillis();
				startWallClockTime = methodPreamble[Scaler.getPerfEnerCounterNum() - 1];
				totalWallClockTime = wallClockTime - startWallClockTime;
				// startWallClockTime = ProfileStack.pop(Scaler.getPerfEnerCounterNum() - 1, threadId, cmid);
				// Over recursive call threshold check. If it's a negative value, that means the current method
				// is invoked itself more than a certain threshold times. We only need care the information of the whole
				// recursive calls
		
				// Get greater range since the method execution time varies a lot
				double min = Controller.options.HOT_METHOD_TIME_MIN;
				double max = Controller.options.HOT_METHOD_TIME_MAX;

				  //Calculate method's profiling information
				//calculateProfile(profileAttrs, methodPreamble, cmid);
				getProfileAttrs(profileAttrs);

				if (isSkippable == null) {
					if (totalWallClockTime < 40) {
						ProfileQueue.setSkippableMethod(cmid);
						return;
					} else {
						ProfileQueue.setNonskippableMethod(cmid);
					}
				}
		
				  /**Event counter printer object*/
				  if(Controller.options.ENABLE_COUNTER_PRINTER && !titleIsPrinted) {
					  //DataPrinter.printEventCounterTitle(Controller.options.ENABLE_COUNTER_PROFILING, Controller.options.ENABLE_ENERGY_PROFILING);
					  titleIsPrinted = true;
				  }
				  
				  //TODO: For enable_scaling_by_counters for future
	//			  for(int i = 1; i <= Scaler.getPerfEnerCounterNum() - 1; i++) {
	//				  
	//				  //If scaling by counters is enabled, we need calculate cache miss rate and TLB misses
	//				  //Otherwise, just simply store the perf counters user set from command line.
	//				  if(Controller.options.ENABLE_SCALING_BY_COUNTERS && i % Scaler.perfCounters == 0) {
	//					  //Move to the next index for L3CACHEMISSRATE event
	//					  ++offset;
	//					  missRate = ((double)profileAttrs[i + offset - Scaler.perfCounters] / (double)profileAttrs[i + offset - Scaler.perfCounters + 1]);
	//					  //get TLB misses
	//					  tlbMisses = profileAttrs[i + offset - Scaler.perfCounters + 2] + profileAttrs[i + offset -Scaler.perfCounters + 3];
	//					  LogQueue.addRatio(threadId, cmid, miss
	//					  LogQueue.add(i + offset - 1, threadId, cmid, missRate);
	//					  //Move to the next index for TLBMISSES
	//					  ++offset;
	//					  LogQueue.add(i + offset - 1, threadId, cmid, tlbMisses);
	//					  continue;
	//				  }
	//				  LogQueue.add(i + offset - 1, threadId, cmid, profileAttrs[i - 1]);
	//			  }
				  
				  //Last entry for wall clock time
				  int wallClockTimeEntry = Scaler.getPerfEnerCounterNum() - 1;
				  profileAttrs[wallClockTimeEntry] = totalWallClockTime;
				  LogQueue.addEndLogQueue(threadId, cmid, profileAttrs);

				  //Print the profiling information based on event counters
				  //printProfile(profileAttrs, cmid, totalWallClockTime);
			}
		}
	}

	public static void calculateProfile(double[] profileAttrs, Double[] methodPreamble, int cmid) {
		  if(!Controller.options.ENABLE_COUNTER_PRINTER) {
			  //Only hardware counters are calculated.
			  for (int i = 0; i < Scaler.getPerfEnerCounterNum() - 1; i++) {
				  profileAttrs[i] = Scaler.perfCheck(i) - methodPreamble[i];
			  }
			  return;
		  }
		  if(Controller.options.ENABLE_COUNTER_PROFILING) {
			  int counterIndex = 0;
			  int i = 0;
			  
			  while (i < Scaler.perfCounters) {
				  //Insert hardware counters in the first socket
				  profileAttrs[i] = Scaler.perfCheck(counterIndex) - methodPreamble[i];
				  counterIndex++;
				  i++;
			  }
			  //If energy profiling is enabled
			  if(Controller.options.ENABLE_ENERGY_PROFILING) {
				  int enerIndex = 0;
				  double[] energy = EnergyCheckUtils.getEnergyStats();
				  
				  while (i < Scaler.getPerfEnerCounterNum() - 1) {
					  //Insert Energy consumptions of dram/uncore gpu, cpu and package.
					  profileAttrs[i] = energy[enerIndex] - methodPreamble[i];
					  i++;
					  enerIndex++;
				  }
			  }
		  } else if(Controller.options.ENABLE_ENERGY_PROFILING) {
			  //If counter printer is enabled, the data would be stored as socket1: hardware counters
			  double[] energy = EnergyCheckUtils.getEnergyStats();
			  //Insert Energy consumptions of dram/uncore gpu, cpu and package.
			  for(int i = 0; i < EnergyCheckUtils.ENERGY_ENTRY_SIZE; i++) {
				  profileAttrs[i] = energy[i] - methodPreamble[i];
			  }
		  }
		  
	}
	
	public static void printProfile(Double[] profileAttrs, int cmid, double totalWallClockTime) {
		double missRate = 0.0d;
		double missRateByTime = 0.0d;
		double tlbMisses = 0.0d;
		double tlbMissByTime = 0.0d;
		double countersByTime = 0.0d;
		if (Controller.options.ENABLE_COUNTER_PRINTER) {

			if (Controller.options.ENABLE_COUNTER_PROFILING) {
				if (Scaler.perfCounters >= 4) {
					// TODO: Assume the first four counters are 'cache-misses'
					// 'cache-references' 'dTLB-load-misses' 'iTLB-load-misses'.
					// Too ugly.
					missRate = profileAttrs[0] / profileAttrs[1];
					missRateByTime = profileAttrs[0] / totalWallClockTime;
					tlbMisses = profileAttrs[2] + profileAttrs[3];
					if (Controller.options.ENABLE_ENERGY_PROFILING) {
						DataPrinter.printALl(cmid, clsNameList[cmid] + "."
								+ methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, missRate, missRateByTime,
								tlbMisses);
					}
				} else if (Controller.options.EVENTCOUNTER
						.equals(Controller.options.CACHE_MISS_RATE)
						|| Controller.options.EVENTCOUNTER
								.equals(Controller.options.BRANCH_MISS_RATE)) {
					// TODO: Requires the sequence as
					// "cache-misses,cache-references" in arguments. Too ugly.
					missRate = profileAttrs[0] / profileAttrs[1];
					missRateByTime = profileAttrs[0] / totalWallClockTime;

					if (Controller.options.ENABLE_ENERGY_PROFILING) {
						DataPrinter.printProfInfoTwo(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, missRate, missRateByTime);
					} else {
						DataPrinter.printCounterInfo(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, missRate, missRateByTime);
					}

				} else if (Controller.options.EVENTCOUNTER
						.equals(Controller.options.TLB_MISSES)) {
					tlbMisses = profileAttrs[0] + profileAttrs[1];
					tlbMissByTime = tlbMisses / totalWallClockTime;

					if (Controller.options.ENABLE_ENERGY_PROFILING) {
						DataPrinter.printProfInfoTwo(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, tlbMisses, tlbMissByTime);
					} else {
						DataPrinter.printCounterInfo(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, tlbMisses, tlbMissByTime);
					}
				} else if (Controller.options.EVENTCOUNTER
						.equals(Controller.options.CONTEXT_SWITCHES)
						|| Controller.options.EVENTCOUNTER
								.equals(Controller.options.PAGE_FAULTS)
						|| Controller.options.EVENTCOUNTER
								.equals(Controller.options.CPU_CYCLES)
						|| Controller.options.EVENTCOUNTER
								.equals(Controller.options.CPU_CLOCK)) {

					countersByTime = profileAttrs[0] / totalWallClockTime;

					if (Controller.options.ENABLE_ENERGY_PROFILING) {
						DataPrinter.printProfInfoOne(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, countersByTime);
					} else {
						DataPrinter.printCounterInfo(cmid, clsNameList[cmid]
								+ "." + methodNameList[cmid],
								Controller.options.FREQUENCY_TO_BE_PRINTED,
								profileAttrs, 0, countersByTime);
					}
				}
			} else if (!Controller.options.ENABLE_COUNTER_PROFILING
					&& Controller.options.ENABLE_ENERGY_PROFILING) {
				// Only time and energy measurement
				DataPrinter.printEnerInfo(cmid, clsNameList[cmid] + "."
						+ methodNameList[cmid],
						Controller.options.FREQUENCY_TO_BE_PRINTED,
						profileAttrs);
			}
		}
		// Last event is always wall clock time.
	//	LogQueue.addLogQueue(threadId, cmid, profileAttrs);
		// totalWallClockTime);
	}

	//TODO: how to deal with other array, like char[] array?
	//Set hander for this arr, next time barrier access the array would reset it.
//	public static void ioArgSampling(Object obj){
//		if (VM.isBooted && VM.dumpMemoryTrace){
//			MiscHeader.setIOTag(obj);
//			Address addr = ObjectReference.fromObject(obj).toAddress().plus((int)VM.ioMemAccOffset);
//			if (VM.dumpMemoryTrace)
//				VM.sysAppendMemoryTrace(addr);
//		}
//	}
}

/*
 * ==========================================
 * Java File I/O: in RVM could do FILE IO : )
 * ==========================================
 *
 * PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
		writer.println("The first line");
		writer.println("The second line");
		writer.close();
 * */



/*================================
 * Copy Array
 * ===============================
 *
String [] tmp1 = new String[2*len];
String [] tmp2 = new String[2*len];
long [] tmp3 = new long[2*len];
//System.arraycopy(arg0, arg1, arg2, arg3, arg4);
clsNameList = tmp1;
methodNameList = tmp2;
methodCount = tmp3;
 */


/*============================================================
 * These code, i.e load libary, unable to call by RVM, even RVM
 * running scaler could not success.
 *
System.load("/home/jianfeih/workspace/jacob/Jikes-Miss-Prediction/CPUScaler/libCPUScaler.so");
System.out.println("libary loaded");
if (!changed){
	int[] a = freqAvailable();
	scale(a[6]);
	changed f true;
}
 */
