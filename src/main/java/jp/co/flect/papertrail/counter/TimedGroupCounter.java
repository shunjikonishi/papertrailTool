package jp.co.flect.papertrail.counter;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.Resource;

public abstract class TimedGroupCounter extends AbstractCounter implements Comparator<String> {
	
	private String allName;
	private String otherName;
	private Map<String, TimedNumberCounter> map = new HashMap<String, TimedNumberCounter>();
	private int maxGroup = 0;
	
	private Map<String, Pattern> patternMap = new LinkedHashMap<String, Pattern>();
	private List<Pattern> excludeList = new ArrayList<Pattern>();
	
	public TimedGroupCounter(String name, String allName) {
		this(name, allName, Resource.OTHER);
	}
	public TimedGroupCounter(String name, String allName, String otherName) {
		super(name);
		this.allName = allName;
		this.otherName = otherName;
		TimedNumberCounter all = new TimedNumberCounter(allName);
		this.map.put(allName, all);
	}
	
	public Type getType() { return Type.Time;}
	
	public int getMaxGroup() { return this.maxGroup;}
	public void setMaxGroup(int n) { this.maxGroup = n;}
	
	protected abstract String getGroupName(Event e);
	protected abstract int getGroupNumber(Event e);
	
	public void addPattern(String name, String pattern) {
		this.patternMap.put(name, Pattern.compile(pattern));
	}
	
	public void addExclude(String pattern) {
		this.excludeList.add(Pattern.compile(pattern));
	}
	
	protected String normalize(String path) {
		for (Map.Entry<String, Pattern> entry : this.patternMap.entrySet()) {
			if (entry.getValue().matcher(path).matches()) {
				return entry.getKey();
			}
		}
		return path;
	}
	
	protected boolean isExclude(String path) {
		for (Pattern p : excludeList) {
			if (p.matcher(path).matches()) {
				return true;
			}
		}
		return false;
	}
	
	public void add(Event e) {
		String name = getGroupName(e);
		int n = getGroupNumber(e);
		if (name == null || n < 0) {
			return;
		}
		TimedNumberCounter counter = this.map.get(name);
		if (counter == null) {
			if (maxGroup > 0) {
				if (map.size() == maxGroup - 1) {
					counter = new TimedNumberCounter(otherName);
					this.map.put(otherName, counter);
				} else if (map.size() == maxGroup) {
					counter = map.get(otherName);
				}
			}
			if (counter == null) {
				counter = new TimedNumberCounter(name);
				this.map.put(name, counter);
			}
		}
		counter.add(e, n);
		this.map.get(this.allName).add(e, n);
	}
	
	public List<CounterRow> getData() {
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		List<String> names = new ArrayList<String>(this.map.keySet());
		Collections.sort(names, this);
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
		Collections.sort(names, this);
		for (String name : names) {
			TimedNumberCounter counter = this.map.get(name);
			buf.append(prefix).append(counter.toString("    ", delimita)).append("\n");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}
	
	public int compare(String s1, String s2) {
		if (this.allName.equals(s1)) {
			return -1;
		} else if (this.allName.equals(s2)) {
			return 1;
		} else if (this.otherName.equals(s1)) {
			return 1;
		} else if (this.otherName.equals(s2)) {
			return -1;
		}
		boolean p1 = patternMap.containsKey(s1);
		boolean p2 = patternMap.containsKey(s2);
		if (p1) {
			return p2 ? s1.compareTo(s2) : -1;
		} else if (p2) {
			return 1;
		}
		return s1.compareTo(s2);
	}
	
}