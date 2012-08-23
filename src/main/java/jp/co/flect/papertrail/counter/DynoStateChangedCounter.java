package jp.co.flect.papertrail.counter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jp.co.flect.papertrail.Event;

public class DynoStateChangedCounter extends TimedCounter {
	
	private Pattern pattern = Pattern.compile("heroku/.*\\.\\d+");
	
	public DynoStateChangedCounter(String name) {
		super(name);
	}
	
	public boolean match(Event event) {
		String pg = event.getProgram();
		String msg = event.getMessage();
		if (pg == null || msg == null) {
			return false;
		}
		return this.pattern.matcher(pg).matches() && msg.startsWith("State changed ");
	}
}

