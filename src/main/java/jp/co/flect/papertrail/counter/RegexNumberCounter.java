package jp.co.flect.papertrail.counter;

import java.math.BigDecimal;
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
		m.matches();
		boolean bFind = m.find();
		int groupCount = m.groupCount();
		if (bFind && groupCount > 0) {
			String group = m.group(1);
			try {
				BigDecimal n = new BigDecimal(group);
				add(e, n);
			} catch (NumberFormatException ex) {
			}
		}
	}
	
}
