package org.jikesrvm.energy;
class LogEntry {
	public int threadId;
	public int methodId;
	public int invocationCounter;
	public Double[] counters;
	public int[] ratio;
	public long time;

	public LogEntry(int threadId, int methodId, int invocationCounter, Double[] counters, long time) {
		this.threadId = threadId;
		this.methodId = methodId;
		this.invocationCounter = invocationCounter;
		this.counters = counters;	
		this.time = time;
	}

	public LogEntry(int threadId, int methodId, int invocationCounter, Double[] counters) {
		this.threadId = threadId;
		this.methodId = methodId;
		this.invocationCounter = invocationCounter;
		this.counters = counters;	
		this.time = time;
	}
	public LogEntry(int threadId, int methodId, Double[] counters, long time) {
		this.threadId = threadId;
		this.methodId = methodId;
		this.counters = counters;	
		this.time = time;
	}
}
