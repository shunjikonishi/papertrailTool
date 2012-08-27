package jp.co.flect.papertrail;

import java.util.List;

public interface Counter {
	
	public enum Type {
		Count,
		Time
	};
	
	public String getName();
	public Type getType();
	public boolean match(Event e);
	public void add(Event e);
	public String toString(String prefix);
	public String toString(String prefix, String delimita);
	
	public List<CounterRow> getData();
	
}
