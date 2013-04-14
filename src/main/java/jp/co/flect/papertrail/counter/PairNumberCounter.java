package jp.co.flect.papertrail.counter;

import java.util.LinkedList;
import jp.co.flect.papertrail.Event;

public abstract class PairNumberCounter extends TimedNumberCounter {
	
	protected LinkedList<Event> list = new LinkedList<Event>();
	
	public PairNumberCounter(String name) {
		super(name);
	}
	
	protected abstract boolean isStart(Event e);
	protected abstract boolean isEnd(Event e);
	
	protected Event getPairStartEvent(Event e) {
		return list.size() > 0 ? list.removeFirst() : null;
	}
	
	@Override
	public boolean match(Event e) {
		return isStart(e) || isEnd(e);
	}
	
	@Override
	public void add(Event e) {
		if (isStart(e)) {
			this.list.add(e);
		} else {
			Event start = getPairStartEvent(e);
			if (start == null) {
				return;
			}
			int n = e.getTime().getTime() - start.getTime().getTime();
			add(e, n);
		}
	}
	
}
