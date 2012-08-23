package jp.co.flect.papertrail;

import java.util.List;

public interface Counter {
	public String getName();
	public boolean match(Event e);
	public void add(Event e);
//	public List<String> getData();
	public String toString(String prefix);
	public String toString(String prefix, String delimita);
	
}
