package com.bcs.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class InputStreamUtil {

	/** Logger */
	private static Logger logger = Logger.getLogger(InputStreamUtil.class);

	public static String getInputStr(InputStream inputStr) throws Exception{
		return getInputStr(inputStr, false);
	}
	
	public static String getInputStr(InputStream inputStr, boolean withLine) throws Exception{

		String content = "";

		if(inputStr != null){
			BufferedReader brR = new BufferedReader(new InputStreamReader((inputStr), StandardCharsets.UTF_8));
	
			String readLineContent = null;
			while ((readLineContent = brR.readLine()) != null) {
				content += readLineContent;
				logger.debug("readLine : " + readLineContent);
				if(withLine){
					content += "\n";
				}
			}
			
			try{
				inputStr.close();
				brR.close();
			}
			catch(Exception e){}
		}
		
		return content; 
	}

	public static void saveInputStr(InputStream inputStr, String filePath) throws Exception{
		saveInputStr(inputStr, filePath, false);
	}
	
	public static void saveInputStr(InputStream inputStr, String filePath, boolean overwrite) throws Exception{

		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		String errorMsg = "";
		try{
			File genfile = new File(filePath);
			
			if(!genfile.exists()){
				genfile.createNewFile();
			}
			out = new BufferedOutputStream(new FileOutputStream(genfile, overwrite));
			in = new BufferedInputStream(inputStr);
			IOUtils.copy(in, out);

			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
	        
	        return;
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			errorMsg = e.getMessage();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			logger.debug("finally");
		}
		throw new Exception(errorMsg);
	}
}
