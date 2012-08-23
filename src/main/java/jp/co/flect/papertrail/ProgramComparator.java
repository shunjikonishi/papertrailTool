package jp.co.flect.papertrail;

import java.util.Comparator;

public class ProgramComparator implements Comparator<String> {
	
	public int compare(String s1, String s2) {
		int idx1 = s1.lastIndexOf(".");
		int idx2 = s2.lastIndexOf(".");
		if (idx1 == -1 || idx2 == -1) {
			return s1.compareTo(s2);
		}
		String prefix1 = s1.substring(0, idx1);
		String prefix2 = s2.substring(0, idx2);
		String suffix1 = s1.substring(idx1 + 1);
		String suffix2 = s2.substring(idx2 + 1);
		if (prefix1.equals(prefix2)) {
			try  {
				return Integer.parseInt(suffix1) - Integer.parseInt(suffix2);
			} catch (NumberFormatException e) {
			}
		}
		return s1.compareTo(s2);
	}
}

