package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Counter;

public abstract class AbstractCounter implements Counter {
	
	private String name;
	
	protected AbstractCounter(String name) {
		this.name = name;
	}
	
	public String getName() { return this.name;}
	public String toString() { return toString("", ",");}
	public String toString(String prefix) { return toString(prefix, ",");}
}
