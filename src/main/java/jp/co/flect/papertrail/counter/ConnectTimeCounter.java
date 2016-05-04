package jp.co.flect.papertrail.counter;

import java.math.BigDecimal;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;

public class ConnectTimeCounter extends TimedNumberCounter {
	
	public ConnectTimeCounter(String name) {
		super(name);
	}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public void add(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log != null) {
			NumberCounterItem item = getItem(e.getTime());
			item.add(new BigDecimal(log.getConnect()));
		}
	}
	
}
