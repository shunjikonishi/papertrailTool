package jp.co.flect.papertrail.counter;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.ProgramComparator;

public class ResponseTimeCounter extends AbstractCounter {
	
	private Map<String, TimedResponseTimeCounter> map = new HashMap<String, TimedResponseTimeCounter>();
	
	public ResponseTimeCounter(String name) {
		super(name);
	}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public void add(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log == null) {
			return;
		}
		String name = log.isError() ? log.getError() : log.getPath();
		TimedResponseTimeCounter counter = this.map.get(name);
		if (counter == null) {
			counter = new TimedResponseTimeCounter(name);
			this.map.put(name, counter);
		}
		counter.add(e);
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

