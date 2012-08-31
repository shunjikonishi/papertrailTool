package jp.co.flect.papertrail;

public class HerokuAccessLog {
	
	private String method;
	private String host;
	private String path;
	private String query;
	private String dyno;
	private int queue;
	private int wait;
	private int service;
	private int status;
	private int bytes;
	private String error;
	
	private static int parseInt(String str, boolean ms) {
		str = str.substring(str.indexOf('=') + 1);
		if (str.length() == 0) {
			return -1;
		}
		if (ms && str.endsWith("ms")) {
			str = str.substring(0, str.length() - 2);
		}
		return Integer.parseInt(str);
	}
	
	public HerokuAccessLog(String msg) {
		parse(msg);
	}
	
	private void parse(String msg) {
		if (msg.startsWith("Error ")) {
			int idx = msg.indexOf("->");
			if (idx != -1) {
				this.error = msg.substring(5, idx).trim();
				msg = msg.substring(idx + 2).trim();
			}
		}
		String[] strs = msg.split(" ");
		this.method = strs[0];
		
		int idx = strs[1].indexOf("/");
		this.host = strs[1].substring(0, idx);
		this.path = strs[1].substring(idx);
		
		idx = this.path.indexOf('?');
		if (idx != -1) {
			this.query = this.path.substring(idx+1);
			this.path = this.path.substring(0, idx);
		}
		
		this.dyno = strs[2].substring(strs[2].indexOf('=')+1);
		this.queue = parseInt(strs[3], false);
		this.wait = parseInt(strs[4], true);
		this.service = parseInt(strs[5], true);
		this.status = parseInt(strs[6], false);
		this.bytes = parseInt(strs[7], false);
	}
	
	public String getMethods() { return this.method;}
	public String getHost() { return this.host;}
	public String getPath() { return this.path;}
	public String getQuery() { return this.query;}
	public String getDyno() { return this.dyno;}
	public int getQueue() { return this.queue;}
	public int getWait() { return this.wait;}
	public int getService() { return this.service;}
	public int getStatus() { return this.status;}
	public int getBytes() { return this.bytes;}
	
	public String getError() { return this.error;}
	public boolean isError() { return this.error != null;}
}
