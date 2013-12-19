package jp.co.flect.papertrail.metrics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jp.co.flect.papertrail.Event;

public class LogMetrics {
	
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
	
	public void addTarget(String str) {
		if (!targetList.contains(str)) {
			this.targetList.add(str);
		}
	}
	
	public List<String> getTargetList() { return this.targetList;}
	
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
	
	public void addAllHerokuMetrics() {
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
	
	public Result process(Event event) {
		String msg = event.getMessage();
		Result m = new Result(event.getGeneratedAt(), this.targetList.size());
		for (int i=0; i<this.targetList.size(); i++) {
			String target = this.targetList.get(i) + "=";
			int idx = msg.indexOf(target);
			if (idx != -1) {
				BigDecimal n = parseNum(msg, idx + target.length());
				m.setValue(i, n);
			}
		}
		return m.hasValue() ? m : null;
	}
	
	public class Result {
		
		private String time;
		private BigDecimal[] values;
		
		public Result(String time, int cnt) {
			this.time = time;
			this.values = new BigDecimal[cnt];
		}
		
		public String getTime() { return this.time;}
		
		public int getKeyCount() { return values.length;}
		public String getKey(int idx) { return LogMetrics.this.targetList.get(idx);}
		
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
		
		public String toString() {
			return toString(",");
		}
		
		public String toString(String delimita) {
			StringBuilder buf = new StringBuilder();
			buf.append(time);
			for (BigDecimal n : this.values) {
				buf.append(delimita);
				if (n != null) {
					buf.append(n.toString());
				}
			}
			return buf.toString();
		}
	}
}
