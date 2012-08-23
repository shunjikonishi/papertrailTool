package jp.co.flect.papertrail;

import com.google.gson.Gson;

public class QueryResult {
	
	private long min_id;
	private long max_id;
	private boolean reached_record_limit;
	private Event[] events;
	
	public long getMinId() { return this.min_id;}
	public long getMaxId() { return this.max_id;}
	public boolean isReachedRecordLimit() { return this.reached_record_limit;}
	
	public Event[] getEvents() { return this.events;}
	
	public int getEventCount() { return this.events != null ? this.events.length : 0;}
	
	public Event getEvent(int idx) { 
		if (this.events == null || idx >= getEventCount() || idx < 0) {
			return null;
		} 
		return this.events[idx];   
	}
	
	public Event getFirstEvent() { 
		return this.events == null || this.events.length == 0 ? null : this.events[0];
	}
	
	public Event getLastEvent() {
		return this.events == null || this.events.length == 0 ? null : this.events[this.events.length-1];
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (Event e : events) {
			buf.append(e).append("\n");
		}
		buf.append("Min=").append(min_id)
			.append(", Max=").append(max_id)
			.append(", Size=").append(events.length)
			.append(", ReachLimit=").append(reached_record_limit);
		return buf.toString();
	}
	
	public static QueryResult fromJson(String str) {
		return new Gson().fromJson(str, QueryResult.class);
	}
}
