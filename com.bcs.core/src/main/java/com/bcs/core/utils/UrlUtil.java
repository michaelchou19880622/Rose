package com.bcs.core.utils;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;

public class UrlUtil {
	/** Logger */
	private static Logger logger = Logger.getLogger(UrlUtil.class);

	public static String encodeAndReplace(String linkUrl){
		return encodeAndReplace(linkUrl, null);
	}
	
	public static String encodeAndReplace(String linkUrl, String replace){

		try{
			if(StringUtils.isNotBlank(replace)){
				linkUrl = linkUrl.replaceAll("\\{replace\\}", URLEncoder.encode(replace, "UTF-8"));
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		return linkUrl;
	}
	
	public static String encodeAndHash(String linkUrl, String MID, String code){

		try{
			String prefix = CoreConfigReader.getString(CONFIG_STR.HASH_PREFIX, true);
			String suffix = CoreConfigReader.getString(CONFIG_STR.HASH_SUFFIX, true);
			
			String hashOrg = "";

			if(StringUtils.isNotBlank(MID) && linkUrl.indexOf("{from}") > 0){
                linkUrl = linkUrl.replaceAll("\\{from\\}", MID);
				hashOrg += MID;
			}
			else if(linkUrl.indexOf("{from}") > 0){
				linkUrl = linkUrl.replaceAll("\\{from\\}", "");
			}

			if(linkUrl.indexOf("{time}") > 0){
				Long t = (new Date()).getTime();
				linkUrl = linkUrl.replaceAll("\\{time\\}", t.toString());
				hashOrg += t.toString();
			}
			
			if(StringUtils.isNotBlank(code) && linkUrl.indexOf("{code}") > 0){
				linkUrl = linkUrl.replaceAll("\\{code\\}", code);
				hashOrg += code;
			}
			else if(linkUrl.indexOf("{code}") > 0){
				linkUrl = linkUrl.replaceAll("\\{code\\}", "");
			}

			if(linkUrl.indexOf("{hash}") > 0){
				String hashStr = prefix + hashOrg + suffix;
				String hash = DigestUtils.md5Hex(hashStr);
				linkUrl = linkUrl.replaceAll("\\{hash\\}", hash);
			}
			
			return linkUrl;
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return linkUrl;
		}
	}

	public static Boolean validateHash(String MID, String code, String time, String hash){
		return validateHash(MID, code, time, hash, null);
	}
	
	public static Boolean validateHash(String MID, String code, String time, String hash, Long delay){

		String prefix = CoreConfigReader.getString(CONFIG_STR.HASH_PREFIX, true);
		String suffix = CoreConfigReader.getString(CONFIG_STR.HASH_SUFFIX, true);
		
		String hashOrg = "";

		if(StringUtils.isNotBlank(MID)){
			hashOrg += MID;
		}

		if(StringUtils.isNotBlank(time)){
			hashOrg += time;
		}
		
		if(StringUtils.isNotBlank(code)){
			hashOrg += code;
		}

		if(StringUtils.isNotBlank(hash)){
			String hashStr = prefix + hashOrg + suffix;
			String dohash = DigestUtils.md5Hex(hashStr);
			
			if(hash.equals(dohash)){
				
				if(delay != null && delay > 0){
					Date check = new Date(Long.parseLong(time));
					Date now = new Date();
					
					Long checkTime = now.getTime() - check.getTime();
					
					if(checkTime >= 0 && checkTime < delay){
					
						return true;
					}
				}
				else{
					return true;
				}
			}
		}

		return false;
	}
	
	public static String replaceSerialSetting(String linkUrl, String mid, String serialId){
		SerialSettingService serialSettingService = ApplicationContextProvider.getApplicationContext().getBean(SerialSettingService.class);
		
		Map<String, String> replaceParam = null;
			
		if(StringUtils.isNotBlank(serialId)){
			replaceParam = serialSettingService.getSerialSettingReplaceParam(serialId, mid);

			if(replaceParam != null && replaceParam.size() > 0){
				
				for(String replcaTarget : replaceParam.keySet()){
					linkUrl = linkUrl.replaceAll("\\{" + replcaTarget + "\\}", replaceParam.get(replcaTarget));
				}
			}
		}
		
		return linkUrl;
	}
}
