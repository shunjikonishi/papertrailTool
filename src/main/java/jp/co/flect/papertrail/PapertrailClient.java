package jp.co.flect.papertrail;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import org.yaml.snakeyaml.Yaml;

public class PapertrailClient {
	
	private static final String URL = "https://papertrailapp.com/api/v1/";
	private static final String API_HEADER = "X-Papertrail-Token";
	
	private static final int TAIL_INTERVAL = 2000;
	
	private static final String[] DATE_FORMATS = {
		"yyyy-MM-dd'T'HH:mm:ssZZ"
	};
	
	private String apiToken;
	private HttpClient client;
	private boolean keepAlive;
	
	public PapertrailClient(String apiToken) {
		this.apiToken = apiToken;
		client = new DefaultHttpClient();
	}
	
	public boolean isKeepAlive() { return this.keepAlive;}
	public void setKeepAlive(boolean b) { this.keepAlive = b;}
	
	public QueryResult query() throws IOException {
		return query((String)null);
	}
	
	public QueryResult query(String query) throws IOException {
		return query(new QueryRequest(query));
	}
	
	public QueryResult query(QueryRequest request) throws IOException {
		String str = getString(request);
		return QueryResult.fromJson(str);
	}
	
	public String getString(QueryRequest request) throws IOException {
		boolean first = true;
		StringBuilder buf = new StringBuilder();
		buf.append(URL).append("events/search.json");
		if (request.getQuery() != null) {
			buf.append(first ? '?' : '&');
			first = false;
			
			String query = request.getQuery();
			char quote = query.indexOf('\'') == -1 ? '\'' : '"';
			buf.append("q=").append(quote).append(URLEncoder.encode(query)).append(quote);
		}
		if (request.getMinId() > 0) {
			buf.append(first ? '?' : '&');
			first = false;
			buf.append("min_id=").append(request.getMinId());
			if (!request.isTail()) {
				buf.append("&tail=false");
			}
		}
		if (request.getMaxId() > 0) {
			buf.append(first ? '?' : '&');
			first = false;
			buf.append("max_id=").append(request.getMaxId());
		}
		if (request.getMinDate() != null) {
			buf.append(first ? '?' : '&');
			first = false;
			long t = request.getMinDate().getTime() / 1000;
			buf.append("min_time=").append(t);
		}
		if (request.getMaxDate() != null) {
			buf.append(first ? '?' : '&');
			first = false;
			long t = request.getMaxDate().getTime() / 1000;
			buf.append("max_time=").append(t);
		}
		HttpGet method = new HttpGet(buf.toString());
		method.addHeader(API_HEADER, this.apiToken);
		if (this.keepAlive) {
			method.addHeader("Connection", "keep-alive");
		}
		HttpResponse res = this.client.execute(method);
		return EntityUtils.toString(res.getEntity(), "utf-8");
	}
	
	public static void main(String[] args) throws Exception {
		String output = "MPR";
		String query = null;
		String date = null;
		File configFile = null;
		String apiKey = null;
		boolean tail = false;
		long time = 0;
		for (int i=0; i<args.length; i++) {
			String s = args[i];
			if (s.equals("-d") && i+1<args.length) {
				date = normalizeDate(args[++i]);
			} else if (s.equals("-o") && i+1<args.length) {
				output = args[++i];
			} else if (s.equals("-c") && i+1<args.length) {
				configFile = new File(args[++i]);
			} else if (s.equals("-a") && i+1<args.length) {
				apiKey = args[++i];
			} else if (s.equals("-f")) {
				tail = true;
			} else if (s.equals("-t") && i+1<args.length) {
				time = parseTime(args[++i]);
			} else if (query == null) {
				query = s;
			} else {
				query += " " + s;
			}
		}
		if (apiKey == null) {
			if (configFile == null) {
				configFile = new File(".papertrail.yml");
			}
			if (!configFile.exists()) {
				throw new IllegalArgumentException("API Key is not specified");
			}
			apiKey = parseYaml(configFile);
		}
		ResultWriter writer = new ResultWriter(output);
		
		PapertrailClient client = new PapertrailClient(apiKey);
		if (date != null || tail) {
			client.setKeepAlive(true);
		}
		QueryRequest request = new QueryRequest(query);
		if (date != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			Date sd = fmt.parse(date);
			if (time > 0) {
				sd = new Date(sd.getTime() + time);
			}
			request.setMinDate(sd);
		}
		QueryResult result = client.query(request);
		writer.printResult(0, date, result);
		if (date != null) {
			Event lastEvent = result.getLastEvent();
			while (lastEvent != null && lastEvent.getReceivedAt().startsWith(date)) {
				request = new QueryRequest(query);
				request.setMinId(lastEvent.getId());
				request.setTail(false);
				result = client.query(request);
				writer.printResult(lastEvent.getId(), date, result);
				lastEvent = result.getLastEvent();
			}
		} else if (tail) {
			long lastId = result.getLastEvent().getId();
			while (true) {
				Thread.sleep(TAIL_INTERVAL);
				
				request = new QueryRequest(query);
				request.setMinId(lastId);
				result = client.query(request);
				if (result.getEventCount() > 0) {
					writer.printResult(lastId, date, result);
					lastId = result.getLastEvent().getId();
				}
			}
		}
	}
	
	private static long parseTime(String s) {
		try {
			String[] strs = s.split(":");
			long ret = Integer.parseInt(strs[0]) * 1000 * 60 * 60;
			if (strs.length > 1) {
				ret += Integer.parseInt(strs[1]) * 1000 * 60;
			}
			if (strs.length > 2) {
				ret += Integer.parseInt(strs[2]) * 1000;
			}
			return ret;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	private static String parseYaml(File f) throws IOException {
		Yaml yaml = new Yaml();
		FileInputStream is = new FileInputStream(f);
		try {
			Map map = (Map)yaml.load(is);
			return (String)map.get("token");
		} finally {
			is.close();
		}
	}
	
	private static String normalizeDate(String date) {
		Calendar cal = Calendar.getInstance();
		int[] nums = new int[3];
		nums[0] = cal.get(Calendar.YEAR);
		nums[1] = cal.get(Calendar.MONTH) + 1;
		nums[2] = cal.get(Calendar.DATE);
		
		String[] strs = date.split("-");
		int idx = 3 - strs.length;
		if (idx < 0) {
			throw new IllegalArgumentException(date);
		}
		for (int i=0; i<strs.length; i++) {
			nums[idx++] = Integer.parseInt(strs[i]);
		}
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<nums.length; i++) {
			if (buf.length() > 0) {
				buf.append("-");
			}
			int n = nums[i];
			if (n < 10) {
				buf.append("0");
			}
			buf.append(n);
		}
		return buf.toString();
	}
	
	private static class ResultWriter {
		
		private boolean outputSeverity   = false;//Sv
		private boolean outputHostName   = false;//H
		private boolean outputSourceName = false;//Sn
		private boolean outputSourceId   = false;//Si
		private boolean outputMessage    = true; //M
		private boolean outputProgram    = true; //P
		private boolean outputSourceIP   = false;//Sp
		private boolean outputReceivedAt = true; //R
		private boolean outputId         = false;//I
		private boolean outputFacility   = false;//F
		
		public ResultWriter(String str) {
			this.outputSeverity   = str.indexOf("Sv") != -1;
			this.outputHostName   = str.indexOf("H") != -1;
			this.outputSourceName = str.indexOf("Sn") != -1;
			this.outputSourceId   = str.indexOf("Si") != -1;
			this.outputMessage    = str.indexOf("M") != -1;
			this.outputProgram    = str.indexOf("P") != -1;
			this.outputSourceIP   = str.indexOf("Sp") != -1;
			this.outputReceivedAt = str.indexOf("R") != -1;
			this.outputId         = str.indexOf("I") != -1;
			this.outputFacility   = str.indexOf("F") != -1;
		}
		
		public void printResult(long skipId, String outputDate, QueryResult result) {
			for (Event event: result.getEvents()) {
				if (outputDate != null && !event.getReceivedAt().startsWith(outputDate)) {
					continue;
				}
				if (event.getId() > skipId) {
					printResult(event);
				}
			}
		}
		
		private void printResult(Event event) {
			StringBuilder buf = new StringBuilder();
			if (outputReceivedAt) {
				buf.append(event.getReceivedAt());
			}
			if (outputId) {
				buf.append(" Id=").append(event.getId());
			}
			if (outputSeverity) {
				buf.append(" Severity=").append(event.getSeverity());
			}
			if (outputHostName) {
				buf.append(" Host=").append(event.getHostName());
			}
			if (outputSourceName) {
				buf.append(" SourceName=").append(event.getSourceName());
			}
			if (outputSourceId) {
				buf.append(" SourceId=").append(event.getSourceId());
			}
			if (outputSourceIP) {
				buf.append(" SourceIP=").append(event.getSourceIP());
			}
			if (outputProgram) {
				buf.append(" Program=").append(event.getProgram());
			}
			if (outputFacility) {
				buf.append(" Facility=").append(event.getFacility());
			}
			if (outputMessage) {
				buf.append(" ").append(event.getMessage());
			}
			System.out.println(buf.toString());
		}
	}
}
