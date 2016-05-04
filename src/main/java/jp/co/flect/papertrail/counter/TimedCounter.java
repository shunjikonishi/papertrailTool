package jp.co.flect.papertrail.counter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jp.co.flect.papertrail.Time;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;

public abstract class TimedCounter extends AbstractCounter {
	
	private List<TimedCounterItem> list = new ArrayList<TimedCounterItem>();
	
	private TimedCounterItem currentMin;
	private TimedCounterItem currentSec;
	
	public TimedCounter(String name) {
		super(name);
		for (int i=0; i<24; i++) {
			list.add(new TimedCounterItem(i));
		}
	}
	
	public Type getType() { return Type.Count;}
	
	public List<CounterRow> getData() {
		CounterItem[] items = new CounterItem[this.list.size()];
		long cnt = 0;
		long min = 0;
		long sec = 0;
		for (int i=0; i<this.list.size(); i++) {
			TimedCounterItem item = this.list.get(i);
			items[i] = item;
			cnt += item.getCount();
			if (min < item.maxOfMinute) {
				min = item.maxOfMinute;
			}
			if (sec < item.maxOfSecond) {
				sec = item.maxOfSecond;
			}
		}
		CounterItem summaryItem = new TimedCounterItem(cnt, min, sec);
		
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		ret.add(new CounterRow(getName(), items, summaryItem));
		return ret;
	}
	
	public void add(Event event) {
		Time time = event.getTime();
		TimedCounterItem item = this.list.get(time.getHours());
		item.countUp();
		
		if (this.currentMin == null || !this.currentMin.time.sameMin(time)) {
			this.currentMin = new TimedCounterItem(time);
		}
		if (this.currentSec == null || !this.currentSec.time.sameSec(time)) {
			this.currentSec = new TimedCounterItem(time);
		}
		this.currentMin.countUp();
		this.currentSec.countUp();
		if (item.maxOfMinute < currentMin.getCount()) {
			item.maxOfMinute = currentMin.getCount();
		}
		if (item.maxOfSecond < currentSec.getCount()) {
			item.maxOfSecond = currentSec.getCount();
		}
	}
	
	public String toString(String prefix, String delimita) {
		StringBuilder buf = new StringBuilder();
		CounterRow row = getData().get(0);
		buf.append(prefix)
			.append(getName())
			.append(delimita);
		for (int i=0; i<row.getItemCount(); i++) {
			BigDecimal[] nums = row.getItem(i).getNumbers();
			for (int j=0; j<nums.length; j++) {
				buf.append(nums[j]).append(delimita);
			}
		}
		BigDecimal[] nums = row.getSummaryItem().getNumbers();
		buf.append(nums[0]).append(delimita)
			.append(nums[1]).append(delimita)
			.append(nums[2]);
		return buf.toString();
	}
	
	public static class TimedCounterItem extends CounterItem {
		
		private Time time;
		private long maxOfMinute;
		private long maxOfSecond;
		
		public TimedCounterItem(int hour) {
			this.time = new Time(hour, 0, 0);
		}
		
		public TimedCounterItem(Time time) {
			this.time = time;
		}
		
		public TimedCounterItem(long count, long min, long sec) {
			super(count);
			this.maxOfMinute = min;
			this.maxOfSecond = sec;
		}
		
		public long getMaxOfMinute() { return this.maxOfMinute;}
		public long getMaxOfSecond() { return this.maxOfSecond;}
		
		public BigDecimal[] getNumbers() {
			BigDecimal[] ret = new BigDecimal[3];
			ret[0] = new BigDecimal(getCount());
			ret[1] = new BigDecimal(getMaxOfMinute());
			ret[2] = new BigDecimal(getMaxOfSecond());
			return ret;
		}
	}
	
}

