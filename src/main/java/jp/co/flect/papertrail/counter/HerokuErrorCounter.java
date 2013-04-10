package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.Counter;

public class HerokuErrorCounter extends RegexGroupCounter {
	
	public HerokuErrorCounter(String name) {
		super(name, "Error ([A-Z]\\d{2} \\(.*?\\))");
	}
	
	@Override
	public boolean match(Event e) {
		if (e.isAccessLog() && e.getAccessLog().isError()) {
			return true;
		}
		return super.match(e);
	}
	
	@Override
	public void add(Event e) {
		if (e.isAccessLog()) {
			HerokuAccessLog log = e.getAccessLog();
			String name = log.getCode();
			if (log.getError() != null) {
				name += "(" + log.getError() + ")";
			}
			Counter counter = this.map.get(name);
			if (counter == null) {
				counter = new AllLogCounter(name);
				this.map.put(name, counter);
			}
			counter.add(e);
			return;
		}
		super.add(e);
	}
}

