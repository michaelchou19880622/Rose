package com.bcs.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.bcs.core.log.util.SystemLogUtil;

public class ErrorRecord {

	public static String recordError(Throwable e) {
		return recordError(e, true);
	}
	/**
	 * @param e
	 * @return result
	 */
	public static String recordError(Throwable e, boolean saveLog) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		try {
			String output = os.toString("UTF8");
			if(saveLog){
				SystemLogUtil.saveLogError("SYSTEM", "RecordError", output, e.getMessage());
			}
			return output;
		} catch (Throwable e2) {
			return e2.fillInStackTrace().toString();
		}
	}
}
