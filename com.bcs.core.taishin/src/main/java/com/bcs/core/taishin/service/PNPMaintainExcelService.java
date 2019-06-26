package com.bcs.core.taishin.service;

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

import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.service.BillingNoticeContentTemplateMsgService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class PNPMaintainExcelService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PNPMaintainExcelService.class);

	@Autowired
	private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository; 

	public void exportExcel(String exportPath, String fileName, 
			String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType){
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			
			this.getExcel(workbook, divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private void getExcel(Workbook workbook, String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType){
		List<PNPMaintainAccountModel> accounts = pnpMaintainAccountModelRepository.findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
					divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);					
		try {
			Integer sheetNumber = 1;
			Sheet sheet = this.createSheet(workbook, sheetNumber++);
			Integer rowNumber = 1; 

			for(PNPMaintainAccountModel pnpAccount : accounts) {
				Row row = sheet.createRow(rowNumber);

				// 帳號 帳號屬性 帳號類別 前方來源系統 通路流 員工編號 單位代號 處 部 組 PccCode 狀態 樣板 簡訊內容
				row.createCell(0).setCellValue(pnpAccount.getAccount());
				row.createCell(1).setCellValue(pnpAccount.getAccountAttribute());
				row.createCell(2).setCellValue(pnpAccount.getAccountClass());
				row.createCell(3).setCellValue(pnpAccount.getSourceSystem());
				row.createCell(4).setCellValue(pnpAccount.getPathway());
				row.createCell(5).setCellValue(pnpAccount.getEmployeeId());
				row.createCell(6).setCellValue(pnpAccount.getDepartmentId());
				row.createCell(7).setCellValue(pnpAccount.getDivisionName());
				row.createCell(8).setCellValue(pnpAccount.getDepartmentName());
				row.createCell(9).setCellValue(pnpAccount.getGroupName());
				row.createCell(10).setCellValue(pnpAccount.getPccCode());
				row.createCell(11).setCellValue(pnpAccount.getStatus());
				row.createCell(12).setCellValue(pnpAccount.getTemplate());
				row.createCell(13).setCellValue(pnpAccount.getPnpContent());

				rowNumber += 1;
				if(rowNumber > 1048500) { // RowLimit = 1048576 
					sheet = this.createSheet(workbook, sheetNumber++);
					rowNumber = 1;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	private Sheet createSheet(Workbook workbook, Integer sheetNumber) {
		Sheet sheet = null;
		try {
			sheet = workbook.createSheet("PNP維護帳號" + sheetNumber);
		
			// first row
			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("帳號");
			row.createCell(1).setCellValue("帳號屬性");
			row.createCell(2).setCellValue("帳號類別");
			row.createCell(3).setCellValue("前方來源系統");
			row.createCell(4).setCellValue("通路流");
			row.createCell(5).setCellValue("員工編號");
			row.createCell(6).setCellValue("單位代號");
			row.createCell(7).setCellValue("處");
			row.createCell(8).setCellValue("部");
			row.createCell(9).setCellValue("組");
			row.createCell(10).setCellValue("PccCode");
			row.createCell(11).setCellValue("狀態");
			row.createCell(12).setCellValue("樣板");
			row.createCell(13).setCellValue("簡訊內容");
			// column width
//			sheet.setColumnWidth(0, 13*256);
//			sheet.setColumnWidth(1, 50*256);
//			sheet.setColumnWidth(2, 15*256);
//			sheet.setColumnWidth(3, 15*256);
//			sheet.setColumnWidth(4, 15*256);
//			sheet.setColumnWidth(5, 15*256);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return sheet;
	}
}
