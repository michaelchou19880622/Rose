package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForMid {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForMid.class);
	
	/**
	 * 匯出EXCEL
	 */
	public void exportToExcel(String exportPath, String fileName, String excelName, String title, String time, List<List<String>> data) throws Exception {
		this.exportToExcel(exportPath, fileName, excelName, title, time, null, data);
	}
	
	public void exportToExcel(String exportPath, String fileName, String excelName, String title, String time, List<String> titles, List<List<String>> data) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			/**
			 * exportMidToExcel
			 */
			Sheet sheetLink = wb.createSheet(excelName); // create a new sheet
			this.exportMidToExcel(wb, sheetLink, title, time, titles, data);
			
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
	 * Export MID To Excel
	 * @param wb
	 * @param sheet
	 * @param data
	 * @throws Exception
	 */
	public void exportMidToExcel(Workbook wb, Sheet sheet, String title, String time, List<List<String>> data) throws Exception{
		this.exportMidToExcel(wb, sheet, title, time, null, data);
	}
	
	public void exportMidToExcel(Workbook wb, Sheet sheet, String title, String time, List<String> titles, List<List<String>> data) throws Exception{

		int index = 0;
		if(StringUtils.isNotBlank(title)){
			Row row = sheet.createRow(index); // declare a row object reference
			row.createCell(0).setCellValue(title);
			
			if(StringUtils.isNotBlank(time)){
				row.createCell(1).setCellValue(time);
			}
			index++;
		}
		
		if(titles != null && titles.size() > 0){
			Row row = sheet.createRow(index); // declare a row object reference
			for(int cell = 0; cell < titles.size(); cell++){
				String cellData = titles.get(cell);

				Integer count = 0;
				try{
					count = data.get(cell).size();
					cellData += ":" + count;
				}
				catch(Exception e){}
				
				if(cellData != null){
					row.createCell(cell).setCellValue(cellData.toString());	
				}
				else{
					row.createCell(cell).setCellValue("-");	
				}
			}
			index++;
		}

		for(int rowCount = 0; rowCount < data.size(); rowCount++){
			List<String> cellDatas = data.get(rowCount);

			for(int cell = 0; cell < cellDatas.size(); cell++){
				Row row = sheet.getRow(cell + index);
				if(row == null){
					row = sheet.createRow(cell + index);
				}

				String cellData = cellDatas.get(cell);
				if(StringUtils.isNotBlank(cellData)){
					row.createCell(rowCount).setCellValue(cellData);	
				}
				else{
					row.createCell(rowCount).setCellValue("-");	
				}
			}
		}
	}
}
