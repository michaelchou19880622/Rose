package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.report.service.ContentLinkReportService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForLinkClickReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForLinkClickReport.class);

	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private ContentLinkReportService contentLinkReportService;
	
	/**
	 * 匯出 Link Click Report EXCEL
	 */
	public void exportToExcelForLinkClickReport(String exportPath, String fileName, String startDate, String endDate,  String linkUrl) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetLink = wb.createSheet("Link Click Report"); // create a new sheet
			this.exportToExcelForLinkClickReport(wb, sheetLink, startDate, endDate, linkUrl);
			
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
	 * Export To Excel For Link Click Report
	 * @param wb
	 * @param sheet
	 * @param startDate
	 * @param endDate
	 * @param iMsgId
	 * @param userStatus
	 * @throws Exception
	 */
	public void exportToExcelForLinkClickReport(Workbook wb, Sheet sheet, String startDate, String endDate, String linkUrl) throws Exception{
		
		List<ContentLink> list = contentLinkService.findByLinkUrl(linkUrl);
			
		if(list == null || list.size() == 0){
			throw new Exception("linkUrl Error");
		}
			
		String linkTitle = "";
		for(ContentLink link : list){
			if(StringUtils.isBlank(linkTitle)){
				linkTitle = link.getLinkTitle();
			}
		}
			
		//取得匯出資料
		Map<String, Map<String, Long>> result = contentLinkReportService.getLinkUrlReport(startDate, endDate, linkUrl);
		
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
					
				Long clickCount = dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString());
				if(clickCount == null){
					clickCount = 0L;
				}
				Long clickUser = dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString());
				if(clickUser == null){
					clickUser = 0L;
				}
				
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(responseDate);
				row1.createCell(1).setCellValue(linkTitle);
				row1.createCell(2).setCellValue(linkUrl);
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
