package com.bcs.core.taishin.service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.PNP.db.service.PNPMaintainAccountModelService;
import com.bcs.core.taishin.circle.service.BillingNoticeContentTemplateMsgService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class PnpReportExcelService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpReportExcelService.class);

	@Autowired
	private PNPMaintainAccountModelService pnpMaintainAccountModelService; 

	public void exportPNPDetailReportExcel(String exportPath, String fileName, 
			String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId){
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			
			this.getDetailReportExcel(workbook, startDate, endDate, account, pccCode, sourceSystem, empId);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void getDetailReportExcel(Workbook workbook, String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId){
		Map<String, List<String>> lists = pnpMaintainAccountModelService.getPNPDetailReportExcelList(startDate, endDate, account, pccCode, sourceSystem, empId);					
		try {
			Integer sheetNumber = 1;
			Sheet sheet = this.createDetailReportSheet(workbook, sheetNumber++);
			Integer rowNumber = 1; 

			for(Object key : lists.keySet()) {
				List<String> list = lists.get(key);
				Row row = sheet.createRow(rowNumber);

				for(int i = 0; i <27; i++) {
					if(i == 17 || i == 19 || i == 25 || i == 26) {	// Date
						row.createCell(i).setCellValue(dateTimeToDateOrTime(list.get(i), "Date"));
					}else if(i == 18 || i == 20) {					// Time
						row.createCell(i).setCellValue(dateTimeToDateOrTime(list.get(i), "Time"));
					}else if(i == 21){								// 發送狀態(PNP代碼)
						row.createCell(i).setCellValue((list.get(i)=="COMPLETE"||list.get(i)=="FINISH")?"200":"501");
					}else if(i == 22){								// 發送狀態(PNP說明)
						row.createCell(i).setCellValue(englishStatusToChinese(list.get(i)));
					}else if(i == 23){								// 發送狀態(簡訊)
						row.createCell(i).setCellValue("");
					}else{
						row.createCell(i).setCellValue(list.get(i));
					}
				}

				rowNumber++;
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createDetailReportSheet(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private Sheet createDetailReportSheet(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("PNP明細報表" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("序號");
			row.createCell(1).setCellValue("前方來源系統");
			row.createCell(2).setCellValue("通路流");
			row.createCell(3).setCellValue("發送通路");
			row.createCell(4).setCellValue("發送帳號");
			row.createCell(5).setCellValue("掛帳PccCode");
			row.createCell(6).setCellValue("發送廠商訊息批次代碼");
			row.createCell(7).setCellValue("發送廠商訊息流水號");
			row.createCell(8).setCellValue("訊息樣板");
			row.createCell(9).setCellValue("訊息內文");
			row.createCell(10).setCellValue("訊息內文點數");
			row.createCell(11).setCellValue("行銷活動代碼");
			row.createCell(12).setCellValue("行銷活動階段");
			row.createCell(13).setCellValue("行銷活動客群代碼");
			row.createCell(14).setCellValue("客戶ID");
			row.createCell(15).setCellValue("客戶手機號碼");
			row.createCell(16).setCellValue("UID");
			row.createCell(17).setCellValue("預約日期");
			row.createCell(18).setCellValue("預約時間");
			row.createCell(19).setCellValue("發送日期");
			row.createCell(20).setCellValue("發送時間");
			row.createCell(21).setCellValue("發送狀態(PNP代碼)");
			row.createCell(22).setCellValue("發送狀態(PNP說明)");
			row.createCell(23).setCellValue("發送狀態(簡訊)");
			row.createCell(24).setCellValue("是否國際簡訊");
			row.createCell(25).setCellValue("資料建立日期");
			row.createCell(26).setCellValue("資料更新日期");
			
			// column width
			for (int i=0; i<27; i++) {
				if(i==9) continue;
				sheet.setColumnWidth(i, 22*256);
			}
			sheet.setColumnWidth(9, 60*256);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}
	
	private String dateTimeToDateOrTime(String dateTimeStr, String dateOrTime) {
		SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		
		try {
			dateTimeStr = dateTimeStr.replaceFirst("\\.\\d*(Z)?$", "$1"); // skip milliseconds
			Date dateTime = sdfDateTime.parse(dateTimeStr);
			
			if(dateOrTime == "Date") {
				return sdfDate.format(dateTime);
			}else if(dateOrTime == "Time") {
				return sdfTime.format(dateTime);
			}
		}catch(Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return null;
	}
	
	private String englishStatusToChinese(String status) {
		switch(status) {
		case "PROCESS": return "發送處理進行中";
		case "FINISH": return "發送處理完成";
		case "FAIL": return "發送處理失敗";
		case "CHECK_DELIVERY": return "發送成功，等待響應";
		case "SENDING": return "發送中";
		case "DELETE": return "已刪除";
		case "DRAFT": return "正在存進資料庫";
		case "WAIT": return "等待進入處理程序";
		case "COMPLETE": return "處理程序完成";
		case "SCHEDULED": return "等待預約發送";
		}
		return null;
	}
	
}
