package jp.co.flect.papertrail.counter;

import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;
import jp.co.flect.papertrail.Event;
import jp.co.flect.papertrail.Resource;

import java.text.ParseException;

public class PostgresDurationCounter extends TimedGroupCounter {
	
	private boolean includeCopy = true;
	private int targetDuration = 0;
	
	public PostgresDurationCounter(String name) {
		this(name, Resource.ALL_DURATION);
	}
	
	public PostgresDurationCounter(String name, String allName) {
		super(name, allName);
	}
	
	public boolean isIncludeCopy() { return this.includeCopy;}
	public void setIncludeCopy(boolean b) { this.includeCopy = b;}
	
	public int getTargetDuration() { return this.targetDuration;}
	public void setTargetDuration(int n) { this.targetDuration = n;}
	
	public boolean match(Event e) {
		boolean ret = e.getProgram().startsWith("app/postgres") &&
			e.getMessage().indexOf("duration:") != -1;
		if (ret && !this.includeCopy) {
			ret = e.getMessage().indexOf("COPY ") != -1;
		}
		return ret;
	}
	
	private String normalize(String sql) {
		StringBuilder buf = new StringBuilder();
		StringBuilder ret = new StringBuilder();
		Tokenizer t = new Tokenizer(sql);
		
		boolean space = false;
		int n = t.next(buf);
		while (n != Tokenizer.T_END && n != Tokenizer.T_ERROR) {
			if (space && 
			    n != Tokenizer.T_COMMA && 
			    n != Tokenizer.T_OPEN_BRACKET &&
			    n != Tokenizer.T_CLOSE_BRACKET) 
			{
				ret.append(" ");
			}
			space = n != Tokenizer.T_OPEN_BRACKET;
			if (n == Tokenizer.T_STRING ||
			    n == Tokenizer.T_NUMBER ||
			    n == Tokenizer.T_BOOLEAN) 
			{
				ret.append("?");
			} else {
				ret.append(buf);
			}
			n = t.next(buf);
		}
		return ret.toString();
	}
	
	@Override
	protected String getGroupName(Event e) {
		String msg = e.getMessage();
		int idx1 = msg.indexOf("duration:");
		int idx2 = msg.indexOf(":", idx1 + 9);
		if (idx1 == -1 || idx2 ==-1) {
			return null;
		}
		String sql = msg.substring(idx2 + 1).trim();
		return normalize(sql);
	}
	
	@Override
	protected int getGroupNumber(Event e) {
		String msg = e.getMessage();
		int idx1 = msg.indexOf("duration:");
		int idx2 = msg.indexOf(" ms", idx1);
		if (idx1 == -1 || idx2 ==-1) {
			return -1;
		}
		String strNum = msg.substring(idx1 + 9, idx2).trim();
		try {
			int n = new Double(strNum).intValue();
			if (this.targetDuration > 0 && n < this.targetDuration) {
				return -1;
			}
			return n;
		} catch (NumberFormatException ex) {
			System.err.println(strNum);
			ex.printStackTrace();
			return -1;
		}
	}
	
	private static class Tokenizer {
		
		public static final int T_END           = 0;
		public static final int T_COMMA         = 1;
		public static final int T_OPEN_BRACKET  = 2;
		public static final int T_CLOSE_BRACKET = 3;
		public static final int T_LITERAL       = 4;
		public static final int T_STRING        = 5;
		public static final int T_NUMBER        = 6;
		public static final int T_BOOLEAN       = 7;
		public static final int T_ERROR         = 100;
		
		public static final int ILLEGAL_ESCAPE  = 1001;
		public static final int INVALID_LITERAL = 1002;
		public static final int UNCLOSED_STRING = 1003;
		
		public static String type2str(int n) {
			switch (n) {
				case T_END:           return "END";
				case T_COMMA:         return "COMMA";
				case T_OPEN_BRACKET:  return "OPEN_BRACKET";
				case T_CLOSE_BRACKET: return "CLOSE_BRACKET";
				case T_LITERAL:       return "LITERAL";
				case T_STRING:        return "STRING";
				case T_NUMBER:        return "NUMBER";
				case T_BOOLEAN:       return "BOOLEAN";
				case T_ERROR:         return "ERROR";
			}
			throw new IllegalStateException(Integer.toString(n));
		}
		
		private String str;
		private int index;
		private int error;
		
		private int prevIndex;
		
		public Tokenizer(String str) {
			this(str, 0);
		}
		
		public Tokenizer(String str, int index) {
			this.str = str;
			this.index = index;
		}
		
		public String getString() { return this.str;}
		public int getIndex() { return this.index;}
		public void setIndex(int n) { this.index = n;}
		
		public int getError() { return this.error;}
		
		public int getPrevIndex() { return this.prevIndex;}
		
		public String nextLiteral(StringBuilder buf) throws ParseException {
			int n = next(buf);
			if (n != T_LITERAL) {
				throw new ParseException(this.str, prevIndex);
			}
			return buf.toString();
		}
		
		public int next(StringBuilder buf) {
			prevIndex = skipWhitespace();
			int len = str.length();
			buf.setLength(0);
			if (index >= len) {
				return T_END;
			}
			
			char c = str.charAt(index++);
			boolean mayBool = false;
			switch (c) {
				case '-':
					buf.append(c);
					skipWhitespace();
					if (index == len) {
						error = INVALID_LITERAL;
						return T_ERROR;
					}
					return number(buf);
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					buf.append(c);
					return number(buf);
				case ',':
					buf.append(c);
					return T_COMMA;
				case '(':
					buf.append(c);
					return T_OPEN_BRACKET;
				case ')':
					buf.append(c);
					return T_CLOSE_BRACKET;
				case ';':
					buf.append(c);
					return T_END;
				case '=':
					buf.append(c);
					return T_LITERAL;
				case '!':
					buf.append(c);
					if (index < len) {
						char c2 = str.charAt(index++);
						if (c2 == '=') {
							buf.append(c2);
							return T_LITERAL;
						}
					}
					error = INVALID_LITERAL;
					return T_ERROR;
				case '<':
				case '>':
					buf.append(c);
					if (index < len) {
						char c2 = str.charAt(index+1);
						if (c2 == '=') {
							buf.append(c2);
							index++;
							return T_LITERAL;
						}
					}
					return T_LITERAL;
				case '\'':
					while (index < len) {
						char c2 = str.charAt(index++);
						switch (c2) {
							case '\'':
								return T_STRING;
							case '\\':
								if (index == len) {
									error = ILLEGAL_ESCAPE;
									return T_ERROR;
								}
								char c3 = str.charAt(index++);
								switch (c3) {
									case 'n':
									case 'N':
										buf.append('\n');
										break;
									case 'r':
									case 'R':
										buf.append('\r');
										break;
									case 't':
									case 'T':
										buf.append('\t');
										break;
									case 'b':
									case 'B':
										buf.append('\u0007');//Bell 
										break;
									case 'f':
									case 'F':
										buf.append('\u000c');//Form Feed
										break;
									case '\'':
										buf.append('\'');
										break;
									case '\"':
										buf.append('"');
										break;
									case '\\':
										buf.append('\\');
										break;
									default:
										buf.append('\\');
										buf.append(c3);
										break;
								}
								break;
							default:
								buf.append(c2);
								break;
						}
					}
					error = UNCLOSED_STRING;
					return T_ERROR;
				case 'F':
				case 'T':
				case 'f':
				case 't':
					mayBool = true;
					break;
				default:
					break;
			}
			buf.append(c);
			boolean bEnd = false;
			while (index < len && !bEnd) {
				c = str.charAt(index++);
				switch (c) {
					case ',':
					case '(':
					case ')':
					case ';':
					case '=':
					case '!':
					case '<':
					case '>':
						index--;
						bEnd = true;
						break;
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						bEnd = true;
						break;
					default:
						buf.append(c);
						break;
				}
			}
			if (mayBool) {
				String ret = buf.toString().toLowerCase();
				if (ret.equals("true") || ret.equals("false")) {
					return T_BOOLEAN;
				}
			}
			return T_LITERAL;
		}
		
		public int skipWhitespace() {
			if (index >= str.length()) {
				return index;
			}
			char c = str.charAt(index);
			while (isSpace(c)) {
				index++;
				if (index == str.length()) {
					break;
				}
				c = str.charAt(index);
			}
			return index;
		}
		
		//ToDo parse date
		private int number(StringBuilder buf) {
			int len = str.length();
			int ret = T_NUMBER;
			boolean dot = false;
			char c = 0;
			while (index < len) {
				c = str.charAt(index++);
				switch (c) {
					case ',':
					case '(':
					case ')':
					case ';':
					case '=':
					case '!':
					case '>':
					case '<':
						index--;
						return ret;
					case '.':
						if (dot) {
							ret = T_LITERAL;
						} else {
							dot = true;
						}
						break;
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						break;
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						return ret;
					default:
						ret = T_LITERAL;
						break;
				}
				buf.append(c);
			}
			if (c == '.') {
				error = INVALID_LITERAL;
				return T_ERROR;
			}
			return ret;
		}
		
		private static boolean isSpace(char c) {
			return c == ' ' || c == '\t' || c == '\r' || c =='\n';
		}
		
		public static String escapeQuotedString(String s) {
			StringBuilder buf = new StringBuilder();
			int len = s.length();
			for (int i=0; i<len; i++) {
				char c = s.charAt(i);
				switch (c) {
					case '\n':
						buf.append("\\n");
						break;
					case '\r':
						buf.append("\\r");
						break;
					case '\t':
						buf.append("\\t");
						break;
					case '\u0007'://Bell 
						buf.append("\\b");
						break;
					case '\u000c'://Form Feed
						buf.append("\\f");
						break;
					case '\'':
						buf.append("\\'");
						break;
					case '\"':
						buf.append("\\\"");
						break;
					case '\\':
						buf.append("\\\\");
						break;
					default:
						buf.append(c);
						break;
				}
			}
			return buf.toString();
		}
	}

}

