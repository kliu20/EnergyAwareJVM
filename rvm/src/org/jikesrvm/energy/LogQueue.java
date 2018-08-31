package org.jikesrvm.energy;

import org.jikesrvm.VM;
import java.util.LinkedList;
import org.jikesrvm.adaptive.controller.Controller;

/**
 * Almost the same as ProfileStack, This class must be used with thread safe way since 
 * its methods are not synchronzed.
 */
public class LogQueue implements ProfilingTypes {
	
	/**Record the log entry in start profiling stage*/
	public static LinkedList<LogEntry> startLogQueue;
	/**Record the log entry in end profiling stage*/
	public static LinkedList<LogEntry> endLogQueue;
	/**Record the post calculated log entry*/
	public static LinkedList<LogEntry> logQueue;

	public static void initRawDataQueue(int socketNum) {
		if (startLogQueue == null) {
			startLogQueue = new LinkedList();
			endLogQueue = new LinkedList();
		}
	}

	public static void initQueue(int socketNum) {
		if (logQueue == null) {
			logQueue = new LinkedList();
		}
	}
	
	/**
	 * Add the profiling value into startLogQueue
	 * @param threadId the corresponding thread ID 
	 * @param methodId the corresponding method ID
	 * @param profileAttrs    the profiling values which needs to be recorded 
	 */
	public static void addStartLogQueue(int threadId, int methodId, Double[] profileAttrs) {
		LogEntry entry = new LogEntry(threadId, methodId, profileAttrs);
		startLogQueue.offer(entry);
	}

	/**
	 * Add the profiling value into endLogQueue
	 * @param threadId the corresponding thread ID 
	 * @param methodId the corresponding method ID
	 * @param profileAttrs    the profiling values which needs to be recorded 
	 */
	public static void addEndLogQueue(int threadId, int methodId, Double[] profileAttrs) {
		LogEntry entry = new LogEntry(threadId, methodId, profileAttrs);
		endLogQueue.offer(entry);
	}

	/**
	 * Add the profiling value into LogQueue
	 * @param threadId the corresponding thread ID 
	 * @param methodId the corresponding method ID
	 * @param profileAttrs    the profiling values which needs to be recorded 
	 */
	public static void addLogQueue(int threadId, int methodId, Double[] profileAttrs) {
		LogEntry entry = new LogEntry(threadId, methodId, profileAttrs);
		logQueue.offer(entry);
	}


	/**
	 * Dump the profiling information with the data has been calculated
	 */
	public static void dumpLogQueue(String[] clsNameList, String[] methodNameList) {
		for (LogEntry entry : logQueue) {
			int timeEntry = entry.counters.length;
			double totalWallClockTime = entry.counters[timeEntry];
			double missRate = entry.counters[0] / entry.counters[1];
			double missRateByTime = entry.counters[0] / totalWallClockTime;

			DataPrinter.printProfInfoTwo(entry.methodId, clsNameList[entry.methodId] + "." + methodNameList[entry.methodId], Controller.options.FREQUENCY_TO_BE_PRINTED, entry.counters, missRate, missRateByTime);   
		}
	}

	/**
	 * Dump the profiling information with the data hasn't been calculated
	 */
	public static void dumpWithRawData(String[] clsNameList, String[] methodNameList) {
		LogEntry startEntry= startLogQueue.poll();
		
		while (startEntry != null) {
			int entryId = 0;
			for (LogEntry endEntry : endLogQueue) {
				entryId++;
				if (startEntry.threadId == endEntry.threadId && 
					startEntry.methodId == endEntry.methodId) {
					int timeEntry = endEntry.counters.length;
					double totalWallClockTime = endEntry.counters[timeEntry] - startEntry.counters[timeEntry];
					double missRate = (endEntry.counters[0] - startEntry.counters[0]) / (endEntry.counters[1] - startEntry.counters[1]);
					double missRateByTime = (endEntry.counters[0] - startEntry.counters[0]) / totalWallClockTime;


					//DataPrinter.printProfInfoTwo(startEntry, clsNameList[startEntry.methodId] + "." + methodNameList[startEntry.methodId], Controller.options.FREQUENCY_TO_BE_PRINTED, TODO: result array, missRate, missRateByTime);   

					endLogQueue.remove(entryId);
					break;
				}
			}
			startEntry = startLogQueue.poll();
		}
	}
}
