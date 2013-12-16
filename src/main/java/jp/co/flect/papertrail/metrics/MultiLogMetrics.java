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

public class MultiLogMetrics {
	
	private LogMetrics base = new LogMetrics();
	private Map<String, LogMetrics> map = new HashMap<String, LogMetrics>();
	
	public LogMetrics getBaseMetrics() { return this.base;}
	public Map<String, LogMetrics> getResultMetrics() { return this.map;}
	
	public void addTarget(String s) { this.base.addTarget(s);}
	
	protected String getName(Event event) {
		String name = event.getProgram();
		int idx = name.indexOf('/');
		if (idx != -1) {
			name = name.substring(idx+1);
		}
		return name;
	}
	
	public void process(Event event) {
		if (!base.match(event)) {
			return;
		}
		String msg = event.getMessage();
		String name = getName(event);
		LogMetrics lm = map.get(name);
		if (lm == null) {
			lm = this.base.clone();
			map.put(name, lm);
		}
		lm.process(event);
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
	
	public static void main(String[] args) throws Exception {
		MultiLogMetrics metrics = new MultiLogMetrics();
		metrics.getBaseMetrics().addHerokuMemoryMetrics();
		metrics.getBaseMetrics().addTarget("MaxMemory");
		metrics.getBaseMetrics().addTarget("TotalMemory");
		metrics.getBaseMetrics().addTarget("FreeMemory");
		
		File file = new File(args[0]);
		metrics.process(file);
		for (Map.Entry<String, LogMetrics> entry : metrics.getResultMetrics().entrySet()) {
			String name = entry.getKey();
			LogMetrics m = entry.getValue();
			m.printTable(System.out, name);
		}
	}
	
}
