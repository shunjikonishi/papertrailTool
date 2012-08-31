package jp.co.flect.papertrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import jp.co.flect.papertrail.counter.TimedResponseTimeCounter.ResponseTimeCounterItem;

public class StressAnalyzer {
	
	private Time startTime;
	private Time endTime;
	private String path;
	private boolean bSec;
	private Map<Time, Item> map = new HashMap<Time, Item>();
	private Map<String, Item> errMap = new HashMap<String, Item>();
	private Set<Integer> statusSet = new TreeSet<Integer>();
	
	public StressAnalyzer(Time startTime, Time endTime, String path, boolean bSec) {
		if (startTime.compareTo(endTime) > 0) {
			throw new IllegalArgumentException(startTime + " > " + endTime);
		}
		if (bSec) {
			startTime = startTime.truncateMillis();
			endTime = endTime.truncateMillis();
		} else {
			startTime = startTime.truncateSeconds();
			endTime = endTime.truncateSeconds();
		}
		this.startTime = startTime;
		this.endTime = endTime;
		this.path = path;
		this.bSec = bSec;
		if (bSec) {
			for (Time t = startTime; t.compareTo(endTime) <= 0; t = t.nextSec()) {
				this.map.put(t, new Item());
			}
		} else {
			for (Time t = startTime; t.compareTo(endTime) <= 0; t = t.nextMin()) {
				this.map.put(t, new Item());
			}
		}
	}
	
	private Item getItem(Time time) {
		time = this.bSec ? time.truncateMillis() : time.truncateSeconds();
		return this.map.get(time);
	}
	
	private Item getErrorItem(String key) {
		Item item = this.errMap.get(key);
		if (item == null) {
			item = new Item();
			this.errMap.put(key, item);
		}
		return item;
	}
	
	public boolean add(Event event) {
		HerokuAccessLog log = event.getAccessLog();
		if (log == null) {
			return false;
		}
		if (!this.path.equals(log.getPath())) {
			return false;
		}
		Item item = getItem(event.getTime());
		if (item == null) {
			return false;
		}
		item.add(log);
		if (log.isError()) {
			Item errItem = getErrorItem(log.getError());
			errItem.add(log);
		}
		return true;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Time,Count,Max,Avg");
		for (Integer status : statusSet) {
			buf.append(",").append(status);
		}
		buf.append("\n");
		
		List<Time> list = new ArrayList<Time>(this.map.keySet());
		Collections.sort(list);
		for (Time t : list) {
			Item item = this.map.get(t);
			buf.append(t);
			addNumbers(buf, item);
			buf.append("\n");
		}
		if (this.errMap.size() > 0) {
			buf.append("\n");
			List<String> list2 = new ArrayList<String>(this.errMap.keySet());
			Collections.sort(list2);
			for (String key : list2) {
				Item item = this.errMap.get(key);
				buf.append(key);
				addNumbers(buf, item);
				buf.append("\n");
			}
		}
		return buf.toString();
	}
	
	private void addNumbers(StringBuilder buf, Item item) {
		for (long n : item.getNumbers()) {
			buf.append(",").append(n);
		}
	}
	
	private class Item extends ResponseTimeCounterItem {
		
		private Map<Integer, Integer> statusMap = new HashMap<Integer, Integer>();
		
		@Override
		public void add(HerokuAccessLog log) {
			super.add(log);
			int status = log.getStatus();
			Integer n = statusMap.get(status);
			if (n == null) {
				n = 1;
			} else {
				n = n.intValue() + 1;
			}
			statusMap.put(status, n);
			statusSet.add(status);
		}
		
		@Override
		public long[] getNumbers() {
			long[] ret = new long[3 + statusSet.size()];
			ret[0] = getCount();
			ret[1] = getMax();
			ret[2] = getAverage();
			int idx = 3;
			for (Integer status : statusSet) {
				Integer n = statusMap.get(status);
				ret[idx++] = n == null ? 0 : n.intValue();
			}
			return ret;
		}
	}
	
	private static void printUsage() {
		System.err.println("Usage: java jp.co.flect.papertrail.StressAnalyzer -s HH:mm:ss -e HH:mm:ss -p PATH -f filename [-v]");
	}
	
	public static void main(String[] args) throws Exception {
		Time start = null;
		Time end = null;
		String path = null;
		String filename = null;
		boolean bSec = false;
		for (int i=0; i<args.length; i++) {
			String s = args[i];
			if (s.equals("-s")) {
				if (i+1<args.length) {
					start = Time.parse(args[++i]);
				}
			} else if (s.equals("-e")) {
				if (i+1<args.length) {
					end = Time.parse(args[++i]);
				}
			} else if (s.equals("-p")) {
				if (i+1<args.length) {
					path = args[++i];
				}
			} else if (s.equals("-f")) {
				if (i+1<args.length) {
					filename = args[++i];
				}
			} else if (s.equals("-v")) {
				bSec = true;
			} else {
				throw new IllegalArgumentException(s);
			}
		}
		if (start == null || end == null || path == null || filename == null) {
			printUsage();
			return;
		}
		StressAnalyzer analyzer = new StressAnalyzer(start, end, path, bSec);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "utf-8"));
		try {
			String line = r.readLine();
			while (line != null) {
				Event event = Event.fromTsv(line);
				analyzer.add(event);
				line = r.readLine();
			}
		} finally {
			r.close();
		}
		System.out.println(analyzer);
	}
	
}
