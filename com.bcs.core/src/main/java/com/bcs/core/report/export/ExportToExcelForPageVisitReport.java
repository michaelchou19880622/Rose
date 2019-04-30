package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.report.service.PageVisitReportService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForPageVisitReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForPageVisitReport.class);
	@Autowired
	private PageVisitReportService pageVisitReportService;
	
	/**
	 * 匯出 Page Visit Report EXCEL
	 */
	public void exportToExcelForPageVisitReport(String exportPath, String fileName, String startDate, String endDate,  String pageUrl, String pageTitle) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheet= wb.createSheet("Page Visit Report"); // create a new sheet
			this.exportToExcelForPageVisitReport(wb, sheet, startDate, endDate, pageUrl, pageTitle);
			
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	/**
	 * Export To Excel For Page Visit Report
	 * @param wb
	 * @param sheet
	 * @param startDate
	 * @param endDate
	 * @param iMsgId
	 * @param userStatus
	 * @throws Exception
	 */
	public void exportToExcelForPageVisitReport(Workbook wb, Sheet sheet, String startDate, String endDate, String pageUrl, String pageTitle) throws Exception{
			
		//取得匯出資料
		Map<String, Map<String, Long>> result = pageVisitReportService.getPageVisitReport(startDate, endDate, pageUrl);
		
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("日期");
		row.createCell(1).setCellValue("連結說明");
		row.createCell(2).setCellValue("連結");
		row.createCell(3).setCellValue("點擊次數");
		row.createCell(4).setCellValue("點擊人數");
		
		if(result.size() != 0){
			int seqNo = 1; //序號
			for (String responseDate : result.keySet()) {
				Map<String, Long> dataMap = result.get(responseDate);
					
				Long clickCount = dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_COUNT.toString());
				if(clickCount == null){
					clickCount = 0L;
				}
				Long clickUser = dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_DISTINCT_COUNT.toString());
				if(clickUser == null){
					clickUser = 0L;
				}
				
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(responseDate);
				row1.createCell(1).setCellValue(pageTitle);
				row1.createCell(2).setCellValue(pageUrl);
				row1.createCell(3).setCellValue(clickCount);
				row1.createCell(4).setCellValue(clickUser);
				
				seqNo++;
				
//				for (int colNum=0; colNum<row.getLastCellNum(); colNum++) {
//					sheet.autoSizeColumn(colNum);
//				}
			}
		}
	}
}
