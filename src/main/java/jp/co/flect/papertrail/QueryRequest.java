package jp.co.flect.papertrail;

import java.util.Date;

public class QueryRequest {
	
	private String query;
	private long maxid;
	private long minid;
	private boolean tail;
	
	private Date maxDate;
	private Date minDate;
	
	public QueryRequest(String query) {
		this.query = query;
	}
	
	public String getQuery() { return this.query;}
	public void setQuery(String s) { this.query = s;}
	
	public long getMaxId() { return this.maxid;}
	public void setMaxId(long n) { this.maxid = n;}
	
	public long getMinId() { return this.minid;}
	public void setMinId(long n) { this.minid = n;}
	
	public Date getMaxDate() { return this.maxDate;}
	public void setMaxDate(Date d) { this.maxDate = d;}
	
	public Date getMinDate() { return this.minDate;}
	public void setMinDate(Date d) { this.minDate = d;}
	
	public boolean isTail() { return this.tail;}
	public void setTail(boolean b) { this.tail = b;}
}
