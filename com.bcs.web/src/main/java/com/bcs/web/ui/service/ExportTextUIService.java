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

import com.bcs.core.report.export.ExportToTextFromList;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportTextUIService {
	@Autowired
	private ExportToTextFromList exportToTextFromList;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportTextUIService.class);
	
	public void exportResultToText(HttpServletRequest request, HttpServletResponse response, String name, List<String> titles, List<List<String>> data) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = name + "_" + sdf.format(date) + ".txt";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToTextFromList.exportToText(filePath, fileName, titles, data);
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
}
