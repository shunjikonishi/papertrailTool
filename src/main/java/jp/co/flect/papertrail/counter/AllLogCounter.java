package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;

public class AllLogCounter extends TimedCounter {
	
	public AllLogCounter(String name) {
		super(name);
	}
	
	public boolean match(Event event) {
		return true;
	}
}

