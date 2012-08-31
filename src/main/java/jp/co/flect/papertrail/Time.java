package jp.co.flect.papertrail;

public class Time implements Comparable<Time> {
	
	private int hour;
	private int min;
	private int sec;
	private int millis;
	
	public Time(int hour, int min, int sec) {
		this(hour, min, sec, 0);
	}
	
	public Time(int hour, int min, int sec, int millis) {
		this.hour = hour;
		this.min = min;
		this.sec = sec;
		this.millis = millis;
	}
	
	public int getHours() { return this.hour;}
	public int getMinutes() { return this.min;}
	public int getSeconds() { return this.sec;}
	public int getMillSeconds() { return this.millis;}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (this.hour < 10) {
			buf.append("0");
		}
		buf.append(this.hour).append(":");
		if (this.min < 10) {
			buf.append("0");
		}
		buf.append(this.min).append(":");
		if (this.sec < 10) {
			buf.append("0");
		}
		buf.append(this.sec);
		if (this.millis > 0) {
			buf.append(".");
			if (this.millis < 10) {
				buf.append("00");
			} else if (this.millis < 100) {
				buf.append("0");
			} 
			buf.append(millis);
		}
		return buf.toString();
	}
	
	public boolean sameMin(Time t) {
		return t.hour == this.hour &&
			t.min == this.min;
	}
	
	public boolean sameSec(Time t) {
		return t.hour == this.hour &&
			t.min == this.min &&
			t.sec == this.sec;
	}
	
	public int compareTo(Time time) {
		return getTime() - time.getTime();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Time) {
			Time t = (Time)o;
			return t.hour == this.hour &&
				t.min == this.min &&
				t.sec == this.sec &&
				t.millis == this.millis;
		}
		return false;
	}
	
	public int getTime() {
		return this.hour * 60 * 60 * 1000 
			+ this.min * 60 * 1000
			+ this.sec * 1000
			+ this.millis;
	}
	
	public Time truncateMillis() {
		return this.millis == 0 ? this : new Time(this.hour, this.min, this.sec);
	}
	
	public Time truncateSeconds() {
		return this.sec == 0 && this.millis == 0 ? this : new Time(this.hour, this.min, 0);
	}
	
	public Time nextMin() {
		int h = this.hour;
		int m = this.min + 1;
		if (m >= 60) {
			h++;
			m -= 60;
		}
		return new Time(h, m, 0);
	}
	
	public Time nextSec() {
		int h = this.hour;
		int m = this.min;
		int s = this.sec + 1;
		if (s >= 60) {
			m++;
			s -= 60;
		}
		if (m >= 60) {
			h++;
			m -= 60;
		}
		return new Time(h, m, s);
	}
	
	public static Time parse(String str) {
		int h = 0;
		int m = 0;
		int s = 0;
		int ms = 0;
		String[] strs = str.split(":");
		switch (strs.length) {
			case 1:
				h = Integer.parseInt(strs[0]);
				break;
			case 2:
				h = Integer.parseInt(strs[0]);
				m = Integer.parseInt(strs[1]);
				break;
			case 3:
				h = Integer.parseInt(strs[0]);
				m = Integer.parseInt(strs[1]);
				int idx = strs[2].indexOf('.');
				if (idx == -1) {
					idx = strs[2].indexOf(',');
				}
				if (idx == -1) {
					s = Integer.parseInt(strs[2]);
				} else {
					s = Integer.parseInt(strs[2].substring(0, idx));
					ms = Integer.parseInt(strs[2].substring(idx+1));
				}
				break;
		}
		return new Time(h, m, s, ms);
	}
}

