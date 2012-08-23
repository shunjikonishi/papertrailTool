package jp.co.flect.papertrail.counter;

public class HerokuErrorCounter extends RegexGroupCounter {
	
	public HerokuErrorCounter(String name) {
		super(name, "Error ([A-Z]\\d{2} \\(.*?\\))");
	}
	
}

