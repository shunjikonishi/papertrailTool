package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;

public class ServerErrorCounter extends TimedCounter {
	
	public ServerErrorCounter(String name) {
		super(name);
	}
	
	public boolean match(Event event) {
		HerokuAccessLog log = event.getAccessLog();
		return log != null && (log.isError() || log.getStatus() >= 500);
	}
}

