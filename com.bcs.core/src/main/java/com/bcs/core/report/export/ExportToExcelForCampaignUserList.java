package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.MsgInteractiveCampaignService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForCampaignUserList {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForCampaignUserList.class);
	
	@Autowired
	private ContentGameService contentGameService;
	
	@Autowired
	private MsgInteractiveCampaignService msgInteractiveCampaignService;

	
	/**
	 * 匯出 Winner List EXCEL
	 */
	public void exportToExcelForCampaignUserList(String exportPath, String fileName, String startDate, String endDate, String iMsgId, String prizeId) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetCoupon = wb.createSheet("Campaign User List"); // create a new sheet
			this.exportToExcelForCampaignUserList(wb, sheetCoupon, startDate, endDate, iMsgId, prizeId);
			
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
	 * Export To Excel For Winner List
	 * @param wb
	 * @param sheet
	 * @throws Exception
	 */
	public void exportToExcelForCampaignUserList(Workbook wb, Sheet sheet, String startDate, String endDate, String iMsgId, String prizeId) throws Exception{
		
		if(iMsgId == null){
			throw new Exception("GameId Error");
		}
			
		List<Map<String, Object>> resultGet = msgInteractiveCampaignService.findCampaignUserList(Long.valueOf(iMsgId), startDate, endDate);
		
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("姓名");
        row.createCell(2).setCellValue("電話");
        row.createCell(3).setCellValue("地址");
        row.createCell(4).setCellValue("獎品");
        row.createCell(5).setCellValue("發票");
        row.createCell(6).setCellValue("發票狀態");
        row.createCell(7).setCellValue("活動時間");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if(resultGet.size() != 0){
			int seqNo = 1; //序號
			
			for(Map<String, Object> data : resultGet){
				
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(data.get("UID") != null ? data.get("UID").toString() : "");
				row1.createCell(1).setCellValue(data.get("userName") != null ? data.get("userName").toString() : "");
				row1.createCell(2).setCellValue(data.get("mobile") != null ? data.get("mobile").toString() : "");
                row1.createCell(3).setCellValue(data.get("address") != null ? data.get("address").toString() : "");
				row1.createCell(4).setCellValue(data.get("prizeName") != null ? data.get("prizeName").toString() : "");
				row1.createCell(5).setCellValue(data.get("invNum") != null ? data.get("invNum").toString() : "");
				row1.createCell(6).setCellValue(data.get("invStatus") != null ? data.get("invStatus").toString() : "");
				if (data.get("modifyTime") != null) {
				    Date date = sdf.parse(data.get("modifyTime").toString());
				    row1.createCell(7).setCellValue(sdf.format(date));
				} else {
	                row1.createCell(7).setCellValue("");
				}
				
				seqNo++;
				
//				for (int colNum=0; colNum<row.getLastCellNum(); colNum++) {
//					sheet.autoSizeColumn(colNum);
//				}
			}
		}
	}
	
	String transform(String boo){
		String result = "";
		
		if(boo.equals("true")){
			result = "有";
		}else{
			result = "無";
		}
		
		return result;
	}
}
