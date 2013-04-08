package jp.co.flect.papertrail.counter;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.ProgramComparator;

public class ResponseTimeCounter extends AbstractCounter {
	
	private Map<String, TimedResponseTimeCounter> map = new HashMap<String, TimedResponseTimeCounter>();
	private List<Pattern> patternList = new ArrayList<Pattern>();
	
	public ResponseTimeCounter(String name) {
		super(name);
	}
	
	public Type getType() { return Type.Time;}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public void addPattern(String pattern) {
		this.patternList.add(Pattern.compile(pattern));
	}
	
	private String normalize(String path) {
		for (Pattern p : patternList) {
			if (p.matcher(path).matches()) {
				return p.pattern();
			}
		}
		return path;
	}
	
	public void add(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log == null) {
			return;
		}
		String name = log.isError() ? log.getError() : log.getPath();
		name = normalize(name);
		TimedResponseTimeCounter counter = this.map.get(name);
		if (counter == null) {
			counter = new TimedResponseTimeCounter(name);
			this.map.put(name, counter);
		}
		counter.add(e);
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
		for (String name : names) {
			TimedResponseTimeCounter counter = this.map.get(name);
			buf.append(prefix).append(counter.toString("    ", delimita)).append("\n");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}
}

