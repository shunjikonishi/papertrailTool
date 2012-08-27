package jp.co.flect.papertrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class LogCutter {
	
	private Time start;
	private Time end;
	
	public LogCutter(Time start, Time end) {
		this.start = start.truncateMillis();
		if (end != null) {
			this.end = end.truncateMillis();
		}
	}
	
	public boolean match(Event event) {
		String str = event.getReceivedAt();
		if (str == null || str.length() < 19) {
			return false;
		}
		Time t = Time.parse(str.substring(11, 19));
		if (t == null) {
			return false;
		}
		return t.getTime() >= this.start.getTime() && (this.end == null || t.getTime() <= this.end.getTime());
	}
	
	public static void main(String[] args) throws Exception {
		Time start = null;
		Time end = null;
		String filename = null;
		OutputField of = null;
		for (int i=0; i<args.length; i++) {
			String s = args[i];
			if (s.equals("-s") && i+1<args.length) {
				start = Time.parse(args[++i]);
			} else if (s.equals("-e") && i+1<args.length) {
				end = Time.parse(args[++i]);
			} else if (s.equals("-f") && i+1<args.length) {
				of = new OutputField(args[++i]);
			} else if (filename == null) {
				filename = s;
			}
		}
		if (filename == null || start == null) {
			System.out.println("Usage: LogCutter -s STARTTIME [-e ENDTIME] FILENAME");
			return;
		}
		File file = new File(filename);
		LogCutter cutter = new LogCutter(start, end);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		try {
			String line = r.readLine();
			while (line != null) {
				Event event = Event.fromTsv(line);
				if (cutter.match(event)) {
					if (of == null) {
						System.out.println(line);
					} else {
						System.out.println(of.toString(event));
					}
				}
				line = r.readLine();
			}
		} finally {
			r.close();
		}
	}
	
	private static class OutputField {
		
		private boolean id;
		private boolean receivedAt;
		private boolean sourceId;
		private boolean sourceName;
		private boolean sourceIp;
		private boolean facility;
		private boolean severity;
		private boolean program;
		private boolean message;
		
		public OutputField(String str) {
			this.id = str.indexOf('n') != -1;
			this.receivedAt = str.indexOf('r') != -1;
			this.sourceId = str.indexOf('s') != -1;
			this.sourceName = str.indexOf('S') != -1;
			this.sourceIp = str.indexOf('i') != -1;
			this.facility = str.indexOf('f') != -1;
			this.severity = str.indexOf('l') != -1;
			this.program = str.indexOf('p') != -1;
			this.message = str.indexOf('m') != -1;
		}
		
		public String toString(Event event) {
			StringBuilder buf = new StringBuilder();
			if (this.id) buf.append(event.getId()).append("\t");
			if (this.receivedAt) buf.append(event.getReceivedAt()).append("\t");
			if (this.sourceId) buf.append(event.getSourceId()).append("\t");
			if (this.sourceName) buf.append(event.getSourceName()).append("\t");
			if (this.sourceIp) buf.append(event.getSourceIP()).append("\t");
			if (this.facility) buf.append(event.getFacility()).append("\t");
			if (this.severity) buf.append(event.getSeverity()).append("\t");
			if (this.program) buf.append(event.getProgram()).append("\t");
			if (this.message) buf.append(event.getMessage());
			else buf.setLength(buf.length() - 1);
			
			return buf.toString();
		}
	}
}
