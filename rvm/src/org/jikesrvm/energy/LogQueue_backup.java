package org.jikesrvm.energy;

import org.jikesrvm.VM;

/**
 * Almost the same as ProfileStack, This class must be used with thread safe way since 
 * its methods are not synchronzed.
 */
public class LogQueue implements ProfilingTypes {
	
	/**Record the event value corresponding to the entry ID*/
	public static double[][] logQueueEvent;
	/**Record the thread ID corresponding to the entry ID*/
	public static int[] logQueueThread;
	/**Record the method ID corresponding to the entry ID*/
	public static int[] logQueueMethod;
	/**Log entry ID*/
	public static int entryId = 0;
	/**
	 * For calculating profiling information
	 * 1st dimension: eventId
	 * 2nd dimension: threadId
	 * 3rd dimension: methodId
	 * 4th dimension: stackId
	 */
	public static double[][][][] hotMethodsProfLog;
	/**
	 * hotMethodsProfLog size for each profiling metric
	 */
	public static int [][][] logSize;
	
	public static int methodCount = 0;
	
	//Size for the occurences of method invocations
	public static int ENTRYSIZE = 10000;
	
	public static void InitStack(int socketNum) {
		if (logQueueThread == null) {
			logQueueEvent = new double[ENTRYSIZE][Scaler.getPerfCounterNum()];
			logQueueThread = new int[ENTRYSIZE];
			logQueueMethod = new int[ENTRYSIZE];
		}
	}
	
	/**
	 * Once stack size of one methodId exceeds the stack boundary, increase the stack size for 
	 * all threads and events of this method stack
	 */
	
	public static void growLogQueue() {

		double[][] newLogQueueEvent = new double[logQueueEvent.length * 1.5][Scaler.getPerfCounterNum()];
		int[] newLogQueueThread = new int[logQueueEvent.length * 1.5];
		int[] newLogQueueMethod = new int[logQueueEvent.length * 1.5];

		System.arraycopy(logQueueThread, 0, newLogQueueThread, 0, logQueueThread.length);
		System.arraycopy(logQueueMehtod, 0, newLogQueueMethod, 0, logQueueMethod.length);

		for (int i = 0; i < logQueueEvent.length; i++) {
			System.arraycopy(logQueueEvent[i], 0, newLogQueueEvent[i], 0, logQueueMethod[i].length);
		}

		logQueueThread = newLogQueueThread;
		logQueueMethod = newLogQueueMethod;
		logQueueEvent = newLogQueueEvent;
	}
	
	/**
	 * Add the profiling value into LogQueue
	 * @param eventId  the event ID of this value
	 * @param threadId the corresponding thread ID 
	 * @param methodId the corresponding method ID
	 * @param value    the profiling value which needs to be recorded 
	 */
	public static synchronized void add(int eventId, int threadId, int methodId, double value) {
		//Check the boundary of LogQueue
		if (entryId++ == logQueueThread.length - 1) {
			growLogQueue();
		}
		logQueueThread[entryId] = threadId;
		logQueueMethod[entryId] = methodId;
		logQueueEvent[entryId][eventId] = value;	
	}

	public static double pop(int eventId, int threadId, int methodId){
		//Epilogue may be sampled before prologue
//		InitStack(EnergyCheckUtils.socketNum);
		if(methodId < 0 || methodId >= hotMethodsProfLog.length)
			VM.sysWriteln("hotMethodsProfLog overflow methodId: " + methodId);
		if (logSize[eventId][threadId][methodId] <= 0){
			VM.sysWriteln("pop fail: no element to pop");
			return -1;
		} else 
//			VM.sysWriteln("pop value: " + hotMethodsProfLog[eventId][threadId][methodId][logSize[methodId] - 1]);
		return hotMethodsProfLog[eventId][threadId][methodId][--logSize[eventId][threadId][methodId]];
	}
	
	public static double peak(int eventId, int threadId, int methodId) {
		return hotMethodsProfLog[eventId][threadId][methodId][logSize[eventId][threadId][methodId] - 1];
	}
	
	public static void dump(int sid) {
	}
}
