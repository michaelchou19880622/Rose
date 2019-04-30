package com.bcs.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

public class InputStreamZipUtil {

	private static final int BUFFER_SIZE = 4096;
	/** Logger */
	private static Logger logger = Logger.getLogger(InputStreamZipUtil.class);

	public static String getInputStr(InputStream  inputStr) throws Exception{
		return getInputStr(inputStr, false);
	}
	
	public static String getInputStr(InputStream  inputStr, boolean withLine) throws Exception{

		String content = "";

		if(inputStr != null){
			
			ZipInputStream zipIn = new ZipInputStream(inputStr);
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					content += extractFile(zipIn);
				} else {
					
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			
			try{
				zipIn.close();
			}
			catch(Exception e){}
		}
		
		return content; 
	}
	
    private static String extractFile(ZipInputStream zipIn) throws IOException {
    	String result = "";
        byte[] bytesIn = new byte[BUFFER_SIZE];
        
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
			logger.debug("read size : " + read);
        	String str = new String(bytesIn, StandardCharsets.UTF_8).substring(0, read);
        	result += str;
        }
        
        return result;
    }
}
