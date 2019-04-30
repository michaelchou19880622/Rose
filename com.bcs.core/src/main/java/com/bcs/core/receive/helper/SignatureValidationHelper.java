package com.bcs.core.receive.helper;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

public class SignatureValidationHelper {
	/** Logger */
	private static Logger logger = Logger.getLogger(SignatureValidationHelper.class);
	
	public static boolean signatureValidation(String receivingMsg, String ChannelId, String channelSignature){
		// Signature validation
		try {
			String signature = encode(receivingMsg, ChannelId);
			logger.info("channelSignature:" + channelSignature);
			logger.info("signature:" + signature);
			
			if(channelSignature.equals(signature)){
				return true;
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		saveLog(receivingMsg, ChannelId, channelSignature);
		
		return false;
	}
	
	public static String encode(String input, String ChannelName) throws Exception{

		String channelSecret = CoreConfigReader.getString(ChannelName, "ChannelSecret", true);
		SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		
		logger.info("channelSecret: " + channelSecret);
		
		return Base64.encodeBase64String(mac.doFinal(input.getBytes("UTF-8")));
	}
	
	private static void saveLog(String receivingMsg, String ChannelId, String channelSignature){
		List<Object> content = new ArrayList<Object>();
		content.add(receivingMsg);
		content.add(ChannelId);
		content.add(channelSignature);

		SystemLogUtil.saveLogError("SignatureValidation", "SignatureValidationFail", "SYSTEM", content, ChannelId);
	}
}
