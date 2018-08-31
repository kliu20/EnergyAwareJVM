package org.jikesrvm.energy;
class LogEntry {
	public int threadId;
	public int methodId;
	public Double[] counters;
	public int[] ratio;

	public LogEntry(int threadId, int methodId, Double[] counters) {
		this.threadId = threadId;
		this.methodId = methodId;
		this.counters = counters;	
	}
}
