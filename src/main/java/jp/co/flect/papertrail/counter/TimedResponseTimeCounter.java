package jp.co.flect.papertrail.counter;

import java.util.List;
import java.util.ArrayList;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.HerokuAccessLog;
import jp.co.flect.papertrail.ProgramComparator;
import java.math.BigInteger;

public class TimedResponseTimeCounter extends AbstractCounter {
	
	private List<ResponseTimeCounterItem> list = new ArrayList<ResponseTimeCounterItem>();
	
	public TimedResponseTimeCounter(String name) {
		super(name);
		for (int i=0; i<24; i++) {
			list.add(new ResponseTimeCounterItem());
		}
	}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public void add(Event e) {
		if (e.isAccessLog()) {
			ResponseTimeCounterItem item = list.get(e.getTime().getHours());
			item.add(e.getAccessLog());
		}
	}
	
	public String toString(String prefix, String delimita) {
		StringBuilder buf = new StringBuilder();
		buf.append(prefix)
			.append(getName())
			.append(delimita);
		long cnt = 0;
		int max = 0;
		BigInteger sum = BigInteger.ZERO;
		for (ResponseTimeCounterItem item : this.list) {
			buf.append(item.getCount())
				.append(delimita)
				.append(item.getMax())
				.append(delimita)
				.append(item.getAverage())
				.append(delimita);
			cnt += item.getCount();
			if (item.getMax() > max) {
				max = item.getMax();
			}
			sum = sum.add(BigInteger.valueOf(item.getSum()));
		}
		buf.append(cnt)
			.append(delimita)
			.append(max)
			.append(delimita)
			.append(cnt == 0 ? BigInteger.ZERO : sum.divide(BigInteger.valueOf(cnt)));
		return buf.toString();
	}
	
	private static class ResponseTimeCounterItem {
		
		private int count;
		private int max;
		private long sum;
		
		public int getCount() { return this.count;}
		public int getMax() { return this.max;}
		public long getSum() { return this.sum;}
		public int getAverage() { return this.count == 0 ? 0 : (int)(this.sum / this.count);}
		
		public void add(HerokuAccessLog log) {
			this.count++;
			this.sum += log.getService();
			if (this.max < log.getService()) {
				this.max = log.getService();
			}
		}
	}
	
}
