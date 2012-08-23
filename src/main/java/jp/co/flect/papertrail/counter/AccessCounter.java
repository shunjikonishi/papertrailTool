package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;

public class AccessCounter extends TimedCounter {
	
	public AccessCounter(String name) {
		super(name);
	}
	
	public boolean match(Event event) {
		return "heroku/router".equals(event.getProgram());
	}
}

