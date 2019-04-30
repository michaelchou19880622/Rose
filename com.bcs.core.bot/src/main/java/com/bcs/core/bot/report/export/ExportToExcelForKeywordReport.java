package com.bcs.core.bot.report.export;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.report.service.InteractiveReportService;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForKeywordReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForKeywordReport.class);

	@Autowired
	private InteractiveReportService interactiveReportService;
	@Autowired
	private MsgInteractiveMainService msgInteractiveMainService;
	
	/**
	 * 匯出 Keyword Report EXCEL
	 */
	public void exportToExcelForKeywordReport(String exportPath, String fileName, String startDate, String endDate,  Long iMsgId, String userStatus) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetLink = wb.createSheet("Keyword Report"); // create a new sheet
			this.exportToExcelForKeywordReport(wb, sheetLink, startDate, endDate, iMsgId, userStatus);
			
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
	 * Export To Excel For Keyword Report
	 * @param wb
	 * @param sheet
	 * @param startDate
	 * @param endDate
	 * @param iMsgId
	 * @param userStatus
	 * @throws Exception
	 */
	public void exportToExcelForKeywordReport(Workbook wb, Sheet sheet, String startDate, String endDate, Long iMsgId, String userStatus) throws Exception{
		
		MsgInteractiveMain main = msgInteractiveMainService.findOne(iMsgId);
		
		if(main == null){
			throw new Exception("iMsgId Error");
		}
		
		//取得匯出資料
		Map<String, Map<String, BigInteger>> map = interactiveReportService.getInteractiveReport(startDate, endDate, iMsgId, userStatus);
		
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("回覆日期");
		row.createCell(1).setCellValue("關鍵字");
		row.createCell(2).setCellValue("一般使用者回應次數");
		row.createCell(3).setCellValue("一般使用者回應人數");
		if (userStatus.equals("ALL")) { //全部
			row.createCell(4).setCellValue("升級使用者回應次數");
			row.createCell(5).setCellValue("升級使用者回應人數");
		} else if (userStatus.equals(LineUser.STATUS_BINDED)) { //綁定(升級)
			row.createCell(2).setCellValue("升級使用者回應次數");
			row.createCell(3).setCellValue("升級使用者回應人數");
		}
		
		if(map.size() != 0){
			int seqNo = 1; //序號
			for (String responseDate : map.keySet()) {
				Map<String, BigInteger> dataMap = map.get(responseDate);
				String responseKeyword = main.getMainKeyword();
				BigInteger responseCountForUnbind = dataMap.get(LineUser.STATUS_UNBIND + InteractiveReportService.COUNT_POSTFIX);
				if(responseCountForUnbind == null){
					responseCountForUnbind = BigInteger.ZERO;
				}
				BigInteger responsePersonsForUnbind = dataMap.get(LineUser.STATUS_UNBIND + InteractiveReportService.COUNT_DISTINCT_POSTFIX);
				if(responsePersonsForUnbind == null){
					responsePersonsForUnbind = BigInteger.ZERO;
				}
				BigInteger responseCountForBinded = dataMap.get(LineUser.STATUS_BINDED + InteractiveReportService.COUNT_POSTFIX);
				if(responseCountForBinded == null){
					responseCountForBinded = BigInteger.ZERO;
				}
				BigInteger responsePersonsForBinded = dataMap.get(LineUser.STATUS_BINDED + InteractiveReportService.COUNT_DISTINCT_POSTFIX);
				if(responsePersonsForBinded == null){
					responsePersonsForBinded = BigInteger.ZERO;
				}
				
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(responseDate);
				row1.createCell(1).setCellValue(responseKeyword);
				row1.createCell(2).setCellValue(responseCountForUnbind.longValue());
				row1.createCell(3).setCellValue(responsePersonsForUnbind.longValue());
				if (userStatus.equals("ALL")) { //全部
					row1.createCell(4).setCellValue(responseCountForBinded.longValue());
					row1.createCell(5).setCellValue(responsePersonsForBinded.longValue());
				} else if (userStatus.equals(LineUser.STATUS_BINDED)) {
					row1.createCell(2).setCellValue(responseCountForBinded.longValue());
					row1.createCell(3).setCellValue(responsePersonsForBinded.longValue());
				}
				
				seqNo++;
			}
			
//			for (int colNum=0; colNum<row.getLastCellNum(); colNum++) {
//				sheet.autoSizeColumn(colNum);
//			}
		}
	}
}
