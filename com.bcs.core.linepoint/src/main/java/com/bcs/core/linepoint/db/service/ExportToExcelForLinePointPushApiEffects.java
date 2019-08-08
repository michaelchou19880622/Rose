package com.bcs.core.linepoint.db.service;

import java.io.FileOutputStream;
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
public class ExportToExcelForLinePointPushApiEffects {
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForLinePointPushApiEffects.class);

	@Autowired
	private LinePointMainService linePointMainService;
	@Autowired
	private LinePointDetailService linePointDetailService;
	
	public void exportExcel(String exportPath, String fileName) {
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			
			this.getPushApiEffects(workbook);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private void getPushApiEffects(Workbook workbook) {
		try {
			List<LinePointMain> effects = linePointMainService.findAll();
			
			Integer sheetNumber = 1;
			Sheet sheet = this.createPushApiEffectsSheet(workbook, sheetNumber++);
			Integer rowNumber = 1; 
			for(LinePointMain main : effects) {

				Row row = sheet.createRow(rowNumber);
				
				// Time Cell Style
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper createHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd HH:mm:ss"));
				
				Cell createTimeCell = row.createCell(2);
				createTimeCell.setCellValue(main.getModifyTime());
				createTimeCell.setCellStyle(cellStyle);
				
				row.createCell(0).setCellValue(main.getSerialId());
				row.createCell(1).setCellValue(main.getTitle());
				row.createCell(3).setCellValue(main.getAmount());
				row.createCell(4).setCellValue(main.getTotalCount());
				row.createCell(5).setCellValue(main.getSuccessfulCount());
				row.createCell(6).setCellValue(main.getFailedCount());
				row.createCell(7).setCellValue(main.getStatus());
				row.createCell(8).setCellValue(main.getSendType());
				row.createCell(9).setCellValue(main.getModifyUser());
				rowNumber += 1;
				
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createPushApiEffectsSheet(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private Sheet createPushApiEffectsSheet(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("成效報表" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("活動代碼");
			row.createCell(1).setCellValue("標題");
			row.createCell(2).setCellValue("建立時間");
			row.createCell(3).setCellValue("點數/人");
			row.createCell(4).setCellValue("應發送總數");
			row.createCell(5).setCellValue("成功發送數");
			row.createCell(6).setCellValue("失敗發送數");
			row.createCell(7).setCellValue("發送狀態");
			row.createCell(8).setCellValue("發送方式");
			row.createCell(9).setCellValue("建立人員");
						
			// column width
			sheet.setColumnWidth(0, 35*256);
			sheet.setColumnWidth(1, 35*256);
			sheet.setColumnWidth(2, 25*256);
			sheet.setColumnWidth(3, 8*256);
			sheet.setColumnWidth(4, 12*256);
			sheet.setColumnWidth(5, 12*256);
			sheet.setColumnWidth(6, 12*256);
			sheet.setColumnWidth(7, 15*256);
			sheet.setColumnWidth(8, 12*256);
			sheet.setColumnWidth(9, 18*256);
					
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}

	public void exportExcel(String exportPath, String fileName, Long mainId, String status) {
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
		
			this.getPushApiEffects(workbook, mainId, status);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private void getPushApiEffects(Workbook workbook, Long mainId, String status) {
		try {
			List<LinePointDetail> effects = null;
			if(status.equals(LinePointDetail.STATUS_SUCCESS)) {
				effects = linePointDetailService.findSuccess(mainId);
			}else {
				effects = linePointDetailService.findFail(mainId);
			}
			
			Integer sheetNumber = 1;
			Sheet sheet = this.createPushApiEffectsDetailSheet(workbook, sheetNumber++);
			Integer rowNumber = 1; 
			for(LinePointDetail detail : effects) {
				Row row = sheet.createRow(rowNumber);
		
				// Time Cell Style
				CellStyle cellStyle = workbook.createCellStyle();
				CreationHelper createHelper = workbook.getCreationHelper();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("YYYY-MM-DD HH:mm:ss"));

				row.createCell(0).setCellValue(detail.getUid());
				if(status.equals(LinePointDetail.STATUS_SUCCESS)) {
					row.createCell(1).setCellValue(detail.getBalance());
				}else {
					row.createCell(1).setCellValue(detail.getMessage());
				}
				
				Cell createTimeCell = null;
				createTimeCell = row.createCell(2);
				createTimeCell.setCellValue(detail.getTriggerTime());
				createTimeCell.setCellStyle(cellStyle);
				
				rowNumber += 1;
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createPushApiEffectsDetailSheet(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}	
	}
	private Sheet createPushApiEffectsDetailSheet(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("成效明細報表" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("UID");
			row.createCell(1).setCellValue("狀態 (失敗原因/成功後的點數)");
			row.createCell(2).setCellValue("發送時間");
			
			sheet.setColumnWidth(0, 35*256);
			sheet.setColumnWidth(1, 35*256);
			sheet.setColumnWidth(2, 20*256);
			
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}
}
