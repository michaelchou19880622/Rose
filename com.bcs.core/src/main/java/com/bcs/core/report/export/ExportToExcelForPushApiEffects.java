package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.db.service.PushMessageRecordService;
import com.bcs.core.utils.ErrorRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExportToExcelForPushApiEffects {
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForPushApiEffects.class);
	
	@Autowired
	private PushMessageRecordService pushMessageRecordService;
	
	public void exportExcel(String exportPath, String fileName, String createTime) {
		try {
//			Workbook workbook = new XSSFWorkbook();
			SXSSFWorkbook workbook = new SXSSFWorkbook();
					
			SXSSFSheet sheet = workbook.createSheet("明細報表");
				
			this.getPushApiEffectDetail(workbook, sheet, createTime);

			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			workbook.write(out);
			out.close();
			workbook.dispose();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void exportExcel(String exportPath, String fileName, String startDate, String endDate) {
		try {
//			Workbook workbook = new XSSFWorkbook();
			SXSSFWorkbook workbook = new SXSSFWorkbook();
	
			SXSSFSheet sheet = workbook.createSheet("成效報表");
				
			this.getPushApiEffects(workbook, sheet, startDate, endDate);
			
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			workbook.write(out);
			out.close();
			workbook.dispose();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void getPushApiEffects(SXSSFWorkbook workbook, SXSSFSheet sheet, String startDateString, String endDateString) {
		Row row = sheet.createRow(0);
		
		row.createCell(0).setCellValue("發送時間");
		row.createCell(1).setCellValue("發送類型");
		row.createCell(2).setCellValue("部門名稱");
		row.createCell(3).setCellValue("服務名稱");
		row.createCell(4).setCellValue("推播主題");
		row.createCell(5).setCellValue("發送成功數");
		row.createCell(6).setCellValue("發送失敗數");
		row.createCell(7).setCellValue("發送數量");
				
        long start = System.currentTimeMillis();	        

		List<Map<String, String>> pushEffects = pushMessageRecordService.getPushMessageEffects(startDateString, endDateString);
	        
		long sumSuccess = 0;
		long sumFail = 0 ;
		Integer rowNumber = 1;

		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/mm/dd hh:mm:ss"));		
		
		for(Map<String, String> pushEffect : pushEffects) {
			row = sheet.createRow(rowNumber);
			
			Cell createTimeCell = row.createCell(0);	
			createTimeCell.setCellValue(pushEffect.get("createTime"));
			createTimeCell.setCellStyle(cellStyle);			
			row.createCell(1).setCellValue(pushEffect.get("sendType"));
			row.createCell(2).setCellValue(pushEffect.get("department"));
			row.createCell(3).setCellValue(pushEffect.get("serviceName"));
			row.createCell(4).setCellValue(pushEffect.get("pushTheme"));
			row.createCell(5).setCellValue(pushEffect.get("successCount"));
			row.createCell(6).setCellValue(pushEffect.get("failCount"));
			long total =  Long.parseLong(pushEffect.get("failCount").toString()) +  Long.parseLong(pushEffect.get("successCount").toString());
			row.createCell(7).setCellValue(String.valueOf(total));
	        
			sumSuccess += Long.parseLong(pushEffect.get("successCount").toString());
			sumFail += Long.parseLong(pushEffect.get("failCount").toString());
			
			rowNumber += 1;
		}
		
        long end = System.currentTimeMillis();	        
        log.info("End GetPushApiEffects () for creating Rows and Cells . Time duration : " + (end - start) + " milliseconds.");
        
		Row sumRow = sheet.createRow(rowNumber);
		sumRow.createCell(0).setCellValue("總計");
		sumRow.createCell(1).setCellValue("");
		sumRow.createCell(2).setCellValue("");
		sumRow.createCell(3).setCellValue("");
		sumRow.createCell(4).setCellValue("");
		sumRow.createCell(5).setCellValue(String.valueOf(sumSuccess));
		sumRow.createCell(6).setCellValue(String.valueOf(sumFail));
		sumRow.createCell(7).setCellValue(String.valueOf(sumFail + sumSuccess));
        
        //Trick way for performance : 先建立一個row 讓sheet可以track all column for auto size 完後, 每個column auto size完畢後在移除row
        sheet.trackAllColumnsForAutoSizing();
        rowNumber += 1;
		Row trackAutoSizingRow = sheet.createRow(rowNumber);
		
		trackAutoSizingRow.createCell(0).setCellValue("發送時間      ");
		trackAutoSizingRow.createCell(1).setCellValue("發送類型      ");
		trackAutoSizingRow.createCell(2).setCellValue("部門名稱      ");
		trackAutoSizingRow.createCell(3).setCellValue("服務名稱      ");
		trackAutoSizingRow.createCell(4).setCellValue("推播主題      ");
		trackAutoSizingRow.createCell(5).setCellValue("發送成功數      ");
		trackAutoSizingRow.createCell(6).setCellValue("發送失敗數      ");
		trackAutoSizingRow.createCell(7).setCellValue("發送數量      ");
		
//		/* 自動調整欄寬  getPhysicalNumberOfCells() 跟 getFirstCellNum() 都無法正常使用 */ 
//		for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
		for (Integer col_index = 0; col_index < 8; col_index++) {        
			sheet.autoSizeColumn(col_index);
		}		
		sheet.removeRow(trackAutoSizingRow);
		
        end = System.currentTimeMillis();	        
        log.info("End GetPushApiEffects () . Time duration : " + (end - start) + " milliseconds.");
		
	}

	private void getPushApiEffectDetail(SXSSFWorkbook workbook, SXSSFSheet sheet, String createTime) {
		//一般只會撈取一筆資料 可以一開始就track all columns for auto sizing.
        sheet.trackAllColumnsForAutoSizing();
		
		Row row = sheet.createRow(0);
		//增加空白, 以美化排版.
		   		
		row.createCell(0).setCellValue("發送時間      ");
		row.createCell(1).setCellValue("發送類型      ");
		row.createCell(2).setCellValue("部門名稱      ");
		row.createCell(3).setCellValue("服務名稱      ");
		row.createCell(4).setCellValue("推播主題      ");
		row.createCell(5).setCellValue("UID          ");
		row.createCell(6).setCellValue("訊息內容      ");
		row.createCell(7).setCellValue("狀態         ");
		
        long start = System.currentTimeMillis();	        

		List<PushMessageRecord> records = pushMessageRecordService.getPushMessageRecordByCreateTime(createTime);
        
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/mm/dd hh:mm:ss"));
		Integer rowNumber = 1;
		
		for(PushMessageRecord record : records) {
			row = sheet.createRow(rowNumber);
			
			Cell sendTimeCell = row.createCell(0);
			// TODO : Need to confirm with PM : createTime() or SendTime()
//			sendTimeCell.setCellValue(record.getSendTime());
			sendTimeCell.setCellValue(record.getCreateTime());
			sendTimeCell.setCellStyle(cellStyle);
			row.createCell(1).setCellValue((record.getSendType().equals("IMMEDIATE")) ? "立即" : "預約");
			row.createCell(2).setCellValue(record.getDepartment());
			row.createCell(3).setCellValue(record.getServiceName());
			row.createCell(4).setCellValue(record.getPushTheme());
			row.createCell(5).setCellValue(record.getUID());
			row.createCell(6).setCellValue(record.getSendMessage());
			row.createCell(7).setCellValue(this.messageTranslate(record.getMainMessage()));
			
			rowNumber += 1;
		}
//		/* 自動調整欄寬  getPhysicalNumberOfCells() 跟 getFirstCellNum() 都無法正常使用 */ 
//		for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
		for (Integer col_index = 0; col_index < 8; col_index++) {
			sheet.autoSizeColumn(col_index);
		}
		
		long end = System.currentTimeMillis();	        
        log.info("End GetPushApiEffectDetail(),  Time duration : " + (end - start) + " milliseconds.");		
		
	}
	
	private String messageTranslate(String message) {
		if(message.equals("Success"))
			return "發送成功";
		else if(message.equals("Failed to send messages"))
			return "失敗：無效的 UID";
		else if(message.equals("The property, 'to', in the request body is invalid (line: -, column: -)"))
			return "失敗：無效的 UID";
		else if(message.contains("The request body has "))
			return "失敗：訊息格式錯誤";
					
		return message;
	}
}
