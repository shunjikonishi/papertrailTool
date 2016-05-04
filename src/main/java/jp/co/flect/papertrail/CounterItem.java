package jp.co.flect.papertrail;

import java.math.BigDecimal;

public abstract class CounterItem {
	
	private long count;
	
	public CounterItem() {
		this(0);
	}
	
	public CounterItem(long count) {
		this.count = count;
	}
	
	public long getCount() { return this.count;}
	public void countUp() { this.count++;}
	
	
	public abstract BigDecimal[] getNumbers();
}
