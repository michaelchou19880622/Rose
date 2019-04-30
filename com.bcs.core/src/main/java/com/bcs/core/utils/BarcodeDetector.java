package com.bcs.core.utils;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.oned.MultiFormatOneDReader;

public class BarcodeDetector {

    public BarcodeDetector() {
    }

    public static void main(String[] args) {
    }


    public static Result[] decodeQRCode(BufferedImage image) throws Exception {
    	return decodeQRCode(image, Boolean.FALSE);
    }

    public static Result[] decodeQRCode(BufferedImage image, Boolean isPureBarcode) throws Exception {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType,Object> hints = new HashMap<DecodeHintType,Object>();
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, isPureBarcode);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        
        MultipleBarcodeReader reader = new QRCodeMultiReader();
        return reader.decodeMultiple(bitmap, hints);
    }

    public static Result[] decodeCode39(BufferedImage image) throws Exception {
    	return decodeCode39(image, Boolean.FALSE);
    }

    public static Result[] decodeCode39(BufferedImage image, Boolean isPureBarcode) throws Exception {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType,Object> hints = new HashMap<DecodeHintType,Object>();
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.CODE_39);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, isPureBarcode);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        
        MultipleBarcodeReader reader = new GenericMultipleBarcodeReader(new MultiFormatOneDReader(hints));
        return reader.decodeMultiple(bitmap, hints);
    }

}
