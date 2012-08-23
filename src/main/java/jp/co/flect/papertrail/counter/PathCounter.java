package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;

public class PathCounter extends TimedCounter {
	
	private String path;
	private boolean eq;
	
	public PathCounter(String name, String path) {
		this(name, path, false);
	}
	
	public PathCounter(String name, String path, boolean eq) {
		super(name);
		this.path = path;
		this.eq = eq;
	}
	
	public boolean match(Event event) {
		HerokuAccessLog log = event.getAccessLog();
		if (log == null) {
			return false;
		}
		if (eq) {
			return this.path.equals(log.getPath());
		} else {
			return log.getPath().contains(this.path);
		}
	}
}

