package com.bcs.core.report.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToTextFromList {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToTextFromList.class);
	
	public static String exportToTextPath(String name){

		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		return filePath;
	}
	
	public static String exportToTextName(String name, Date date){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String fileName = name + "_" + sdf.format(date) + ".txt";
		
		return fileName;
	}
	
	public void exportResultToText(String name, List<String> titles, List<List<String>> data) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = name + "_" + sdf.format(date) + ".txt";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToText(filePath, fileName, titles, data);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	/**
	 * 匯出TEXT
	 */
	public void exportToText(String exportPath, String fileName, List<List<String>> data) throws Exception {
		this.exportToText(exportPath, fileName, null, data);
	}
	
	public void exportToText(String exportPath, String fileName, List<String> titles, List<List<String>> data) throws Exception {
		try {
			PrintWriter out = new PrintWriter( exportPath + System.getProperty("file.separator") + fileName );
			
			this.exportToText(out, titles, data);
			
			out.close();
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
	public void exportToText(PrintWriter out, List<List<String>> data) throws Exception{
		this.exportToText(out, null, data);
	}
	
	public void exportToText(PrintWriter out, List<String> titles, List<List<String>> data) throws Exception{

		if(titles != null && titles.size() > 0){
			for(int cell = 0; cell < titles.size(); cell++){
				if(cell != 0){
					out.print(",");
				}
				
				String cellData = titles.get(cell);

				if(cellData != null){
					out.print(cellData.toString());
				}
				else{
					out.print("-");
				}
			}
			out.println();
		}
		
		for(List<String> cellDatas : data ){
			
			for(int cell = 0; cell < cellDatas.size(); cell++){
				if(cell != 0){
					out.print(",");
				}
				
				Object cellData = cellDatas.get(cell);
				
				if(cellData != null){
					out.print(cellData.toString());
				}
				else{
					out.print("-");
				}
			}
			
			out.println();
		}
	}
}
