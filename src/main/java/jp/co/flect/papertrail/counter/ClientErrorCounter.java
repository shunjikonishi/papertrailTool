package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;

public class ClientErrorCounter extends TimedCounter {
	
	public ClientErrorCounter(String name) {
		super(name);
	}
	
	public boolean match(Event event) {
		HerokuAccessLog log = event.getAccessLog();
		return log != null && log.getStatus() >= 400 && log.getStatus() < 500;
	}
}

