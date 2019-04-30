package com.bcs.core.api.test;

import org.apache.log4j.Logger;

import com.bcs.core.utils.BarcodeGenerator;
import com.bcs.core.utils.ErrorRecord;

public class BarcodeGeneratorTest {

	/** Logger */
	private static Logger logger = Logger.getLogger(BarcodeGeneratorTest.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		try{
			BarcodeGenerator.generateBarcode128("A123456789");
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		System.exit(0);
	}
}
