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
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.report.service.ContentLinkReportService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForLinkClickReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForLinkClickReport.class);

	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private ContentLinkReportService contentLinkReportService;
	@Autowired
	private ContentFlagService contentFlagService;
	
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
			}
		}
	}
	
	/**
	 * 匯出 Link Click Report EXCEL
	 */
	public void exportToExcelForLinkClickReportNew(String exportPath, String fileName, String startDate, String endDate,  String linkId, String linkUrl) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheetLink = wb.createSheet("ClickReportById");
			this.exportToExcelForLinkClickReportNew(wb, sheetLink, startDate, endDate, linkId, linkUrl);
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
	public void exportToExcelForLinkClickReportNew(Workbook wb, Sheet sheet, String startDate, String endDate, String linkId, String linkUrl) throws Exception{
		ContentLink link = contentLinkService.findOne(linkId);
		if(link == null){
			throw new Exception("link Id Error");
		}			
		String linkTitle = link.getLinkTitle();
		//取得匯出資料
		Map<String, Map<String, Long>> result = contentLinkReportService.getLinkIdReportNew(startDate, endDate, linkId);
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
			}
		}
	}
	
	public void exportLinkClickReportListNew(String exportPath, String fileName, String startDate, String endDate,  String dataStartDate, String dataEndDate, String queryFlag) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheetLink = wb.createSheet("ClickReportBySearch");
			this.exportToExcelForLinkClickReportNew(wb, sheetLink, startDate, endDate, dataStartDate, dataEndDate, queryFlag);
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void exportToExcelForLinkClickReportNew(Workbook wb, Sheet sheet, String startDate, String endDate, String dataStartDate, String dataEndDate, String queryFlag) throws Exception{
		List<Object[]> result = null; // TRACING_ID, LINK_ID, LINK_TITLE, LINK_URL, MODIFY_TIME, CLICK_COUNT, USER_COUNT
		String tracingUrlPre = UriHelper.getTracingUrlPre();
		result = contentLinkService.findListByModifyDateAndFlag(startDate, endDate, dataStartDate, dataEndDate, queryFlag, 0, -1);
		int seqNo = 0;
		Row row = sheet.createRow(seqNo++); // declare a row object reference
		row.createCell(0).setCellValue("追蹤連結");
		row.createCell(1).setCellValue("連結名稱");
		row.createCell(2).setCellValue("追蹤目標");
		row.createCell(3).setCellValue("時間/註記");
		row.createCell(4).setCellValue("點擊次數");
		row.createCell(5).setCellValue("點擊人數");
		logger.info("exportLinkClickReportListNew, Got report data successfully, queryFlag=" + queryFlag + " startDate=" + startDate + " endDate=" + endDate + " dataStartDate=" + dataStartDate + " dataEndDate=" + dataEndDate + " numOfRecords=" + (result == null ? 0 : result.size()));
		for(Object[] data : result){
			logger.info("obj=" + data.toString());
        	Row row1 = sheet.createRow(seqNo++);
			row1.createCell(0).setCellValue(tracingUrlPre + castToString(data[0]));
			row1.createCell(1).setCellValue(castToString(data[2]));
			row1.createCell(2).setCellValue(castToString(data[3]));
			List<String> flagList = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(castToString(data[1]), "LINK");
			String flagStr = castToString(data[4]);
			if (flagStr.length() > 19) {
				flagStr = flagStr.substring(0, 19);
			}
			for(String flag : flagList){
				flagStr += "&" + flag;
			}
			row1.createCell(3).setCellValue(flagStr);
			String totalCount = castToString(data[5]);
			String userCount = castToString(data[6]);
			row1.createCell(4).setCellValue("" + (StringUtils.isBlank(totalCount) ? 0 : Long.parseLong(totalCount)));
			row1.createCell(5).setCellValue("" + (StringUtils.isBlank(userCount) ? 0 : Long.parseLong(userCount)));
		}
		return;
	}
	
	private String castToString(Object obj){
		if(obj != null){
			return obj.toString();
		}
		return "";
	}
}
