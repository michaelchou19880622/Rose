package com.bcs.core.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CryptUtil {
    /** Logger */
    private static Logger logger = Logger.getLogger(CryptUtil.class);
    
    public static final String AES = "AES";
    
    CryptUtil() {}
    
    public static void main(String[] args) throws Exception {
    	String token = CryptUtil.Encrypt(CryptUtil.AES, "cp390819", "taishinlinebuscs", "taishinlinebuscs");
    	System.out.println(token);
    	
    	System.out.println(CryptUtil.Decrypt(CryptUtil.AES, token, "taishinlinebuscs", "taishinlinebuscs").equals("cp390819"));
    }
    
    public static String Encrypt(String algorithm, String message, String secretKey, String initializationVector) throws Exception {
        if(StringUtils.isBlank(algorithm) || StringUtils.isBlank(message) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Missing Parameters!");
        }
        if(secretKey.length() < 16)
            throw new IllegalArgumentException("Secret key must be more than 16 charactors!");
        
        logger.info("[CryptUtil] Encrypt - algorithm: " + algorithm);
        logger.info("[CryptUtil] Encrypt - message: " + message);
        logger.info("[CryptUtil] Encrypt - secret key: " + secretKey);
        
        if(algorithm.equals("AES")) {
            byte[] raw = secretKey.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, algorithm);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes());
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            
            byte[] encrypted = cipher.doFinal(message.getBytes());            
            String encryptString = Base64.getEncoder().encodeToString(encrypted);
            
            logger.info("[CryptUtil] Encrypt - result: " + encryptString);
            
            return encryptString;
        } else {
            throw new IllegalArgumentException("Unsupporting algorithm!");
        }
    }
    
    public static String Decrypt(String algorithm, String encryptedString, String secretKey, String initializationVector) throws Exception {
        if(StringUtils.isBlank(algorithm) || StringUtils.isBlank(encryptedString) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Missing Parameters!");
        }
        if(secretKey.length() < 16)
            throw new IllegalArgumentException("Secret key must be more than 16 charactors!");
        
        logger.info("[CryptUtil] Decrypt - algorithm: " + algorithm);
        logger.info("[CryptUtil] Decrypt - encryptedString: " + encryptedString);
        logger.info("[CryptUtil] Decrypt - secret key: " + secretKey);
        
        if(algorithm.equals("AES")) {
            byte[] raw = secretKey.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, algorithm);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes());
            
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            
            byte[] decryptedContent = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            String decryptedString = new String(decryptedContent);
            
            logger.info("[CryptUtil] Decrypt - result: " + decryptedString);
            
            return decryptedString;
        } else {
            throw new IllegalArgumentException("Unsupporting algorithm!");
        }
    }

}