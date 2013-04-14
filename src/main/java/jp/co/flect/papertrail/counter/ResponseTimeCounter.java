package jp.co.flect.papertrail.counter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.Resource;

public class ResponseTimeCounter extends TimedGroupCounter {
	
	private List<Pattern> patternList = new ArrayList<Pattern>();
	private List<Pattern> excludeList = new ArrayList<Pattern>();
	private boolean includeConnectTime = false;
	
	public ResponseTimeCounter(String name) {
		this(name, Resource.ALL_ACCESS);
	}
	
	public ResponseTimeCounter(String name, String allName) {
		super(name, allName);
	}
	
	public boolean isIncludeConnectTime() { return this.includeConnectTime;}
	public void setIncludeConnectTime(boolean b) { this.includeConnectTime = b;}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public void addPattern(String pattern) {
		this.patternList.add(Pattern.compile(pattern));
	}
	
	public void addExclude(String pattern) {
		this.excludeList.add(Pattern.compile(pattern));
	}
	
	private String normalize(String path) {
		for (Pattern p : patternList) {
			if (p.matcher(path).matches()) {
				return p.pattern();
			}
		}
		return path;
	}
	
	private boolean isExclude(String path) {
		for (Pattern p : excludeList) {
			if (p.matcher(path).matches()) {
				return true;
			}
		}
		return false;
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
		return normalize(path);
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

