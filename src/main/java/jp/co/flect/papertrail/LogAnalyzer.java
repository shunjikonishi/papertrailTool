package jp.co.flect.papertrail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jp.co.flect.papertrail.counter.AccessCounter;
import jp.co.flect.papertrail.counter.AllLogCounter;
import jp.co.flect.papertrail.counter.ClientErrorCounter;
import jp.co.flect.papertrail.counter.DynoStateChangedCounter;
import jp.co.flect.papertrail.counter.HerokuErrorCounter;
import jp.co.flect.papertrail.counter.PathCounter;
import jp.co.flect.papertrail.counter.PatternCounter;
import jp.co.flect.papertrail.counter.ProgramCounter;
import jp.co.flect.papertrail.counter.RegexGroupCounter;
import jp.co.flect.papertrail.counter.ResponseTimeCounter;
import jp.co.flect.papertrail.counter.ServerErrorCounter;
import jp.co.flect.papertrail.counter.SlowRequestCounter;

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
	
	private static Map<String, CounterFactory> factoryMap = new HashMap<String, CounterFactory>();
	
	static {
		factoryMap.put("-al", new CounterFactory("AllLog", 1) {
			protected Counter doCreate(String[] args) {
				return new AllLogCounter(getName(args));
			}
		});
		factoryMap.put("-ac", new CounterFactory("AccessLog", 1) {
			protected Counter doCreate(String[] args) {
				return new AccessCounter(getName(args));
			}
		});
		factoryMap.put("-sl", new CounterFactory("SlogRequest", 2) {
			protected Counter doCreate(String[] args) {
				String name = null;
				int threshold = 0;
				switch (args.length) {
					case 1:
						threshold = Integer.parseInt(args[0]);
						name = getDefaultName() + "(" + threshold + "ms)";
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
		factoryMap.put("-ce", new CounterFactory("ClientError", 1) {
			protected Counter doCreate(String[] args) {
				return new ClientErrorCounter(getName(args));
			}
		});
		factoryMap.put("-se", new CounterFactory("ServerError", 1) {
			protected Counter doCreate(String[] args) {
				return new ServerErrorCounter(getName(args));
			}
		});
		factoryMap.put("-ds", new CounterFactory("DynoState", 1) {
			protected Counter doCreate(String[] args) {
				return new DynoStateChangedCounter(getName(args));
			}
		});
		factoryMap.put("-pg", new CounterFactory("Program", 1) {
			protected Counter doCreate(String[] args) {
				return new ProgramCounter(getName(args));
			}
		});
		factoryMap.put("-he", new CounterFactory("HerokuError", 1) {
			protected Counter doCreate(String[] args) {
				return new HerokuErrorCounter(getName(args));
			}
		});
		factoryMap.put("-rt", new CounterFactory("ResponseTime", 1) {
			protected Counter doCreate(String[] args) {
				return new ResponseTimeCounter(getName(args));
			}
		});
		factoryMap.put("-rg", new CounterFactory("RegexGroup", 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-rg");
				}
				String name = args[0];
				String pattern = args.length == 2 ? args[1] : name;
				return new RegexGroupCounter(name, pattern);
			}
		});
		factoryMap.put("-pt", new CounterFactory("Pattern", 2) {
			protected Counter doCreate(String[] args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("-pt");
				}
				String name = args[0];
				String pattern = args.length == 2 ? args[1] : name;
				return new PatternCounter(name, pattern);
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
		
		public Counter create(String[] args) {
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
		
		public ArgumentParser(String[] args) {
			this.args = args;
		}
		
		public Counter nextCounter() {
			if (idx >= args.length) {
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
		
		public String getFilename() {
			return this.filename;
		}
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
		filename = parser.getFilename();
		if (filename == null) {
			printUsage();
			return;
		}
		File file = new File(filename);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
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
		System.out.println(analyzer);
	}
	
}
