package jp.co.flect.papertrail.excel;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import jp.co.flect.papertrail.LogAnalyzer;
import jp.co.flect.papertrail.Counter;
import jp.co.flect.papertrail.CounterItem;
import jp.co.flect.papertrail.CounterRow;

public class ExcelWriter {
	
	private Workbook workbook;
	private Sheet templateSheet;
	
	public ExcelWriter(Workbook workbook, Sheet templateSheet) {
		this.workbook = workbook;
		this.templateSheet = templateSheet;
	}
	
	public void write(LogAnalyzer logAnalyzer, String sheetName) {
		Sheet sheet = createSheet(sheetName);
		int rowIndex = 0;
		while (true) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				break;
			}
			rowIndex++;
		}
		for (Counter counter : logAnalyzer.getCounters()) {
			List<CounterRow> list = counter.getData();
			if (list.size() == 1) {
				write("", list.get(0), sheet.createRow(rowIndex++));
			} else {
				Row row = sheet.createRow(rowIndex++);
				Cell cell = createCell(row, 0);
				cell.setCellValue(counter.getName());
				for (CounterRow items : list) {
					write("    ", items, sheet.createRow(rowIndex++));
				}
			}
		}
	}
	
	private Cell createCell(Row row, int colIndex) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(row.getSheet().getColumnStyle(colIndex));
		return cell;
	}
	
	private void write(String prefix, CounterRow items, Row row) {
		int colIndex = 0;
		Cell cell = createCell(row, colIndex++);
		cell.setCellValue(prefix + items.getName());
		for (int i=0; i<items.getItemCount(); i++) {
			CounterItem item = items.getItem(i);
			long[] nums = item.getNumbers();
			for (int j=0; j<nums.length; j++) {
				cell = createCell(row, colIndex++);
				cell.setCellValue(nums[j]);
			}
		}
		long[] nums = items.getSummaryItem().getNumbers();
		for (int j=0; j<nums.length; j++) {
			cell = createCell(row, colIndex++);
			cell.setCellValue(nums[j]);
		}
	}
	
	private Sheet createSheet(String sheetName) {
		Sheet sheet = null;
		if (templateSheet == null) {
			sheet = workbook.createSheet(sheetName);
		} else {
			int idx = workbook.getSheetIndex(templateSheet);
			sheet = workbook.cloneSheet(idx);
			workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);
			workbook.setSheetOrder(sheetName, idx);
		}
		return sheet;
	}
}
