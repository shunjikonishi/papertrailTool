package jp.co.flect.papertrail.counter;

import java.util.List;
import java.util.ArrayList;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.Time;
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
	
	public Type getType() { return Type.Time;}
	
	public boolean match(Event e) {
		return e.isAccessLog();
	}
	
	public ResponseTimeCounterItem getItem(Time time) {
		return this.list.get(time.getHours());
	}
	
	public List<CounterRow> getData() {
		CounterItem[] items = new CounterItem[this.list.size()];
		long cnt = 0;
		int max = 0;
		BigInteger sum = BigInteger.ZERO;
		for (int i=0; i<this.list.size(); i++) {
			ResponseTimeCounterItem item = this.list.get(i);
			items[i] = item;
			cnt += item.getCount();
			if (item.getMax() > max) {
				max = item.getMax();
			}
			sum = sum.add(BigInteger.valueOf(item.getSum()));
		}
		final long finalMax = max;
		final long finalAvg = cnt == 0 ? 0 : sum.divide(BigInteger.valueOf(cnt)).longValue();
		CounterItem summaryItem = new CounterItem(cnt) {
			public long[] getNumbers() {
				long[] ret = new long[3];
				ret[0] = getCount();
				ret[1] = finalMax;
				ret[2] = finalAvg;
				return ret;
			}
		};
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		ret.add(new CounterRow(getName(), items, summaryItem));
		return ret;
	}
	
	public void add(Event e) {
		HerokuAccessLog log = e.getAccessLog();
		if (log != null) {
			ResponseTimeCounterItem item = list.get(e.getTime().getHours());
			item.add(log);
		}
	}
	
	public String toString(String prefix, String delimita) {
		StringBuilder buf = new StringBuilder();
		CounterRow row = getData().get(0);
		buf.append(prefix)
			.append(getName())
			.append(delimita);
		for (int i=0; i<row.getItemCount(); i++) {
			long[] nums = row.getItem(i).getNumbers();
			for (int j=0; j<nums.length; j++) {
				buf.append(nums[j]).append(delimita);
			}
		}
		long[] nums = row.getSummaryItem().getNumbers();
		buf.append(nums[0]).append(delimita)
			.append(nums[1]).append(delimita)
			.append(nums[2]);
		return buf.toString();
	}
	
	public static class ResponseTimeCounterItem extends CounterItem {
		
		private int max;
		private long sum;
		
		public int getMax() { return this.max;}
		public long getSum() { return this.sum;}
		public int getAverage() { return getCount() == 0 ? 0 : (int)(this.sum / getCount());}
		
		public void add(HerokuAccessLog log) {
			add(log.getService());
		}
		
		public void add(int time) {
			countUp();
			this.sum += time;
			if (this.max < time) {
				this.max = time;
			}
		}
		
		public long[] getNumbers() {
			long[] ret = new long[3];
			ret[0] = getCount();
			ret[1] = getMax();
			ret[2] = getAverage();
			return ret;
		}
	}
	
}
