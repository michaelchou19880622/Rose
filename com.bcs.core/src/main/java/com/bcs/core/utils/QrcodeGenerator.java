package com.bcs.core.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrcodeGenerator {

	private static int DEFAULT_Size = 400;
	private static String fileType = "png";
	/** Logger */
	private static Logger logger = Logger.getLogger(FileUtil.class);

	public static void generateQrcode(String inputStr, OutputStream stream)
			throws Exception {

		createQRImage(stream, inputStr, DEFAULT_Size);
	}

	public static void generateBarcode128(String inputStr, OutputStream stream,
			int size) throws Exception {

		createQRImage(stream, inputStr, size);
	}

	public static void generateBarcode128(String inputStr) throws Exception {

		createQRImage(inputStr, DEFAULT_Size);
	}

	public static void createQRImage(String inputStr, int size)
			throws Exception {

		String fileOutputPath = System.getProperty("user.home") + "/Desktop/qrcode/test.png";

		logger.debug("fileOutputPath:" + fileOutputPath);

		// Open output file
		File outputFile = new File(fileOutputPath);
		OutputStream out = new FileOutputStream(outputFile);
		createQRImage(out, inputStr, size);
	}
	
	/**
	 * @param args
	 * @throws WriterException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		String qrCodeText = "http://www.journaldev.com";
		createQRImage(qrCodeText, DEFAULT_Size);
		System.out.println("DONE");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void createQRImage(OutputStream out, String qrCodeText, int size) throws WriterException, IOException {
		// Create the ByteMatrix for the QR-Code that encodes the given String
		Hashtable hintMap = new Hashtable();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,
				BarcodeFormat.QR_CODE, size, size, hintMap);
		// Make the BufferedImage that are to hold the QRCode
		int matrixWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
				BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, matrixWidth, matrixWidth);
		// Paint and save the image using the ByteMatrix
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		ImageIO.write(image, fileType, out);
	}
}
