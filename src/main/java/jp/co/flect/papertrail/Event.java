package jp.co.flect.papertrail;

public class Event {
	
	private long id;
	private String generated_at;
	private String received_at;
	private int source_id;
	private String source_name;
	private String source_ip;
	private String facility;
	private String severity;
	private String program;
	private String message;
//	private String display_received_at;
	
	private HerokuAccessLog accessLog;
	private Time time;
	
	public Event() {}
	
	public Event(String message) {
		this.message = message;
	}
	
	public String getSeverity() { return this.severity;}
	public void setSeverity(String s) { this.severity = s;}
	
	public String getSourceName() { return this.source_name;}
	public void setSourceName(String s) { this.source_name = s;}
	
	public int getSourceId() { return this.source_id;}
	public void setSourceId(int n) { this.source_id = n;}
	
	public String getMessage() { return this.message;}
	public void setMessage(String s) { this.message = s;}
	
	public String getProgram() { return this.program;}
	public void setProgram(String s) { this.program = s;}
	
	public String getSourceIP() { return this.source_ip;}
	public void setSourceIP(String s) { this.source_ip = s;}
	
	public String getGeneratedAt() { return this.generated_at;}
	public void setGeneratedAt(String s) { this.generated_at = s;}
	
	public String getReceivedAt() { return this.received_at;}
	public void setReceivedAt(String s) { this.received_at = s;}
	
	public long getId() { return this.id;}
	public void setId(long n) { this.id = n;}
	
	public String getFacility() { return this.facility;}
	public void setFacility(String s) { this.facility = s;}
	
	public boolean isAccessLog() {
		return getAccessLog() != null;
	}
	
	public HerokuAccessLog getAccessLog() {
		if (this.accessLog != null) {
			return this.accessLog;
		}
		if ("heroku/router".equals(this.program)) {
			try {
				this.accessLog = new HerokuAccessLog(this.message);
			} catch (RuntimeException e) {
				e.printStackTrace();
				return null;
			}
		}
		return this.accessLog;
	}
	
	public Time getTime() {
		if (this.time == null && this.generated_at != null) {
			this.time = Time.parse(this.generated_at.substring(11, 19));
		}
		return this.time;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("GeneratedAt=").append(this.generated_at)
			.append("ReceivedAt=").append(this.received_at)
			.append(", Severity=").append(this.severity)
			.append(", SourceIP=").append(this.source_ip)
			.append(", SourceName=").append(this.source_name)
			.append(", Program=").append(this.program)
			.append(", Facility=").append(this.facility)
			.append(", Message=").append(this.message);
		return buf.toString();
	}
	
	public static Event fromTsv(String str) {
		String[] strs = str.split("\t");
		if (strs.length > 10) {
			StringBuilder buf = new StringBuilder();
			buf.append(strs[9]);
			for (int i=10; i<strs.length; i++) {
				buf.append("\t").append(strs[i]);
			}
			strs[9] = buf.toString();
		}
		Event event = new Event(strs[9]);
		event.setId(Long.parseLong(strs[0]));
		event.setGeneratedAt(strs[1]);
		event.setReceivedAt(strs[2]);
		event.setSourceId(Integer.parseInt(strs[3]));
		event.setSourceName(strs[4]);
		event.setSourceIP(strs[5]);
		event.setFacility(strs[6]);
		event.setSeverity(strs[7]);
		event.setProgram(strs[8]);
		return event;
	}
}
