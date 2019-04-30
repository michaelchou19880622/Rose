package com.bcs.web.ui.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class VIPNightUIService {
	@PersistenceContext
    EntityManager entityManager;
	
	/** Logger */
	// private static Logger logger = Logger.getLogger(VIPNightUIService.class);
	
	public Integer getQRcodeScannedCount() {
		return this.getQRcodeScannedRecord().size();
	}
	
	public void exportRecord(String exportPath, String fileName) throws FileNotFoundException, IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("貴賓之夜 QRcode 掃描紀錄");
		List<String> record = this.getQRcodeScannedRecord();
		File folder = new File(exportPath);
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		if(record.size() > 0) {
			for(Integer i = 0; i < record.size(); i++) {
				Row row = sheet.createRow(i);
				
				row.createCell(0).setCellValue(record.get(i));
			}
		}
		
		FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
		wb.write(out);
		out.close();
		wb.close();
	}
	
	private List<String> getQRcodeScannedRecord() {
		String sqlString = "SELECT MID FROM BCS_MSG_API_SEND_RECORD WHERE MSG_NAME = 'VIP_NIGHT_MGM' GROUP BY MID ORDER BY Min(SEND_TIME);";
		
		Query query = entityManager.createNativeQuery(sqlString);
		
		@SuppressWarnings("unchecked")
		List<String> resultList = query.getResultList();
		
		return resultList;
	}
}