package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.SQLDateFormatUtil;

@Service
public class ExportReportForMGM {
	
    public static final String REPORT_TYPE_COMPLETED = "COMPLETED";
    public static final String REPORT_TYPE_UNCOMPLETED = "UNCOMPLETED";
    
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportReportForMGM.class);
	
    @Autowired
    private ShareUserRecordService shareUserRecordService;
	@Autowired
	private ShareCampaignService shareCampaignService;
    
	/**
	 * 匯出 
	 */
	public void exportToExcel(String exportPath, String fileName, String startDate, String endDate, String campaignId, String reportType) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
	        ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
			
			Sheet sheetLink = wb.createSheet(shareCampaign.getCampaignName()); // create a new sheet
			
			if(REPORT_TYPE_COMPLETED.equals(reportType) || REPORT_TYPE_UNCOMPLETED.equals(reportType)) {
			    this.exportCompletedOrUncompleted(wb, sheetLink, startDate, endDate, campaignId, reportType);
			}
			else {
			    this.exportDetail(wb, sheetLink, startDate, endDate, campaignId);
			}
			
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void exportDetail(Workbook wb, Sheet sheet, String startDate, String endDate, String campaignId) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        Date start = sdf.parse(startDate);
        Date end = sdf.parse(endDate);
        Calendar c = Calendar.getInstance();
        c.setTime(end);
        c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
        c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
        end = c.getTime();
        
        List<Object[]> resultGet = shareUserRecordService.findByModifyTimeAndCampaignId(start, end, campaignId);
        
        Row row = sheet.createRow(0); // declare a row object reference
        row.createCell(0).setCellValue("分享者UID");
        row.createCell(1).setCellValue("分享時間");
        row.createCell(2).setCellValue("被分享者UID");
        row.createCell(3).setCellValue("點擊時間");
        
        if(resultGet.size() != 0){
            int seqNo = 1; //序號
            
            for(Object[] o : resultGet) {

                Row row1 = sheet.createRow(seqNo);
                row1.createCell(0).setCellValue(o[0] == null? "" : o[0].toString());
                row1.createCell(1).setCellValue(o[1] == null? "" : sdf2.format(SQLDateFormatUtil.formatSqlStringToDate(o[1], sdf2)));
                row1.createCell(2).setCellValue(o[2] == null? "" : o[2].toString());
                row1.createCell(3).setCellValue(o[3] == null? "" : sdf2.format(SQLDateFormatUtil.formatSqlStringToDate(o[3], sdf2)));
                seqNo++;
            }
        }
    }
	
	public void exportCompletedOrUncompleted(Workbook wb, Sheet sheet, String startDate, String endDate, String campaignId, String reportType) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        Date start = sdf.parse(startDate);
        Date end = sdf.parse(endDate);
        Calendar c = Calendar.getInstance();
        c.setTime(end);
        c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
        c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
        end = c.getTime();
        
        List<Object[]> resultGet = null;
        
        if(REPORT_TYPE_COMPLETED.equals(reportType)) {
            resultGet = shareUserRecordService.findCompletedByModifyTimeAndCampaignId(start, end, campaignId);
        }
        else if(REPORT_TYPE_UNCOMPLETED.equals(reportType)) {
            resultGet = shareUserRecordService.findUncompletedByModifyTimeAndCampaignId(start, end, campaignId);
        }
        
        Row row = sheet.createRow(0); // declare a row object reference
        row.createCell(0).setCellValue("分享者UID");
        row.createCell(1).setCellValue("分享時間");
        row.createCell(2).setCellValue("分享人數");
        
        if(resultGet != null && resultGet.size() != 0){
            int seqNo = 1; //序號
            
            for(Object[] o : resultGet) {

                Row row1 = sheet.createRow(seqNo);
                row1.createCell(0).setCellValue(o[0] == null? "" : o[0].toString());
                row1.createCell(1).setCellValue(o[1] == null? "" : sdf2.format(SQLDateFormatUtil.formatSqlStringToDate(o[1], sdf2)));
                row1.createCell(2).setCellValue(o[2] == null? "" : o[2].toString());
                seqNo++;
            }
        }
    }
}
