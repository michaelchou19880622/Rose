package com.bcs.core.upload;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

@Service
public class ImportDataFromExcel {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ImportDataFromExcel.class);
	
	public Set<String> importData(InputStream inp) throws Exception {
		
		Set<String> values = new HashSet<String>();
		
		Workbook wb = WorkbookFactory.create(inp);
		
		Sheet sheetLink = wb.getSheetAt(0);
		
		int blankCount = 0;
		Iterator<Row> rows = sheetLink.rowIterator();
		while(rows.hasNext()){
			Row row = rows.next();
			
			Cell cell = row.getCell(0);
			if(cell != null){

				cell.setCellType(Cell.CELL_TYPE_STRING);	
				
				String str = cell.getStringCellValue();
				logger.debug("Cell:" + str);
				
				if(StringUtils.isNotBlank(str)){
					values.add(str);	
				}
				else{
					blankCount++;
				}
			}
			else{
				blankCount++;
			}
			
			if(blankCount > 10){
				break;
			}
		}
		
		return values;
	}
    
    private static boolean isEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }
    
    private static boolean isEmpty(CSVRecord csvRecord) {
        if (csvRecord == null) {
            return true;
        }
        return false;
    }
    
    private static boolean isEmpty(Cell cell) {
        if (cell == null || StringUtils.isBlank(cell.getStringCellValue())) {
            return true;
        }
        return false;
    }
	
	public Map<String, String> importDataKeyValue(InputStream inp) throws Exception {
		
		Map<String, String> values = new HashMap<String, String>();
		
		Workbook wb = WorkbookFactory.create(inp);
		
		Sheet sheetLink = wb.getSheetAt(0);
		
		int blankCount = 0;
		Iterator<Row> rows = sheetLink.rowIterator();
		while(rows.hasNext()){
			Row row = rows.next();
			
			Cell cellKey = row.getCell(0);
			Cell cellValue = row.getCell(1);
			if(cellKey != null && cellValue != null){

				cellKey.setCellType(Cell.CELL_TYPE_STRING);	
				cellValue.setCellType(Cell.CELL_TYPE_STRING);	
				
				String keyStr = cellKey.getStringCellValue();
				logger.debug("Cell:" + keyStr);
				String valueStr = cellValue.getStringCellValue();
				logger.debug("Cell:" + valueStr);
				
				if(StringUtils.isNotBlank(keyStr) && StringUtils.isNotBlank(valueStr)){
					values.put(keyStr, valueStr);
				}
				else{
					blankCount++;
				}
			}
			else{
				blankCount++;
			}
			
			if(blankCount > 10){
				break;
			}
		}
		
		return values;
	}
	
	public Map<String, List<String>> importDataKeyValueList(InputStream inp) throws Exception {
        
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        
        Workbook wb = WorkbookFactory.create(inp);
        
        Sheet sheetLink = wb.getSheetAt(0);
        
        int blankCount = 0;
        Iterator<Row> rows = sheetLink.rowIterator();
        while(rows.hasNext()){
            Row row = rows.next();

            if(blankCount > 10){
                break;
            }
            if (isEmpty(row)) {
                blankCount++;
                continue;
            }
            
            int lastColumn = row.getLastCellNum();
            Cell keyCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
            if (isEmpty(keyCell)) {
                blankCount++;
                continue;
            }
            
            String key = keyCell.getStringCellValue();
            List<String> values = new ArrayList<String>();
            for (int i = 1; i < lastColumn; i++) {

                Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
                
                if(cell == null){
                    values.add(null);
                } else {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    String str = cell.getStringCellValue();
                    logger.debug("Cell[" + i + ":]" + str);
                    values.add(str);
                }
            }
            
            map.put(key, values);
        }
        
        return map;
    }
	
	public List<Map<String, String>> importCSVDataKeyValueList(InputStream inp) throws Exception { 
		List<Map<String, String>> dataListMap = new ArrayList<>();
        Reader csvReader = new BufferedReader(new InputStreamReader(inp, "UTF-8"));
        Iterable<CSVRecord> csvRecords = CSVFormat.EXCEL.withHeader().withTrim().parse(csvReader);
        for (CSVRecord record : csvRecords) {
        	Iterator it = record.toMap().entrySet().iterator();  
        	Map <String,String> columDataMap = new  HashMap<String, String>();
        	while(it.hasNext()){
        		Map.Entry columnData = (Map.Entry)it.next();
        		if(!columnData.getValue().toString().isEmpty()){//欄位資料不為空
        		    String key = columnData.getKey().toString();
        		    if(key.startsWith(ImportDataFromText.UTF8_BOM)) {
        		        key = key.substring(1);
        		    }
        			columDataMap.put(key.toUpperCase(), columnData.getValue().toString());
        		}
        	}
        	if(!columDataMap.isEmpty()){//欄位資料Map不為空
        		dataListMap.add(columDataMap);
        	}
		}
        return dataListMap;
    }
}
