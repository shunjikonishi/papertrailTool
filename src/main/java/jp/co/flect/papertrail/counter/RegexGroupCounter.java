package jp.co.flect.papertrail.counter;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;

public class RegexGroupCounter extends AbstractCounter {
	
	private Pattern pattern;
	private Map<String, Counter> map = new HashMap<String, Counter>();
	
	public RegexGroupCounter(String name, String regex) {
		super(name);
		this.pattern = Pattern.compile(regex);
	}
	
	public Type getType() { return Type.Count;}
	
	public boolean match(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		return m.find();
	}
	
	public void add(Event e) {
		Matcher m = this.pattern.matcher(e.getMessage());
		if (m.find()) {
			String group = m.groupCount() > 0 ? m.group(1) : this.pattern.pattern();
			Counter counter = this.map.get(group);
			if (counter == null) {
				counter = new AllLogCounter(group);
				this.map.put(group, counter);
			}
			counter.add(e);
		}
	}
	
	public List<CounterRow> getData() {
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		List<String> names = new ArrayList<String>(this.map.keySet());
		Collections.sort(names);
		for (String group : names) {
			Counter counter = this.map.get(group);
			ret.addAll(counter.getData());
		}
		return ret;
	}
	
	public String toString(String prefix, String delimita) {
		StringBuilder buf = new StringBuilder();
		buf.append(prefix).append(getName()).append("\n");
		List<String> names = new ArrayList<String>(this.map.keySet());
		Collections.sort(names);
		for (String group : names) {
			Counter counter = this.map.get(group);
			buf.append(prefix).append(counter.toString("    ", delimita)).append("\n");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}
}

