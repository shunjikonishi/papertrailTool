package jp.co.flect.papertrail.counter;

import java.util.ArrayList;
import java.util.List;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.Resource;

public class ResponseTimeCounter extends TimedGroupCounter {
	
	private boolean includeConnectTime = false;
	
	public ResponseTimeCounter(String name) {
		this(name, Resource.ALL_ACCESS);
	}
	
	public ResponseTimeCounter(String name, String allName) {
		super(name, allName);
	}
	
	public ResponseTimeCounter(String name, String allName, String otherName) {
		super(name, allName, otherName);
	}
	
	public boolean isIncludeConnectTime() { return this.includeConnectTime;}
	public void setIncludeConnectTime(boolean b) { this.includeConnectTime = b;}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	@Override
	protected String getGroupName(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log == null) {
			return null;
		}
		String path = log.getPath();
		if (isExclude(path)) {
			return null;
		}
		return "[" + log.getMethod() + "] " + normalize(path);
	}
	
	@Override
	protected int getGroupNumber(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log == null) {
			return -1;
		}
		int n = log.getService();
		if (this.includeConnectTime) {
			n += log.getConnect();
		}
		if (n < 0) {
			n = 0;
		}
		return n;
	}
	
}

