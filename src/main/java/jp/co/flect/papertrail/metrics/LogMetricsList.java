package jp.co.flect.papertrail.metrics;

import java.util.ArrayList;

public class LogMetricsList extends ArrayList<LogMetrics.Result> {
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (LogMetrics.Result m : this) {
			buf.append(m.toString()).append("\n");
		}
		return buf.toString();
	}
}
