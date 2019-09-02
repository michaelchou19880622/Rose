package com.bcs.core.resource;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.spring.ApplicationContextProvider;

public class CoreConfigReader {

	/** configuration reader */
	private static CoreConfigReader reader;
	
	public static boolean isSystemTypeProduction(){
		String systemType = getString(CONFIG_STR.SYSTEM_TYPE);
		if("PRODUCTION".equals(systemType)){
			return true;
		}
		return false;
	}
	
	/**
	 * 帳務系統akka 送訊息服務是否啟動
	 * @return
	 */
	public static boolean isBillingNoticeSendMsg(){
		String isBillingNoticeSendMsg = getString(CONFIG_STR.IS_BN_SENDMSG);
		if(StringUtils.isNotBlank(isBillingNoticeSendMsg) && "true".equals(isBillingNoticeSendMsg)){
			return true;
		}
		return false;
	}
	
	/**
	 * 帳務系統ftp download是否啟動
	 * @return
	 */
	public static boolean isBillingNoticeFtpDownload(){
		String isBillingNoticeFtpDownload = getString(CONFIG_STR.IS_BN_FTPDOWNLOAD);
		if(StringUtils.isNotBlank(isBillingNoticeFtpDownload) && "true".equals(isBillingNoticeFtpDownload)){
			return true;
		}
		return false;
	}
	
	/**
	 *  帳務系統ftp 是否為開發者模式
	 * @return
	 */
	public static boolean isBillingNoticeFtpTypeDevelop(){
		String systemType = getString(CONFIG_STR.BN_FTP_TYPE);
		if("DEVELOP".equals(systemType)){
			return true;
		}
		return false;
	}
	
	/**
	 * PNP akka 送訊息服務是否啟動
	 * @return
	 */
	public static boolean isPNPSendMsg(){
		String isPNPSendMsg = getString(CONFIG_STR.IS_PNP_SENDMSG);
		if(StringUtils.isNotBlank(isPNPSendMsg) && "true".equals(isPNPSendMsg)){
			return true;
		}
		return false;
	}
	
	/**
	 * PNP ftp download是否啟動
	 * @return
	 */
	public static boolean isPNPFtpDownload(){
		String isPNPFtpDownload = getString(CONFIG_STR.IS_PNP_FTPDOWNLOAD);
		if(StringUtils.isNotBlank(isPNPFtpDownload) && "true".equals(isPNPFtpDownload)){
			return true;
		}
		return false;
	}
	
	/**
	 *  PNP ftp 是否為開發者模式
	 * @return
	 */
	public static boolean isPNPFtpTypeDevelop(){
		String systemType = getString(CONFIG_STR.PNP_FTP_TYPE);
		if("DEVELOP".equals(systemType)){
			return true;
		}
		return false;
	}
	
	public static boolean isMainSystem(){
		String isMain = getString(CONFIG_STR.IS_MAIN_SYSTEM);
		if(StringUtils.isNotBlank(isMain) && "true".equals(isMain)){
			return true;
		}
		return false;
	}
	
	public static boolean isSystemTypeTest(){
		String systemType = getString(CONFIG_STR.SYSTEM_TYPE);
		if("TEST".equals(systemType)){
			return true;
		}
		return false;
	}
	
	public static boolean isSystemTypeDevelop(){
		String systemType = getString(CONFIG_STR.SYSTEM_TYPE);
		if("DEVELOP".equals(systemType)){
			return true;
		}
		return false;
	}

	/**
	 * Get the value of the key from properties
	 * 
	 * @param ChannelId
	 * @param key
	 * @return value
	 */
	public static String getString(String key) {
		return getString(key, false);
	}
	
	public static String getString(String key, boolean fromDB) {
		return getString(null, key, fromDB);
	}
	
	public static String getString(String key, boolean fromDB, boolean fromCatch) {
		return getString(null, key, fromDB, fromCatch);
	}
	
	public static String getString(CONFIG_STR key) {
		return getString(key, false);
	}
	
	public static String getString(CONFIG_STR key, boolean fromDB) {

		return getString(key.toString(), fromDB);
	}
	
	public static String getString(CONFIG_STR key, boolean fromDB, boolean fromCatch) {

		return getString(key.toString(), fromDB, fromCatch);
	}
	
	
	public static int getInteger(String key){
		try{
			return Integer.parseInt(getString(key, false));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static int getInteger(CONFIG_STR key){
		try{
			return Integer.parseInt(getString(key, false));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static boolean getBoolean(String key){
		try{
			return Boolean.parseBoolean(getString(key, false));
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static boolean getBoolean(CONFIG_STR key){
		try{
			return Boolean.parseBoolean(getString(key, false));
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static int getInteger(String key, boolean fromDB){
		try{
			return Integer.parseInt(getString(key, fromDB));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static int getInteger(String key, boolean fromDB, boolean fromCatch){
		try{
			return Integer.parseInt(getString(key, fromDB, fromCatch));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static int getInteger(CONFIG_STR key, boolean fromDB){
		try{
			return Integer.parseInt(getString(key, fromDB));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	
	public static int getInteger(CONFIG_STR key, boolean fromDB, boolean fromCatch){
		try{
			return Integer.parseInt(getString(key, fromDB, fromCatch));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static boolean getBoolean(String key, boolean fromDB){
		try{
			return Boolean.parseBoolean(getString(key, fromDB));
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static boolean getBoolean(CONFIG_STR key, boolean fromDB){
		try{
			return Boolean.parseBoolean(getString(key, fromDB));
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static boolean getBoolean(CONFIG_STR key, boolean fromDB, boolean fromCatch){
		try{
			return Boolean.parseBoolean(getString(key, fromDB, fromCatch));
		}
		catch(Exception e){
			return false;
		}
	}
	
	/**
	 * Get the value of the key from properties
	 * 
	 * @param ChannelId
	 * @param key
	 * @return value
	 */
	public static String getString(String ChannelId, String key) {
		return getString(ChannelId, key, false);
	}

	public static String getString(String ChannelId, String key, boolean fromDB) {
		return getString(ChannelId, key, fromDB, true);
	}
	
	public static int getInteger(String ChannelId, String key, boolean fromDB){
		try{
			return Integer.parseInt(getString(ChannelId, key, fromDB));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static int getInteger(String ChannelId, String key, boolean fromDB, boolean fromCatch){
		try{
			return Integer.parseInt(getString(ChannelId, key, fromDB, fromCatch));
		}
		catch(Exception e){
			return -1;
		}
	}
	
	public static String getString(String ChannelId, String key, boolean fromDB, boolean fromCatch) {
		if (reader == null) {
			reader = new CoreConfigReader();
		}
		
		if(ChannelId == null){
			ChannelId = "";
		}
		
		if(fromDB){
			try{
				String result = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findOne(ChannelId + "." + key, fromCatch);
				if(StringUtils.isNotBlank(result)){
					return result;
				}
			}
			catch(Throwable e){}// Skip
		}

		if (reader.resourceBundle.containsKey(ChannelId + "." + key)) {
			return reader.resourceBundle.getString(ChannelId + "." + key);
		}

		return null;
	}

	/** properties */
	private ResourceBundle resourceBundle;

	/**
	 * Constructor
	 */
	private CoreConfigReader() {
		resourceBundle = ResourceBundle.getBundle("config.setting");
	}
}
