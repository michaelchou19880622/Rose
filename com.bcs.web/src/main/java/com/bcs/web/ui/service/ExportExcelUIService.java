package com.bcs.web.ui.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.report.export.ExportToExcelForMid;
import com.bcs.core.report.export.ExportToExcelFromDB;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportExcelUIService {
	@Autowired
	private ExportToExcelForMid exportToExcelForMid;
	@Autowired
	private ExportToExcelFromDB exportToExcelFromDB;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportExcelUIService.class);
	
	public void exportDBResultToExcel(HttpServletRequest request, HttpServletResponse response, String excelName, List<String> titles, List<Object[]> data) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = excelName + "_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelFromDB.exportToExcel(filePath, fileName, excelName, titles, data);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		LoadFileUIService.loadFileToResponse(filePath, fileName, response);

		if(titles != null){
			try{
				titles.clear();
			} catch (Exception e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
		try{
			data.clear();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}

	public void exportMidResultToExcel(HttpServletRequest request, HttpServletResponse response, String excelName, String title, Date start, Date end, List<String> titles, List<List<String>> data) throws IOException{
		
		String time = start.toString() + "~" + end.toString();
		
		this.exportMidResultToExcel(request, response, excelName, title, time, titles, data);
	}
	
	public void exportMidResultToExcel(HttpServletRequest request, HttpServletResponse response, String excelName, String title, String time, List<String> titles, List<List<String>> data) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = excelName + "_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForMid.exportToExcel(filePath, fileName, excelName, title, time, titles, data);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		LoadFileUIService.loadFileToResponse(filePath, fileName, response);

		if(titles != null){
			try{
				titles.clear();
			} catch (Exception e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
		try{
			for(List<String> list : data){
				list.clear();
			}
			
			data.clear();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
