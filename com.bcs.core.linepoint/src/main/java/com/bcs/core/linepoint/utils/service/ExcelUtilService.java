package com.bcs.core.linepoint.utils.service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExcelUtilService {
	private static Logger logger = Logger.getLogger(ExcelUtilService.class);
	
	public InputStream csvToXlsx(InputStream is, String exportPath, String fileName) {
		try {
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			
			csvToXlsx(is, workbook);
			workbook.write(out);
			out.close();
			workbook.close();
			
			InputStream inp = new FileInputStream(exportPath + System.getProperty("file.separator") + fileName);
			return inp;
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return null;
		}
	}
	
	public static void csvToXlsx(InputStream is, Workbook workBook) {
	    try {
	    	Sheet sheet = workBook.createSheet("sheet1");
	        String currentLine=null;
	        int RowNum=0;
	        
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        while ((currentLine = br.readLine()) != null) {
	            String str[] = currentLine.split(",");
	            RowNum++;
	            Row currentRow=sheet.createRow(RowNum);
	            for(int i=0;i<str.length;i++){
	                currentRow.createCell(i).setCellValue(str[i]);
	            }
	        }
	    }catch (Exception e){
	    	logger.info("[ExcelUtil csvToXlsx] Exception: " + e.getMessage());
	    	logger.error(ErrorRecord.recordError(e));
	    }
	}
}