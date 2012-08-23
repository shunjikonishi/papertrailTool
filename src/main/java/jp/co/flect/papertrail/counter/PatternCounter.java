package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Event;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PatternCounter extends TimedCounter {
	
	private Pattern pattern;
	
	public PatternCounter(String name, String regex) {
		super(name);
		this.pattern = Pattern.compile(regex);
	}
	
	public boolean match(Event event) {
		Matcher m = this.pattern.matcher(event.getMessage());
		return m.find();
	}
}

