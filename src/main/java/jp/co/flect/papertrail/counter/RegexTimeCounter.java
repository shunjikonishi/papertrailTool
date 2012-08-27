package jp.co.flect.papertrail.counter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jp.co.flect.papertrail.Event;

public class RegexTimeCounter extends TimedResponseTimeCounter {
	
	private Pattern pattern;
	
	public RegexTimeCounter(String name, String regex) {
		super(name);
		this.pattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean match(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		return m.find() && m.groupCount() > 0;
	}
	
	public void add(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		if (m.find() && m.groupCount() > 0) {
			String group = m.group(1);
			try {
				int n = Integer.parseInt(group);
				ResponseTimeCounterItem item = getItem(e.getTime());
				item.add(n);
			} catch (NumberFormatException ex) {
			}
		}
	}
	
}
