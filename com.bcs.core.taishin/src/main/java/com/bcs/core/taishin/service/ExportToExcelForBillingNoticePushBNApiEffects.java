package com.bcs.core.taishin.service;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.taishin.circle.service.BillingNoticeContentTemplateMsgService;

@Service
public class ExportToExcelForBillingNoticePushBNApiEffects {
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForBillingNoticePushBNApiEffects.class);

	@Autowired
	private BillingNoticeContentTemplateMsgService billingNoticeContentTemplateMsgService;

	public void exportExcel(String exportPath, String fileName, String startDate, String endDate) {
		try {
			Workbook workbook = new XSSFWorkbook();
	
			Sheet sheet = workbook.createSheet("成效報表");
				
			this.getBNPushApiEffects(workbook, sheet, startDate, endDate);
			
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}

	private void getBNPushApiEffects(Workbook workbook, Sheet sheet, String startDate, String endDate) {
		Row row = sheet.createRow(0);
		
		row.createCell(0).setCellValue("產品名稱");
		row.createCell(1).setCellValue("發送時間");
		row.createCell(2).setCellValue("發送類型");
		row.createCell(3).setCellValue("發送成功數");
		row.createCell(4).setCellValue("發送失敗數");
		
		Map<String, List<String>> bnEffects = billingNoticeContentTemplateMsgService.getBNEffects(startDate, endDate);
		
		Integer rowNumber = 1;
		for(Object key : bnEffects.keySet()) {
			List<String> list = bnEffects.get(key);
			logger.info("list2:" + list.toString());
			
			row = sheet.createRow(rowNumber);
			
			row.createCell(0).setCellValue(list.get(0));
			
			// Time Cell Style
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
			
			Cell createTimeCell = row.createCell(1);
			createTimeCell.setCellValue(list.get(1));
			createTimeCell.setCellStyle(cellStyle);
			
			row.createCell(2).setCellValue(list.get(2));
			row.createCell(3).setCellValue(list.get(3));
			row.createCell(4).setCellValue(list.get(4));
			rowNumber += 1;
		}
		
		/* 自動調整欄寬 */
		for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
			sheet.autoSizeColumn(col_index);
		}
		sheet.setColumnWidth(0, 50*256);
		sheet.setColumnWidth(2, 15*256);
		sheet.setColumnWidth(3, 15*256);
		sheet.setColumnWidth(4, 15*256);
	}
	
	public void exportExcel(String exportPath, String fileName, String date, String title, String sendType) {
		try {
			Workbook workbook = new XSSFWorkbook();
	
			Sheet sheet = workbook.createSheet("成效明細報表");
				
			this.getBNPushApiEffects(workbook, sheet, date, title, sendType);
			
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void getBNPushApiEffects(Workbook workbook, Sheet sheet, String date, String title, String sendType) {
		Row row = sheet.createRow(0);
		
		row.createCell(0).setCellValue("產品名稱");
		row.createCell(1).setCellValue("建立時間");
		row.createCell(2).setCellValue("修改時間");
		row.createCell(3).setCellValue("發送時間");
		row.createCell(4).setCellValue("發送類型");
		row.createCell(5).setCellValue("UID");
		
		Map<String, List<String>> bnEffects = billingNoticeContentTemplateMsgService.getBNEffectsDetail(date, title, sendType);
		
		Integer rowNumber = 1;
		for(Object key : bnEffects.keySet()) {
			List<String> list = bnEffects.get(key);
			logger.info("list2:" + list.toString());
			
			row = sheet.createRow(rowNumber);
			
			row.createCell(0).setCellValue(list.get(0));
			
			
			
			// Time Cell Style
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("YYYY-MM-DD HH:mm:ss"));
			
			Cell createTimeCell = null;
			
			for(int i = 1; i <= 3; i++) {
				createTimeCell = row.createCell(i);
				createTimeCell.setCellValue(list.get(i).replaceFirst("\\.\\d*(Z)?$", "$1"));
				createTimeCell.setCellStyle(cellStyle);
			}
			
			row.createCell(4).setCellValue(list.get(4));
			row.createCell(5).setCellValue(list.get(5));
			
			rowNumber += 1;
		}
		
		/* 自動調整欄寬 */
		for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
			sheet.autoSizeColumn(col_index);
		}
		sheet.setColumnWidth(0, 40*256);
		sheet.setColumnWidth(4, 13*256);
	}
	
//	public static void main(String[] args) {
//		String time = "2019-03-08 15:24:20.0070000";
//		time = time.replaceFirst("\\.\\d*(Z)?$", "$1"); // 刪去毫秒
//		System.out.println(time);
//	}
}
