package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;

public class SlowConnectCounter extends TimedCounter {
	
	private int threshold;
	
	public SlowConnectCounter(String name, int threshold) {
		super(name);
		this.threshold = threshold;
	}
	
	public boolean match(Event event) {
		HerokuAccessLog log = event.getAccessLog();
		return log != null && log.getConnect() > this.threshold;
	}
}

