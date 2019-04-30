package com.bcs.core.bot.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.report.service.InteractiveReportService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelUserGroup {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelUserGroup.class);

	@Autowired
	private InteractiveReportService interactiveReportService;
	@Autowired
	private MsgInteractiveMainService msgInteractiveMainService;
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	@Autowired
	private MsgBotReceiveService msgBotReceiveService;
	
	public void exportToExcelUserGroup(String exportPath, String startDate, String endDate, String fileName){

		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx

			Sheet sheet = wb.createSheet();
			
			this.exportToExcelUserGroup(sheet, startDate, endDate);
			
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void exportToExcelUserGroup(Sheet sheet, String startDate, String endDate) throws Exception{

		String referenceId = RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString();
		
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("日期");
		row.createCell(1).setCellValue("已串聯有效好友數");
		row.createCell(2).setCellValue("未串聯有效好友數");
		row.createCell(3).setCellValue("串聯後封鎖好友數");
		row.createCell(4).setCellValue("新增好友數");
		row.createCell(5).setCellValue("封鎖好友數");

		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.debug("startDate:" + startDate);
			logger.debug("endDate:" + endDate);

			// Query CONTENT_TYPE_USER_GROUP
			Map<String, Map<String, Long>> userGroupList = recordReportService
					.findRecordReportListByContentType(referenceId,
							RECORD_REPORT_TYPE.CONTENT_TYPE_USER_GROUP.toString(), startDate, endDate);
			
			// Query CONTENT_TYPE_RECEIVE_OP
			Map<String, Map<String, Long>> receiveOpList = recordReportService
					.findRecordReportListByContentType(referenceId,
							RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(), startDate, endDate);

			int rowCount = 1;

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);
					
					String startTimeStr = sdf.format(calendarStart.getTime());
					String breakTimeStr = sdf.format(calendarBreak.getTime());
					
					Map<String, Long> mapUserGroup = userGroupList.get(startTimeStr);
					Map<String, Long> mapReceiveOp = receiveOpList.get(startTimeStr);
					
					Row dateRow = sheet.createRow(rowCount);

					// Query countBINDED
					Long countBINDED = null;
					if(mapUserGroup != null){
						countBINDED = mapUserGroup.get(RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_BINDED_COUNT.toString());
					}
					if (countBINDED == null) {
						countBINDED = sendGroupService.countDefaultGroupSize(-2L,startTimeStr, breakTimeStr);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										RECORD_REPORT_TYPE.CONTENT_TYPE_USER_GROUP.toString(),
										RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_BINDED_COUNT.toString(), 
										countBINDED);
					}

					// Query countUNBIND
					Long countUNBIND = null;
					if(mapUserGroup != null){
						countUNBIND = mapUserGroup.get(RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_UNBIND_COUNT.toString());
					}
					if (countUNBIND == null) {
						countUNBIND = sendGroupService.countDefaultGroupSize(-3L,startTimeStr, breakTimeStr);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										RECORD_REPORT_TYPE.CONTENT_TYPE_USER_GROUP.toString(),
										RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_UNBIND_COUNT.toString(), 
										countUNBIND);
					}

					// Query countBinded2Block
					Long countBinded2Block = null;
					if(mapUserGroup != null){
						countBinded2Block = mapUserGroup.get(RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_BINDED_TO_BLOCK_COUNT.toString());
					}
					if (countBinded2Block == null) {
						countBinded2Block = userTraceLogService.countBinded2Block(startTimeStr, breakTimeStr);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										RECORD_REPORT_TYPE.CONTENT_TYPE_USER_GROUP.toString(),
										RECORD_REPORT_TYPE.DATA_TYPE_USER_GROUP_BINDED_TO_BLOCK_COUNT.toString(), 
										countBinded2Block);
					}

					// Query countReceiveOpAdd
					Long countReceiveOpAdd = null;
					if(mapReceiveOp != null){
						countReceiveOpAdd = mapReceiveOp.get(RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_ADD_COUNT.toString());
					}
					if (countReceiveOpAdd == null) {
						countReceiveOpAdd = msgBotReceiveService
								.countReceiveByType(startTimeStr, breakTimeStr,MsgBotReceive.EVENT_TYPE_FOLLOW);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(),
										RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_ADD_COUNT.toString(),
										countReceiveOpAdd);
					}
					
					// Query countReceiveOpBlock
					Long countReceiveOpBlock = null;
					if(mapReceiveOp != null){
						countReceiveOpBlock = mapReceiveOp.get(RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_BLOCK_COUNT.toString());
					}
					if (countReceiveOpBlock == null) {
						countReceiveOpBlock = msgBotReceiveService
								.countReceiveByType(startTimeStr, breakTimeStr,MsgBotReceive.EVENT_TYPE_UNFOLLOW);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(),
										RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_BLOCK_COUNT.toString(),
										countReceiveOpBlock);
					}
					
					dateRow.createCell(0).setCellValue(startTimeStr);
					dateRow.createCell(1).setCellValue(countBINDED);
					dateRow.createCell(2).setCellValue(countUNBIND);
					dateRow.createCell(3).setCellValue(countBinded2Block);
					dateRow.createCell(4).setCellValue(countReceiveOpAdd);
					dateRow.createCell(5).setCellValue(countReceiveOpBlock);

					calendarStart.add(Calendar.DATE, 1);
					rowCount++;
				}
				else{
				
					break;
				}
			}
		}
		else{
			throw new Exception("Time Range Error");
		}
	}
}
