package jp.co.flect.papertrail.metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import jp.co.flect.papertrail.Event;

public class LogMetricsCollector {
	
	private LogMetrics metrics = new LogMetrics();
	private Map<String, LogMetricsList> map = new HashMap<String, LogMetricsList>();
	
	public LogMetrics getMetrics() { return this.metrics;}
	public Map<String, LogMetricsList> getResult() { return this.map;}
	
	public void addTarget(String s) { this.metrics.addTarget(s);}
	
	protected String getName(Event event) {
		String name = event.getProgram();
		int idx = name.indexOf('/');
		if (idx != -1) {
			name = name.substring(idx+1);
		}
		return name;
	}
	
	public void process(Event event) {
		LogMetrics.Result ret = metrics.process(event);
		if (ret != null) {
			String name = getName(event);
			LogMetricsList list = map.get(name);
			if (list == null) {
				list = new LogMetricsList();
				map.put(name, list);
			}
			list.add(ret);
		}
	}
	
	public void process(File f) throws IOException {
		process(new FileInputStream(f));
	}
	
	public void process(InputStream is) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(is, "utf-8"));
		try {
			String line = r.readLine();
			while (line != null) {
				Event event = Event.fromTsv(line);
				process(event);
				line = r.readLine();
			}
		} finally {
			r.close();
		}
	}
	
	private static void printUsage() {
		System.err.println("Usage: java jp.co.flect.papertrail.LogMetrics [OPTIONS] filename");
	}
	
}
