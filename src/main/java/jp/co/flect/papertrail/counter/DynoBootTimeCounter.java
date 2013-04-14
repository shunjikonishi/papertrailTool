package jp.co.flect.papertrail.counter;

import java.util.Iterator;
import jp.co.flect.papertrail.Event;

public class DynoBootTimeCounter extends PairNumberCounter {
	
	public DynoBootTimeCounter(String name) {
		super(name);
	}
	
	@Override
	protected boolean isStart(Event e) {
		return e.getMessage().indexOf("to starting") != -1;
	}
	
	@Override
	protected boolean isEnd(Event e) {
		return e.getMessage().indexOf("to up") != -1;
	}
	
	@Override
	public boolean match(Event e) {
		String pg = e.getProgram();
		String msg = e.getMessage();
		if (pg == null || msg == null || 
		    !pg.startsWith("heroku/web.") ||
		    !msg.startsWith("State changed")) 
		{
			return false;
		}
		return super.match(e);
	}
	
	@Override
	protected Event getPairStartEvent(Event e) {
		Iterator<Event> it = this.list.iterator();
		while (it.hasNext()) {
			Event target = it.next();
			if (target.getProgram().equals(e.getProgram())) {
				it.remove();
				return target;
			}
		}
		return null;
	}
	
}
