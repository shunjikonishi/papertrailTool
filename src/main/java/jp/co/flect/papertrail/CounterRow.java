package jp.co.flect.papertrail;

public class CounterRow {
	
	private String name;
	private CounterItem[] items;
	private CounterItem summaryItem;
	
	public CounterRow(String name, CounterItem[] items, CounterItem summaryItem) {
		this.name = name;
		this.items = items;
		this.summaryItem = summaryItem;
	}
	
	public String getName() { return this.name;}
	public int getItemCount() { return this.items.length;}
	public CounterItem getItem(int idx) { return this.items[idx];}
	
	public CounterItem getSummaryItem() { return this.summaryItem;}
}
