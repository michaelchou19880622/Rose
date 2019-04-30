package com.bcs.web.ui.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.VIPNightUIService;

@Controller
@RequestMapping("/bcs")
public class BCSVIPNightController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSVIPNightController.class);
	
	@Autowired
	private VIPNightUIService VIPNightUIService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/qrcodeScannedRecordPage")
	public String QRcodeScannedRecordPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("QRcodeScannedRecordPage");
		
		return BcsPageEnum.QRcodeScannedRecordPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getQRcodeScannedCount")
	public ResponseEntity<?> GetQRcodeScannedCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("GetQRcodeScannedCount");
		
		return new ResponseEntity<>(VIPNightUIService.getQRcodeScannedCount(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/exportQRcodeScannedRecord")
	public void ExportQRcodeScannedRecord(HttpServletRequest request, HttpServletResponse response) {
		logger.info("ExportQRcodeScannedRecord");
		
		try {
			String exportPath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			String fileName = "QRcodeScannedList_" + sdf.format(new Date()) + ".xlsx";
			
			VIPNightUIService.exportRecord(exportPath, fileName);
			
			LoadFileUIService.loadFileToResponse(exportPath, fileName, response);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}