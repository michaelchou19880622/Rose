package com.bcs.core.linepoint.utils.service;

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

import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class LinePointReportExcelService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointReportExcelService.class);
	@Autowired
	LinePointMainService linePointMainService;
	@Autowired
	LinePointDetailService linePointDetailService;
	
	// ---- Statistics Report ---- 
	
	public void exportExcel_LinePointStatisticsReport(String filePathAndName, Date startDate, Date endDate, String modifyUser, String title){
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(filePathAndName);
			
			this.getExcel_LinePointStatisticsReport(workbook, startDate, endDate, modifyUser, title);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void getExcel_LinePointStatisticsReport(Workbook workbook, Date startDate, Date endDate, String modifyUser, String title){
		List<LinePointMain> mains = linePointMainService.findByTitleAndModifyUserAndDate(startDate, endDate, modifyUser, title);

		try {
			Integer sheetNumber = 1;
			Sheet sheet = this.createSheet_LinePointStatisticsReport(workbook, sheetNumber++);
			Integer rowNumber = 1; 

			for(LinePointMain main : mains) {
				Row row = sheet.createRow(rowNumber);

				// get Service Name
				String serviceName = "BCS";
				if(main.getSendType().equals(LinePointMain.SEND_TYPE_API)) {
					List<LinePointDetail> details = linePointDetailService.findByLinePointMainId(main.getId());
					serviceName = details.get(0).getServiceName();
				}
				
				// Time Cell Rows
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper createHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("YYYY-MM-DD HH:mm:ss"));
				
				Cell createTimeCell = row.createCell(1);
				createTimeCell.setCellValue(main.getModifyTime());
				createTimeCell.setCellStyle(cellStyle);
				
				// Combine Other Rows
				row.createCell(0).setCellValue(main.getTitle());
				//row.createCell(1).setCellValue(main.getModifyTime());
				row.createCell(2).setCellValue(main.getModifyUser());
				row.createCell(3).setCellValue(main.getDepartmentFullName());
				row.createCell(4).setCellValue(main.getPccCode());
				row.createCell(5).setCellValue(serviceName);
				row.createCell(6).setCellValue(main.getSerialId());
//				row.createCell(7).setCellValue(main.getSuccessfulCount() + main.getFailedCount());
//				row.createCell(8).setCellValue(main.getSuccessfulCount());
//				row.createCell(9).setCellValue(main.getFailedCount());
				row.createCell(10).setCellValue(main.getTotalAmount());

				rowNumber++;
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createSheet_LinePointStatisticsReport(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private Sheet createSheet_LinePointStatisticsReport(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("LinePoint統計報表" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("專案名稱");
			row.createCell(1).setCellValue("建立日期");
			row.createCell(2).setCellValue("建立人員");
			row.createCell(3).setCellValue("建立人員單位");
			row.createCell(4).setCellValue("PCC");
			row.createCell(5).setCellValue("Service name");
			row.createCell(6).setCellValue("Campaign Code");
			row.createCell(7).setCellValue("發送筆數");
			row.createCell(8).setCellValue("成功筆數");
			row.createCell(9).setCellValue("失敗筆數");
			row.createCell(10).setCellValue("發送總點數");
			
			// column width
			for (int i=0; i<=10; i++) {
				sheet.setColumnWidth(i, 35*256);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}
	
	// ---- Statistics Report Detail ---- 
	
	public void exportExcel_LinePointStatisticsReportDetail(String filePathAndName, Long linePointMainId){
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(filePathAndName);
			
			this.getExcel_LinePointStatisticsReportDetail(workbook, linePointMainId);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void getExcel_LinePointStatisticsReportDetail(Workbook workbook, Long linePointMainId){
		List<LinePointDetail> details = linePointDetailService.findByLinePointMainId(linePointMainId);
		logger.info("[getExcel_LinePointStatisticsReportDetail] details:"+details);
		
		try {
			Integer sheetNumber = 1;
			Sheet sheet = this.createSheet_LinePointStatisticsReportDetail(workbook, sheetNumber++);
			Integer rowNumber = 1; 

			for(LinePointDetail detail : details) {
				String result = "";
				if(detail.getStatus().equals(LinePointDetail.STATUS_SUCCESS)) {
					result = "成功";
				}else if(detail.getStatus().equals(LinePointDetail.STATUS_FAIL)) {
					result = "失敗";
				}else {
					continue;
				}
				
				Row row = sheet.createRow(rowNumber);

				// Time Cell Rows
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper createHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("YYYY-MM-DD HH:mm:ss"));
				
				Cell createTimeCell = row.createCell(0);
				createTimeCell.setCellValue(detail.getSendTime());
				createTimeCell.setCellStyle(cellStyle);
				
				// Combine Other Rows
				row.createCell(1).setCellValue(detail.getOrderKey());
				row.createCell(2).setCellValue(detail.getUid());
				row.createCell(3).setCellValue(detail.getCustid());
				row.createCell(4).setCellValue(detail.getAmount());
				

				
				if(detail.getDetailType().equals(LinePointDetail.DETAIL_TYPE_CANCEL_API) || 
						detail.getDetailType().equals(LinePointDetail.DETAIL_TYPE_CANCEL_API)) {
					result = "取消" + result;
				}
				
				row.createCell(5).setCellValue(result);
				row.createCell(6).setCellValue(detail.getStatus().equals(LinePointDetail.STATUS_FAIL)?detail.getMessage():"");

				rowNumber++;
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createSheet_LinePointStatisticsReportDetail(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private Sheet createSheet_LinePointStatisticsReportDetail(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("LinePoint統計明細報表" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("發送時間");
			row.createCell(1).setCellValue("訂單編號");
			row.createCell(2).setCellValue("客戶UID");
			row.createCell(3).setCellValue("客戶ID");
			row.createCell(4).setCellValue("發送數量");
			row.createCell(5).setCellValue("發送結果");
			row.createCell(6).setCellValue("失敗原因");
			
			// column width
			for (int i=0; i<=10; i++) {
				sheet.setColumnWidth(i, 35*256);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}
}
