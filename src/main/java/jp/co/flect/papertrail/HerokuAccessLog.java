package jp.co.flect.papertrail;

import java.util.Map;
import java.util.HashMap;

public class HerokuAccessLog {
	
	private String at;
	private String method;
	private String path;
	private String query;
	private String host;
	private String fwd;
	private String dyno;
	private int connect;
	private int service;
	private int status;
	private int bytes;
	private String error;
	
	private static int parseInt(String str, boolean ms) {
		if (str == null || str.length() == 0) {
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
		Map<String, String> map = toMap(msg);
		this.at = map.get("at");
		this.method = map.get("method");
		this.path = map.get("path");
		if (this.path != null && this.path.indexOf('?') != -1) {
			int idx = this.path.indexOf('?');
			this.query = this.path.substring(idx + 1);
			this.path = this.path.substring(0, idx);
		}
		this.host = map.get("host");
		this.fwd = map.get("fwd");
		this.dyno = map.get("dyno");
		this.connect = parseInt(map.get("connect"), true);
		this.service = parseInt(map.get("service"), true);
		this.status = parseInt(map.get("status"), false);
		this.bytes = parseInt(map.get("bytes"), false);
	}
	
	private Map<String, String> toMap(String msg) {
		Map<String, String> map = new HashMap<String, String>();
		int idx = 0;
		while (idx < msg.length()) {
			char c = msg.charAt(idx);
			if (c == ' ') {
				idx++;
				continue;
			}
			int eq = msg.indexOf('=', idx);
			if (eq == -1) {
				return map;
			}
			String name = msg.substring(idx, eq);
			idx = eq + 1;
			char vEnd = msg.charAt(idx) == '"' ? '"' : ' ';
			int end = msg.indexOf(vEnd, idx + 1);
			if (end == -1) {
				return map;
			}
			String value = msg.substring(idx, end);
			if (vEnd == '"') {
				value = value.substring(1, value.length() - 1);
			}
			map.put(name, value);
			idx = end + 1;
		}
		return map;
	}
	
	public String getAt() { return this.at;}
	public String getMethod() { return this.method;}
	public String getPath() { return this.path;}
	public String getQuery() { return this.query;}
	public String getHost() { return this.host;}
	public String getFwd() { return this.fwd;}
	public String getDyno() { return this.dyno;}
	public int getConnect() { return this.connect;}
	public int getService() { return this.service;}
	public int getStatus() { return this.status;}
	public int getBytes() { return this.bytes;}
	
	public String getError() { return this.error;}
	public boolean isError() { return this.error != null;}
}
