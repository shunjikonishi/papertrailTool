package jp.co.flect.papertrail;

import java.util.Map;
import java.util.HashMap;

public class HerokuAccessLog {
	
	private Map<String, String> map;
	private int connect;
	private int service;
	private int status;
	private int bytes;
	private boolean error;
	
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
		this.map = toMap(msg);
		String path = map.get("path");
		if (path != null) {
			int idx = path.indexOf('?');
			if (idx != -1) {
				String query = path.substring(idx + 1);
				path = path.substring(0, idx);
				map.put("path", path);
				map.put("query", query);
			}
			
		}
		this.connect = parseInt(map.get("connect"), true);
		this.service = parseInt(map.get("service"), true);
		this.status = parseInt(map.get("status"), false);
		this.bytes = parseInt(map.get("bytes"), false);
		
		this.error = "error".equals(map.get("at"));
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
	
	public String getAt() { return map.get("at");}
	public String getMethod() { return map.get("method");}
	public String getPath() { return map.get("path");}
	public String getQuery() { return map.get("query");}
	public String getHost() { return map.get("host");}
	public String getFwd() { return map.get("fwd");}
	public String getDyno() { return map.get("dyno");}
	public int getConnect() { return this.connect;}
	public int getService() { return this.service;}
	public int getStatus() { return this.status;}
	public int getBytes() { return this.bytes;}
	
	public String getCode() { return map.get("code");}
	public String getError() { return map.get("desc");}
	public boolean isError() { return this.error;}
}
