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
import java.math.BigDecimal;

public class TimedNumberCounter extends AbstractCounter {
	
	private List<NumberCounterItem> list = new ArrayList<NumberCounterItem>();
	
	public TimedNumberCounter(String name) {
		super(name);
		for (int i=0; i<24; i++) {
			list.add(new NumberCounterItem());
		}
	}
	
	public Type getType() { return Type.Time;}
	
	public boolean match(Event e) {
		throw new UnsupportedOperationException("Override this method");
	}
	
	public void add(Event e) {
		throw new UnsupportedOperationException("Override this method");
	}
	
	public void add(Event e, BigDecimal num) {
		NumberCounterItem item = getItem(e.getTime());
		item.add(num);
	}
	
	public NumberCounterItem getItem(Time time) {
		return this.list.get(time.getHours());
	}
	
	public List<CounterRow> getData() {
		CounterItem[] items = new CounterItem[this.list.size()];
		BigDecimal cnt = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		for (int i=0; i<this.list.size(); i++) {
			NumberCounterItem item = this.list.get(i);
			items[i] = item;
			cnt = cnt.add(new BigDecimal(item.getCount()));
			if (max.compareTo(item.getMax()) < 0) {
				max = item.getMax();
			}
			sum = sum.add(item.getSum());
		}
		CounterItem summaryItem = new NumberCounterItem(cnt.longValue(), max, sum);
		ArrayList<CounterRow> ret = new ArrayList<CounterRow>();
		ret.add(new CounterRow(getName(), items, summaryItem));
		return ret;
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
	
	public static class NumberCounterItem extends CounterItem {
		
		private BigDecimal max = BigDecimal.ZERO;
		private BigDecimal sum = BigDecimal.ZERO;
		
		public NumberCounterItem() {
			super();
		}

		public NumberCounterItem(long count, BigDecimal max, BigDecimal sum) {
			super(count);
			this.max = max;
			this.sum = sum;
		}

		public BigDecimal getMax() { return this.max;}
		public BigDecimal getSum() { return this.sum;}
		public BigDecimal getAverage() { 
			return getCount() == 0 ? BigDecimal.ZERO : this.sum.divide(new BigDecimal(getCount()), BigDecimal.ROUND_HALF_UP);
		}
		
		public void add(BigDecimal num) {
			countUp();
			this.sum = this.sum.add(num);
			if (this.max.compareTo(num) < 0) {
				this.max = num;
			}
		}
		
		public BigDecimal[] getNumbers() {
			BigDecimal[] ret = new BigDecimal[3];
			ret[0] = new BigDecimal(getCount());
			ret[1] = getMax();
			ret[2] = getAverage();
			return ret;
		}
	}
	
}
