package com.bcs.core.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

public class BarcodeGenerator {

	private static int DEFAULT_DPI = 100;
	private static int DEFAULT_HEIGHT = 30;
	/** Logger */
	private static Logger logger = Logger.getLogger(FileUtil.class);

	public static void generateBarcode128(String inputStr, OutputStream stream)
			throws Exception {

		generateBarcode128(inputStr, stream, DEFAULT_DPI, DEFAULT_HEIGHT);
	}

	public static void generateBarcode128(String inputStr, OutputStream stream,
			int dpi, int height) throws Exception {

		createBarCode(inputStr, stream, dpi, height);
	}

	public static void generateBarcode128(String inputStr) throws Exception {

		generateBarcode128(inputStr, DEFAULT_DPI, DEFAULT_HEIGHT);
	}

	public static void generateBarcode128(String inputStr, int dpi, int height)
			throws Exception {

		String fileOutputPath = System.getProperty("user.home") + "/Desktop/barcode128" + inputStr + ".png";

		logger.debug("fileOutputPath:" + fileOutputPath);

		// Open output file
		File outputFile = new File(fileOutputPath);
		OutputStream out = new FileOutputStream(outputFile);
		createBarCode(inputStr, out, dpi, height);
	}

	private static void createBarCode(String inputStr, OutputStream stream,
			int dpi, int height) throws IOException {

		Code128Bean bean = new Code128Bean();

		// Configure the barcode generator
		bean.setModuleWidth(UnitConv.in2mm(2.0f / dpi));
		bean.setBarHeight(height);
		bean.setFontSize(0);

		// Set up the canvas provider for monochrome PNG output
		BitmapCanvasProvider canvas = new BitmapCanvasProvider(stream,
				"image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

		// Generate the barcode
		bean.generateBarcode(canvas, inputStr);

		// Signal end of generation
		canvas.finish();
	}
}
