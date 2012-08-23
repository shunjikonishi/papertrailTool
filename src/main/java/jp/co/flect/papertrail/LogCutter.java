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
		for (int i=0; i<args.length; i++) {
			String s = args[i];
			if (s.equals("-s") && i+1<args.length) {
				start = Time.parse(args[++i]);
			} else if (s.equals("-e") && i+1<args.length) {
				end = Time.parse(args[++i]);
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
					System.out.println(line);
				}
				line = r.readLine();
			}
		} finally {
			r.close();
		}
	}
	
}
