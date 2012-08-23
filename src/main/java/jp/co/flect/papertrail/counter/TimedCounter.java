package jp.co.flect.papertrail.counter;

import java.util.ArrayList;
import java.util.List;
import jp.co.flect.papertrail.Time;
import jp.co.flect.papertrail.Event;

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
	
	public void add(Event event) {
		Time time = event.getTime();
		TimedCounterItem item = this.list.get(time.getHours());
		item.count++;
		
		if (this.currentMin == null || !this.currentMin.time.sameMin(time)) {
			this.currentMin = new TimedCounterItem(time);
		}
		if (this.currentSec == null || !this.currentSec.time.sameSec(time)) {
			this.currentSec = new TimedCounterItem(time);
		}
		this.currentMin.count++;
		this.currentSec.count++;
		if (item.maxOfMinute < currentMin.count) {
			item.maxOfMinute = currentMin.count;
		}
		if (item.maxOfSecond < currentSec.count) {
			item.maxOfSecond = currentSec.count;
		}
	}
	
	public String toString(String prefix, String delimita) {
		StringBuilder buf = new StringBuilder();
		buf.append(prefix)
			.append(getName())
			.append(delimita);
		int cnt = 0;
		int min = 0;
		int sec = 0;
		for (TimedCounterItem item : this.list) {
			buf.append(item.count)
				.append(delimita)
				.append(item.maxOfMinute)
				.append(delimita)
				.append(item.maxOfSecond)
				.append(delimita);
			cnt += item.count;
			if (min < item.maxOfMinute) {
				min = item.maxOfMinute;
			}
			if (sec < item.maxOfSecond) {
				sec = item.maxOfSecond;
			}
		}
		buf.append(cnt)
			.append(delimita)
			.append(min)
			.append(delimita)
			.append(sec);
		return buf.toString();
	}
	
	private static class TimedCounterItem {
		
		public TimedCounterItem(int hour) {
			this.time = new Time(hour, 0, 0);
		}
		
		public TimedCounterItem(Time time) {
			this.time = time;
		}
		
		public Time time;
		public int count;
		public int maxOfMinute;
		public int maxOfSecond;
	}
	
}

