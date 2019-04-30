package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelFromDB {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelFromDB.class);
	
	/**
	 * 匯出EXCEL
	 */
	public void exportToExcel(String exportPath, String fileName, String excelName, List<Object[]> data) throws Exception {
		this.exportToExcel(exportPath, fileName, excelName, null, data);
	}
	
	public void exportToExcel(String exportPath, String fileName, String excelName, List<String> titles, List<Object[]> data) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			/**
			 * exportDBToExcel
			 */
			Sheet sheetLink = wb.createSheet(excelName); // create a new sheet
			this.exportDBToExcel(wb, sheetLink, titles, data);
			
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
	 * Export DB To Excel
	 * @param wb
	 * @param sheet
	 * @param data
	 * @throws Exception
	 */
	public void exportDBToExcel(Workbook wb, Sheet sheet, List<Object[]> data) throws Exception{
		this.exportDBToExcel(wb, sheet, null, data);
	}
	
	public void exportDBToExcel(Workbook wb, Sheet sheet, List<String> titles, List<Object[]> data) throws Exception{

		int index = 0;
		if(titles != null && titles.size() > 0){
			Row row = sheet.createRow(index); // declare a row object reference
			for(int cell = 0; cell < titles.size(); cell++){
				String cellData = titles.get(cell);

				if(cellData != null){
					row.createCell(cell).setCellValue(cellData.toString());	
				}
				else{
					row.createCell(cell).setCellValue("-");	
				}
			}
			index++;
		}
		
		for(Object[] cellDatas : data ){
			
			Row row = sheet.createRow(index); // declare a row object reference
			
			for(int cell = 0; cell < cellDatas.length; cell++){
				Object cellData = cellDatas[cell];
				if(cellData != null){
					row.createCell(cell).setCellValue(cellData.toString());	
				}
				else{
					row.createCell(cell).setCellValue("-");	
				}
			}
			
			index++;
		}
	}
}
