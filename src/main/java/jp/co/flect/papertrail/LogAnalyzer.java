package jp.co.flect.papertrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.text.MessageFormat;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import jp.co.flect.papertrail.counter.AccessCounter;
import jp.co.flect.papertrail.counter.AllLogCounter;
import jp.co.flect.papertrail.counter.ClientErrorCounter;
import jp.co.flect.papertrail.counter.DynoStateChangedCounter;
import jp.co.flect.papertrail.counter.HerokuErrorCounter;
import jp.co.flect.papertrail.counter.PathCounter;
import jp.co.flect.papertrail.counter.PatternCounter;
import jp.co.flect.papertrail.counter.ProgramCounter;
import jp.co.flect.papertrail.counter.RegexGroupCounter;
import jp.co.flect.papertrail.counter.RegexTimeCounter;
import jp.co.flect.papertrail.counter.ResponseTimeCounter;
import jp.co.flect.papertrail.counter.ServerErrorCounter;
import jp.co.flect.papertrail.counter.SlowRequestCounter;
import jp.co.flect.papertrail.excel.ExcelWriter;

public class LogAnalyzer {
	
	private List<Counter> list = new ArrayList<Counter>();
	
	public int getCounterCount() {
		return this.list.size();
	}
	
	public void add(Counter counter) {
		this.list.add(counter);
	}
	
	public void add(Event event) {
		for (Counter c : this.list) {
			if (c.match(event)) {
				c.add(event);
			}
		}
	}
	
	public List<Counter> getCounters() { return this.list;}
	
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimita) {
		StringBuilder buf = new StringBuilder();
		for (Counter c : this.list) {
			buf.append(c.toString("", delimita)).append("\n");
		}
		return buf.toString();
	}
	
	public void saveToFile(File file, String sheetName) throws IOException, InvalidFormatException {
		if (!file.exists()) {
			copyTemplate(file);
		}
		Workbook workbook = null;
		FileInputStream is = new FileInputStream(file);
		try {
			workbook = WorkbookFactory.create(is);
		} finally {
			is.close();
		}
		ExcelWriter writer = new ExcelWriter(workbook, workbook.getSheet("Template"));
		writer.write(this, sheetName);
		
		FileOutputStream os = new FileOutputStream(file);
		try {
			workbook.write(os);
		} finally {
			os.close();
		}
	}
	
	private void copyTemplate(File file) throws IOException {
		InputStream is = LogAnalyzer.class.getClassLoader().getResourceAsStream("jp/co/flect/papertrail/excel/LogAnalyzer.xlsx");
		try {
			FileOutputStream os = new FileOutputStream(file);
			try {
				byte[] buf = new byte[4096];
				int n = is.read(buf);
				while (n > 0) {
					os.write(buf, 0, n);
					n = is.read(buf);
				}
			} finally {
				os.close();
			}
		} finally {
			is.close();
		}
	}
	
	private static Map<String, CounterFactory> factoryMap = new HashMap<String, CounterFactory>();
	
	static {
		factoryMap.put("-al", new CounterFactory(Resource.ALL_LOG, 1) {
			protected Counter doCreate(String[] args) {
				return new AllLogCounter(getName(args));
			}
		});
		factoryMap.put("-ac", new CounterFactory(Resource.ACCESS_LOG, 1) {
			protected Counter doCreate(String[] args) {
				return new AccessCounter(getName(args));
			}
		});
		factoryMap.put("-sl", new CounterFactory(Resource.SLOW_REQUEST, 2) {
			protected Counter doCreate(String[] args) {
				String name = null;
				int threshold = 0;
				switch (args.length) {
					case 1:
						threshold = Integer.parseInt(args[0]);
						name = MessageFormat.format(getDefaultName(), threshold);
						break;
					case 2:
						name = args[0];
						threshold = Integer.parseInt(args[1]);
						break;
					default:
						throw new IllegalStateException();
				}
				return new SlowRequestCounter(name, threshold);
			}
		});
		factoryMap.put("-rp", new CounterFactory(null, 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-rp");
				}
				String name = args[0];
				String path = args.length == 2 ? args[1] : name;
				return new PathCounter(name, path);
			}
		});
		factoryMap.put("-ce", new CounterFactory(Resource.CLIENT_ERROR, 1) {
			protected Counter doCreate(String[] args) {
				return new ClientErrorCounter(getName(args));
			}
		});
		factoryMap.put("-se", new CounterFactory(Resource.SERVER_ERROR, 1) {
			protected Counter doCreate(String[] args) {
				return new ServerErrorCounter(getName(args));
			}
		});
		factoryMap.put("-ds", new CounterFactory(Resource.DYNO_STATE, 1) {
			protected Counter doCreate(String[] args) {
				return new DynoStateChangedCounter(getName(args));
			}
		});
		factoryMap.put("-pg", new CounterFactory(Resource.PROGRAM, 1) {
			protected Counter doCreate(String[] args) {
				return new ProgramCounter(getName(args));
			}
		});
		factoryMap.put("-he", new CounterFactory(Resource.HEROKU_ERROR, 1) {
			protected Counter doCreate(String[] args) {
				return new HerokuErrorCounter(getName(args));
			}
		});
		factoryMap.put("-rt", new CounterFactory(Resource.RESPONSE_TIME, 1) {
			protected Counter doCreate(String[] args) {
				return new ResponseTimeCounter(getName(args));
			}
		});
		factoryMap.put("-rg", new CounterFactory(Resource.REGEX_GROUP, 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-rg");
				}
				String name = args[0];
				String pattern = args.length == 2 ? args[1] : name;
				return new RegexGroupCounter(name, pattern);
			}
		});
		factoryMap.put("-pt", new CounterFactory(Resource.PATTERN, 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-pt");
				}
				String name = args[0];
				String pattern = args.length == 2 ? args[1] : name;
				return new PatternCounter(name, pattern);
			}
		});
		factoryMap.put("-rn", new CounterFactory(Resource.REGEX_NUMBER, 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-rn");
				}
				String name = args[0];
				String pattern = args.length == 2 ? args[1] : name;
				return new RegexTimeCounter(name, pattern);
			}
		});
	}
	
	private static abstract class CounterFactory {
		
		private String defaultName;
		private int maxArgCount;
		
		protected CounterFactory(String defaultName, int maxArgCount) {
			this.defaultName = defaultName;
			this.maxArgCount = maxArgCount;
		}
		
		protected String getDefaultName() { return this.defaultName;}
		
		protected String getName(String[] args) {
			return args.length > 0 ? args[0] : this.defaultName;
		}
		
		public Counter create(String... args) {
			if (args.length > this.maxArgCount) {
				throw new IllegalArgumentException(args[this.maxArgCount]);
			}
			return doCreate(args);
		}
		
		protected abstract Counter doCreate(String[] args);
	}
	
	private static class ArgumentParser {
		
		private String[] args;
		private int idx = 0;
		private String filename;
		private S3Archive s3;
		private String s3dateStr;
		private LinkedList<Counter> allCounters;
		private File excelFile;
		
		public ArgumentParser(String[] args) {
			this.args = args;
		}
		
		public Counter nextCounter() {
			if (idx >= args.length) {
				if (allCounters != null && allCounters.size() > 0) {
					return allCounters.removeFirst();
				}
				return null;
			}
			String s = args[idx++];
			if (s.equals("-f")) {
				if (idx < args.length) {
					this.filename = args[idx++];
				} else {
					throw new IllegalArgumentException("Filename is not specified.");
				}
				return nextCounter();
			} else if (s.equals("-s3")) {
				if (idx + 3 < args.length) {
					this.s3 = new S3Archive(args[idx++], args[idx++], args[idx++]);
					this.s3dateStr = S3Archive.getDateStr(args[idx++]);
				} else {
					throw new IllegalArgumentException("S3 infomation is not specified.");
				}
				return nextCounter();
			} else if (s.equals("-xlsx")) {
				if (idx < args.length) {
					this.excelFile = new File(args[idx++]);
				} else {
					throw new IllegalArgumentException("Excel file is not specified.");
				}
				return nextCounter();
			} else if (s.equals("-*")) {
				this.allCounters = new LinkedList<Counter>();
				this.allCounters.add(factoryMap.get("-al").create());
				this.allCounters.add(factoryMap.get("-ac").create());
				this.allCounters.add(factoryMap.get("-sl").create("1000"));
				this.allCounters.add(factoryMap.get("-ce").create());
				this.allCounters.add(factoryMap.get("-se").create());
				this.allCounters.add(factoryMap.get("-ds").create());
				this.allCounters.add(factoryMap.get("-pg").create());
				this.allCounters.add(factoryMap.get("-he").create());
				this.allCounters.add(factoryMap.get("-rt").create());
				return nextCounter();
			} else if (s.startsWith("-")) {
				CounterFactory factory = factoryMap.get(s);
				if (factory == null) {
					throw new IllegalArgumentException(s);
				}
				List<String> list = new ArrayList<String>();
				for (int i=idx; i<args.length; i++) {
					String s2 = args[idx];
					if (s2.startsWith("-")) {
						break;
					}
					list.add(s2);
					idx++;
				}
				return factory.create(list.toArray(new String[list.size()]));
			} else {
				throw new IllegalArgumentException(s);
			}
		}
		
		public InputStream getInputStream() throws IOException {
			if (this.s3 != null && this.s3dateStr != null) {
				File file = File.createTempFile("tmp", ".log");
				file.deleteOnExit();
				s3.saveToFile(s3dateStr, true, file);
				return new FileInputStream(file);
			} else if (this.filename != null) {
				InputStream is = new FileInputStream(new File(filename));
				if (filename.endsWith(".gz")) {
					try {
						return is = new GZIPInputStream(is);
					} catch (IOException e) {
						is.close();
						throw e;
					}
				}
				return is;
			}
			return null;
		}
		
		public String getSheetName() {
			String name = null;
			if (this.s3 != null && this.s3dateStr != null) {
				name = this.s3dateStr;
			} else if (this.filename != null) {
				name = this.filename;
				int idx = name.indexOf(".");
				if (idx != -1) {
					name = name.substring(0, idx);
				}
			}
			if (name != null) {
				//Remove year
				String[] strs = name.split("-");
				if (strs.length == 3) {
					name = strs[1] + "-" + strs[2];
				}
			}
			return name;
		}
		
		public File getExcelFile() { return this.excelFile;}
	}
	
	private static void printUsage() {
		System.err.println("Usage: java jp.co.flect.papertrail.LogAnalyzer [OPTIONS] -f filename");
	}
	
	public static void main(String[] args) throws Exception {
		String filename = null;
		LogAnalyzer analyzer = new LogAnalyzer();
		ArgumentParser parser = new ArgumentParser(args);
		for (Counter c = parser.nextCounter(); c != null; c = parser.nextCounter()) {
			analyzer.add(c);
		}
		InputStream is = parser.getInputStream();
		if (is == null) {
			printUsage();
			return;
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(is, "utf-8"));
		try {
			String line = r.readLine();
			while (line != null) {
				Event event = Event.fromTsv(line);
				analyzer.add(event);
				line = r.readLine();
			}
		} finally {
			r.close();
		}
		File file = parser.getExcelFile();
		if (file == null) {
			System.out.println(analyzer);
		} else {
			analyzer.saveToFile(file, parser.getSheetName());
		}
	}
	
}
