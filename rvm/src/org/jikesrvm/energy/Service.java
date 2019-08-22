package org.jikesrvm.energy;

import java.util.List;
import java.lang.ThreadLocal;

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
	public static double[][] prevProfile = new double[INIT_SIZE][10];
	public static boolean prevProfileInit = false;
	
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

	/**
	 * Do profile
	 * @param profileAttrs the collection that contains profile information 
	 */
	  public static void getProfileAttrs(Double[] profileAttrs) {
		double perfCounter = 0.0d;
		int eventId = 0;
		int threadId = (int)Thread.currentThread().getId();
		//Loop unwinding
		if (Controller.options.ENABLE_COUNTER_PROFILING) {
			for (int i = 0; i < Scaler.perfCounters; i++) {
				perfCounter = Scaler.perfCheck(i);
				VM.sysWriteln("check: " + Scaler.perfCheck(i) + " perfCounter: " + perfCounter);
				if (perfCounter < 0) {
					DataPrinter.filePrinter.println("hardware counter is negative!");
				}
				if (!prevProfileInit) {
					VM.sysWriteln("eventId is: " + eventId);
					VM.sysWriteln("prevProfile lenth is: " + prevProfile.length + " " + prevProfile[0].length);
					profileAttrs[eventId] = perfCounter;
					prevProfile[threadId][eventId] = perfCounter;
					prevProfileInit = true;
					eventId++;
				} else {
					prevProfile[threadId][eventId] = perfCounter - prevProfile[threadId][eventId];
					VM.sysWriteln("eventId is: " + eventId);
					VM.sysWriteln("profileAttrs.length is: " + profileAttrs.length);
					if (eventId >= profileAttrs.length || eventId < 0) {
						VM.sysWriteln("eventId over the limit is: " + eventId + " profileAttrs.length: " + profileAttrs.length);
					}
					if (profileAttrs[eventId] < 0) {
						DataPrinter.filePrinter.println("hardware difference value is negative!");
						DataPrinter.filePrinter.println(perfCounter + " " + prevProfile[threadId][eventId] + " " + profileAttrs[eventId]);
					}
					prevProfile[threadId][eventId] = profileAttrs[eventId];
					eventId++;
				}
			}
		}

		if (Controller.options.ENABLE_ENERGY_PROFILING) {
			
			double[] energy = EnergyCheckUtils.getEnergyStats();
			
			for (int i = 0; i < EnergyCheckUtils.ENERGY_ENTRY_SIZE; i++) {
				if (energy[i] < 0) {
					DataPrinter.filePrinter.println("energy value is negative!");
				}
				if (!prevProfileInit) {
					profileAttrs[eventId] = energy[i];
					prevProfile[threadId][eventId] = energy[i];
					prevProfileInit = true;
					eventId++;
				} else {
	
					profileAttrs[eventId] = energy[i]- prevProfile[threadId][eventId];
					if (profileAttrs[eventId] < 0) {
						DataPrinter.filePrinter.println("energy difference value is negative!");
						DataPrinter.filePrinter.println(energy[i] + " " + prevProfile[threadId][eventId] + " " + profileAttrs[eventId]);
					}
					prevProfile[threadId][eventId] = profileAttrs[eventId];
					eventId++;
				}
			}
		}
	  }

	  @Entrypoint
	  public static void startProfile(int cmid) {

		RVMThread thread = RVMThread.getCurrentThread();
		//Using sampling based method to profile
		if (thread.energyTimeSliceExpired % 2 != 0) {

			Double[] profileAttrs = new Double[Scaler.getPerfEnerCounterNum()];
			int threadId = (int)Thread.currentThread().getId();
			double wallClockTime = System.currentTimeMillis();
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
			LogQueue.addLogQueue(threadId, cmid, profileAttrs, (long)(thread.energyTimeSliceExpired * VM.interruptQuantum));

			thread.energyTimeSliceExpired = 0;
		}
	}

	@Entrypoint
	public static void endProfile(int cmid) {
		RVMThread thread = RVMThread.getCurrentThread();
		//Using sampling based method to profile
		if (thread.energyTimeSliceExpired % 2 != 0) {

			double tlbMisses = 0.0d;
			double missRate = 0.0d;
			int offset = 0;
			/** Event values for the method */
			Double[] profileAttrs = new Double[Scaler.getPerfEnerCounterNum()];
			int threadId = (int) Thread.currentThread().getId();
			double wallClockTime = System.currentTimeMillis();
			
			//Do profile	
			getProfileAttrs(profileAttrs);

			  /**Event counter printer object*/
//			  if(Controller.options.ENABLE_COUNTER_PRINTER && !titleIsPrinted) {
//				  //DataPrinter.printEventCounterTitle(Controller.options.ENABLE_COUNTER_PROFILING, Controller.options.ENABLE_ENERGY_PROFILING);
//				  titleIsPrinted = true;
//			  }
			  
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
			  LogQueue.addLogQueue(threadId, cmid, profileAttrs, (long)(thread.energyTimeSliceExpired * VM.interruptQuantum));

			thread.energyTimeSliceExpired = 0;
		}
	}
}
