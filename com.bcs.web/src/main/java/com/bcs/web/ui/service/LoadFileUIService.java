package com.bcs.web.ui.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.utils.ErrorRecord;

@Service
public class LoadFileUIService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(LoadFileUIService.class);
	
	public static void loadFileToResponse(String filePath, String fileName, HttpServletResponse response) throws IOException{

		InputStream inp = new FileInputStream(filePath + System.getProperty("file.separator") + fileName);
		response.setContentType("application/download; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setCharacterEncoding("UTF-8");
		OutputStream outp = response.getOutputStream();
		try {
			IOUtils.copy(inp, outp);
			response.flushBuffer();
		} catch (IOException e) {
			logger.error(ErrorRecord.recordError(e));
			throw e;
		} finally {
			if(outp != null) {
				outp.close();
			}
		}
	}
}
