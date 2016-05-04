package jp.co.flect.papertrail.counter;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.ProgramComparator;

public class ProgramCounter extends AbstractCounter {
	
	private Map<String, Counter> map = new HashMap<String, Counter>();
	private Map<String, Pattern> patternMap = new LinkedHashMap<String, Pattern>();
	
	public ProgramCounter(String name) {
		super(name);
	}
	
	public Type getType() { return Type.Count;}
	
	public boolean match(Event e) {
		return true;
	}
	
	public void addPattern(String name, String pattern) {
		this.patternMap.put(name, Pattern.compile(pattern));
	}

	private String normalize(String name) {
		for (Map.Entry<String, Pattern> entry : this.patternMap.entrySet()) {
			if (entry.getValue().matcher(name).matches()) {
				return entry.getKey();
			}
		}
		return name;
	}

	public void add(Event e) {
		String pg = normalize(e.getProgram());
		Counter counter = this.map.get(pg);
		if (counter == null) {
			counter = new AllLogCounter(pg);
			this.map.put(pg, counter);
		}
		counter.add(e);
	}
	
	public List<CounterRow> getData() {
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		List<String> names = new ArrayList<String>(this.map.keySet());
		Collections.sort(names, new ProgramComparator());
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
		Collections.sort(names, new ProgramComparator());
		for (String pg : names) {
			Counter counter = this.map.get(pg);
			buf.append(prefix).append(counter.toString("    ", delimita)).append("\n");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}
}

