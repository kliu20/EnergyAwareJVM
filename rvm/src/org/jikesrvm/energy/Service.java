package org.jikesrvm.energy;

import java.util.List;
import java.lang.ThreadLocal;

import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.adaptive.controller.Controller;
import org.vmmagic.pragma.Entrypoint;
import org.jikesrvm.runtime.SysCall;

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
	public static double[][] prevProfile = new double[INIT_SIZE*2][3];
	public static boolean[] prevProfileInit = new boolean[INIT_SIZE*2];
	public static boolean profileEnable = false;
	public static long start_ts = System.currentTimeMillis(); 

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


		public synchronized static int addMethodEntry(String cls, String name){
			//name=name+"\0";
			//cls=cls+"\0";
			String fullName = cls+"$$$$$"+name;
			fullName+="\0";
			return SysCall.sysCall.add_method_entry(fullName.getBytes(),"".getBytes());	
		}

		/**
		 * Do profile
		 * @param profileAttrs the collection that contains profile information 
		 */
		  public static void getProfileAttrs(double[] profileAttrs) {
			double perfCounter = 0.0d;
			int eventId = 0;
			int threadId = (int)Thread.currentThread().getId();
			double startTime = 0.0d;
			//Loop unwinding
			if (!prevProfileInit[threadId]) {
				prevProfileInit[threadId] = true;
				if (Controller.options.ENABLE_COUNTER_PROFILING) {
					for (int i = 0; i < Scaler.perfCounters; i++) {

						prevProfile[threadId][eventId] = Scaler.perfCheck(i);
						eventId++;
					}
				}
				if (Controller.options.ENABLE_ENERGY_PROFILING) {

					double[] energy = EnergyCheckUtils.getEnergyStats();

					for (int i = 0; i < EnergyCheckUtils.ENERGY_ENTRY_SIZE; i++) {
						prevProfile[threadId][eventId] = energy[i];
						eventId++;
					}
				}
			} else {
				if (Controller.options.ENABLE_COUNTER_PROFILING) {
					for (int i = 0; i < Scaler.perfCounters; i++) {

						perfCounter = Scaler.perfCheck(i);

						profileAttrs[eventId] = perfCounter - prevProfile[threadId][eventId];
						prevProfile[threadId][eventId] = perfCounter;
						eventId++;
					}
				}

				if (Controller.options.ENABLE_ENERGY_PROFILING) {
					
					double[] energy = EnergyCheckUtils.getEnergyStats();
					
					for (int i = 0; i < EnergyCheckUtils.ENERGY_ENTRY_SIZE; i++) {

						profileAttrs[eventId] = energy[i]- prevProfile[threadId][eventId];
						prevProfile[threadId][eventId] = energy[i];
						eventId++;
					}
				}
			}
		  }

		  public static void enableProfile() {
			VM.sysWriteln("Profiling Enabled ... Stay tuned!");
		  	profileEnable = true;
	 	 }

	@Entrypoint
	public static void startProfile(int cmid) {
		profile(cmid);
	}

	@Entrypoint
	public static void endProfile(int cmid) {
		profile(cmid);
	}

	public static void profile(int cmid) {
		RVMThread thread = RVMThread.getCurrentThread();
		//Using sampling based method to profile
		if (thread.energyTimeSliceExpired >= 2) {

			thread.skippedInvocations--;

			if (thread.skippedInvocations == 0) {	

				/** Event values for the method */
				double[] profileAttrs = new double[Scaler.getPerfEnerCounterNum()];
				int threadId = (int) Thread.currentThread().getId();
				
				//Do profile	
				getProfileAttrs(profileAttrs);
				int freq = (int) Controller.options.FREQUENCY_TO_BE_PRINTED;
				SysCall.sysCall.add_log_entry(profileAttrs,cmid,System.currentTimeMillis() - start_ts,freq);
				
				thread.skippedInvocations = RVMThread.STRIDE;
				thread.samplesThisTimerInterrupt--;

				if (thread.samplesThisTimerInterrupt == 0) {
					thread.samplesThisTimerInterrupt = RVMThread.SAMPLES;
					thread.energyTimeSliceExpired = 0;
				}
			}
		}

	}
}