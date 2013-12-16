package jp.co.flect.papertrail.metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jp.co.flect.papertrail.Event;

public class LogMetrics implements Cloneable {
	
	public static final String HEROKU_LOAD_AVG_1M    = "load_avg_1m";
	public static final String HEROKU_LOAD_AVG_5M    = "load_avg_5m";
	public static final String HEROKU_LOAD_AVG_15M   = "load_avg_15m";
	public static final String HEROKU_MEMORY_RSS     = "memory_rss";
	public static final String HEROKU_MEMORY_CACHE   = "memory_cache";
	public static final String HEROKU_MEMORY_SWAP    = "memory_swap";
	public static final String HEROKU_MEMORY_TOTAL  = "memory_total";
	public static final String HEROKU_MEMORY_PGPGOUT = "memory_pgpgout";
	public static final String HEROKU_MEMORY_PGPGIN  = "memory_pgpgin";
	
	private List<String> targetList = new ArrayList<String>();
	private List<Metrics> metricsList = new ArrayList<Metrics>();
	
	public void addTarget(String str) {
		if (!targetList.contains(str)) {
			this.targetList.add(str);
		}
	}
	
	public List<String> getTargetList() { return this.targetList;}
	public List<Metrics> getMetricsList() { return this.metricsList;}
	
	public LogMetrics clone() {
		try {
			LogMetrics ret = (LogMetrics)super.clone();
			ret.targetList = new ArrayList<String>(this.targetList);
			ret.metricsList = new ArrayList<Metrics>();
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void addHerokuCpuMetrics() {
		addTarget(HEROKU_LOAD_AVG_1M);
		addTarget(HEROKU_LOAD_AVG_5M);
		addTarget(HEROKU_LOAD_AVG_15M);
	}
	
	public void addHerokuMemoryMetrics() {
		addTarget(HEROKU_MEMORY_RSS);
		addTarget(HEROKU_MEMORY_CACHE);
		addTarget(HEROKU_MEMORY_SWAP);
		addTarget(HEROKU_MEMORY_TOTAL);
		addTarget(HEROKU_MEMORY_PGPGOUT);
		addTarget(HEROKU_MEMORY_PGPGIN);
	}
	
	public void addAllMetrics() {
		addHerokuCpuMetrics();
		addHerokuMemoryMetrics();
	}
	
	private BigDecimal parseNum(String str, int spos) {
		StringBuilder buf = new StringBuilder();
		for (int i=spos; i<str.length(); i++) {
			char c = str.charAt(i);
			if (c == '.' || (c >= '0' && c <= '9')) {
				buf.append(c);
			} else {
				break;
			}
		}
		return new BigDecimal(buf.toString());
	}
	
	public boolean match(Event event) {
		String msg = event.getMessage();
		for (int i=0; i<this.targetList.size(); i++) {
			String target = this.targetList.get(i) + "=";
			int idx = msg.indexOf(target);
			if (idx != -1) {
				return true;
			}
		}
		return false;
	}
	
	public boolean process(Event event) {
		String msg = event.getMessage();
		Metrics m = new Metrics(event.getGeneratedAt(), this.targetList.size());
		for (int i=0; i<this.targetList.size(); i++) {
			String target = this.targetList.get(i) + "=";
			int idx = msg.indexOf(target);
			if (idx != -1) {
				BigDecimal n = parseNum(msg, idx + target.length());
				m.setValue(i, n);
			}
		}
		boolean ret = m.hasValue();
		if (ret) {
			metricsList.add(m);
		}
		return ret;
	}
	
	public List<Metrics> process(File f) throws IOException {
		return process(new FileInputStream(f));
	}
	
	public List<Metrics>  process(InputStream is) throws IOException {
		this.metricsList.clear();
		
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
		return this.metricsList;
	}
	
	public void printTable(PrintStream out, String caption) {
		out.println("<table border='1'>");
		if (caption != null) {
			out.println("<caption>" + caption + "</caption>");
		}
		out.print("<thead><tr><th>Time</th>");
		for (String target : this.targetList) {
			out.print("<th>" + target + "</th>");
		}
		out.println("</tr></thead><tbody>");
		for (Metrics m : this.metricsList) {
			out.println(m.toTableRow());
		}
		out.println("</tbody></table>");
	}
	
	public static class Metrics {
		
		private String time;
		private BigDecimal[] values;
		
		public Metrics(String time, int cnt) {
			this.time = time;
			this.values = new BigDecimal[cnt];
		}
		
		public String getTime() { return this.time;}
		public BigDecimal getValue(int idx) { return this.values[idx];}
		public void setValue(int idx, BigDecimal n) { this.values[idx] = n;}
		
		public boolean hasValue() {
			for (BigDecimal n : this.values) {
				if (n != null) return true;
			}
			return false;
		}
		
		public String toTableRow() {
			StringBuilder buf = new StringBuilder();
			buf.append("<tr><th>").append(time).append("</th>");
			for (BigDecimal n : this.values) {
				buf.append("<td>").append(n == null ? "&nbsp;" : n.toString()).append("</td>");
			}
			buf.append("</tr>");
			return buf.toString();
		}
		
		public String toCsv() {
			StringBuilder buf = new StringBuilder();
			buf.append(time);
			for (BigDecimal n : this.values) {
				buf.append(",");
				if (n != null) {
					buf.append(n.toString());
				}
			}
			return buf.toString();
		}
		
		public String toString() { return toTableRow();}
	}
}
