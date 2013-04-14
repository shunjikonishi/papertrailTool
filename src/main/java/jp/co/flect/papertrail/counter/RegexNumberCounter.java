package jp.co.flect.papertrail.counter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jp.co.flect.papertrail.Event;

public class RegexNumberCounter extends TimedNumberCounter {
	
	private Pattern pattern;
	
	public RegexNumberCounter(String name, String regex) {
		super(name);
		this.pattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean match(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		return m.find() && m.groupCount() > 0;
	}
	
	@Override
	public void add(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		if (m.find() && m.groupCount() > 0) {
			String group = m.group(1);
			try {
				int n = Integer.parseInt(group);
				add(e, n);
			} catch (NumberFormatException ex) {
			}
		}
	}
	
}
