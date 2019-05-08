package com.bcs.core.taishin.circle.PNP.ftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.tsb.util.TrendPwMgmt;

@Service
public class PNPFtpService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PNPFtpService.class);
//	private static String channelIds = CoreConfigReader.getString(CONFIG_STR.BN_FTP_CHANNELIDS, true);
//	private static String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true);
	private static String fileExtension = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_FILE_EXTENSION, true);
	private static boolean is64Bit = CoreConfigReader.getBoolean(CONFIG_STR.PNP_FTP_IS64BIT, true);
	private Map<String,PNPFtpSetting> ftpSettings = new HashMap<>();

	/**
	 * FTP密碼系統載入dll的狀態，參數為false :系統照原本設計從系統參數檔取得帳號、密碼，<BR>
	 * 參數為true :系統透過JNI機制去取得帳號、密碼
	 */
	public static boolean JNI_LIBRARY_STATUS = false;
	/**
	 * Initial Method
	 */
	static {
//		try {
//			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
//				if (is64Bit) {
//					System.loadLibrary("PwDllJV64");
//				} else {
//					System.loadLibrary("PwDllJV");
//				}
//				JNI_LIBRARY_STATUS = true;
//			}
//		} catch (SecurityException lSE) {
//			JNI_LIBRARY_STATUS = false;
//			logger.error("SecurityException:" + lSE.getMessage());
//		} catch (UnsatisfiedLinkError lUE) {
//			JNI_LIBRARY_STATUS = false;
//			logger.error("UnsatisfiedLinkError:" + lUE.getMessage());
//		} catch (NullPointerException lNPE) {
//			JNI_LIBRARY_STATUS = false;
//			logger.error("NullPointerException:" + lNPE.getMessage());
//		} catch (Exception lEXP) {
//			JNI_LIBRARY_STATUS = false;
//			logger.error("Exception:" + lEXP.getMessage());
//		}

		logger.info("JNI_LIBRARY_STATUS:" + JNI_LIBRARY_STATUS);
	}

	public PNPFtpService() {
		
		logger.info("initFtpSettings..."+ (initFtpSettings() ? "OK" : "Fail"));
//		logger.info("BN_FTP_CHANNELIDS:" + channelIds);
//		if (StringUtils.isNotBlank(channelIds)) {
//			for (String channel : channelIds.split(",")) {
//				if (StringUtils.isNotBlank(channel)) {
//					String host = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_HOST.toString(), true);
//					int port = CoreConfigReader.getInteger(channel, CONFIG_STR.BN_FTP_PORT.toString(), true);
//					int serverHostNamePort = CoreConfigReader.getInteger(channel,
//							CONFIG_STR.BN_FTP_SERVER_HOSTNAME_PORT.toString(), true);
//					String serverHostName = CoreConfigReader.getString(channel,
//							CONFIG_STR.BN_FTP_SERVER_HOSTNAME.toString(), true);
//					String APPCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_APP_CODE.toString(), true);
//					String RESCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_RES_CODE.toString(), true);
//					String account = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_ACCOUNT.toString(), true);
//					String password = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PASSWORD.toString(), true);
//					String path = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PATH.toString(), true);
//					String fileEncoding = CoreConfigReader.getString(channel,
//							CONFIG_STR.BN_FTP_FILE_ENCODING.toString(), true);
//					String protocol = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PROTOCOL.toString(), true);
//					if (StringUtils.isNotBlank(host)) {
//						PNPFtpSetting ftpSetting = new PNPFtpSetting();
//						ftpSetting.setProtocol(protocol);
//						ftpSetting.setChannelId(channel);
//						ftpSetting.setPath(path);
//						ftpSetting.setAccount(account);
//						ftpSetting.setPassword(password);
//						ftpSetting.setAPPCode(APPCode);
//						ftpSetting.setRESCode(RESCode);
//						ftpSetting.setHost(host);
//						ftpSetting.setPort(port);
//						ftpSetting.setFileEncoding(fileEncoding);
//						ftpSetting.setServerHostName(serverHostName);
//						ftpSetting.setServerHostNamePort(serverHostNamePort);
//						if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
//							if (!validateFtpHostData(ftpSetting)) {
//								throw new RuntimeException("FTP setting error!");
//							}
//						} else {
//							if (!validateDevFtpHostData(ftpSetting)) {
//								throw new RuntimeException("FTP setting error!");
//							}
//						}
//
//						ftpSettings.add(ftpSetting);
//						// 正式環境時使用
//						if (!CoreConfigReader.isPNPFtpTypeDevelop()) {
//							loadFtp(ftpSetting);
//						}
//					}
//				}
//			}
//		}
	}
	
	public boolean initFtpSettings() {
		PNPFtpSetting pnpFtpSettingMTK = new PNPFtpSetting();
		pnpFtpSettingMTK.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READLINES_ENCODE, true));
		pnpFtpSettingMTK.setChannelId(AbstractPnpMainEntity.SOURCE_MITAKE);
		pnpFtpSettingMTK.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_MITAKE, true));
		pnpFtpSettingMTK.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_PORT_MITAKE, true));
		pnpFtpSettingMTK.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_MITAKE, true));
		pnpFtpSettingMTK.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_MITAKE, true));
		pnpFtpSettingMTK.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_MITAKE, true));
		pnpFtpSettingMTK.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_MITAKE, true));
		pnpFtpSettingMTK.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APPCODE_MITAKE, true));
		pnpFtpSettingMTK.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RESCODE_MITAKE, true));
		pnpFtpSettingMTK.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_MITAKE, true));
		pnpFtpSettingMTK.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH_MITAKE, true));
		pnpFtpSettingMTK.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_MITAKE, true));
		pnpFtpSettingMTK.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL_MITAKE, true));
		pnpFtpSettingMTK.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MITAKE, true));
		//SMS setting
		pnpFtpSettingMTK.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_MITAKE, true));
		pnpFtpSettingMTK.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_PORT_MITAKE, true));
		pnpFtpSettingMTK.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR_MITAKE, true));
		pnpFtpSettingMTK.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS_MITAKE, true));
		pnpFtpSettingMTK.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST_MITAKE, true));
		pnpFtpSettingMTK.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT_MITAKE, true));
		pnpFtpSettingMTK.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APPCODE_MITAKE, true));
		pnpFtpSettingMTK.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RESCODE_MITAKE, true));
		pnpFtpSettingMTK.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL_MITAKE, true));
		if(!CoreConfigReader.isPNPFtpTypeDevelop()) {
			pnpFtpSettingMTK = useTrendPwMgmt(pnpFtpSettingMTK);
		}
		ftpSettings.put(AbstractPnpMainEntity.SOURCE_MITAKE, pnpFtpSettingMTK);
		
		PNPFtpSetting pnpFtpSettingMI = new PNPFtpSetting();
		pnpFtpSettingMI.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READLINES_ENCODE, true));
		pnpFtpSettingMI.setChannelId(AbstractPnpMainEntity.SOURCE_MING);
		pnpFtpSettingMI.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_MING, true));
		pnpFtpSettingMI.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_PORT_MING, true));
	    pnpFtpSettingMI.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_MING, true));
		pnpFtpSettingMI.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_MING, true));
		pnpFtpSettingMI.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_MING, true));
		pnpFtpSettingMI.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_MING, true));
		pnpFtpSettingMI.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APPCODE_MING, true));
		pnpFtpSettingMI.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RESCODE_MING, true));
		pnpFtpSettingMI.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_MING, true));
		pnpFtpSettingMI.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH_MING, true));
		pnpFtpSettingMI.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_MING, true));
		pnpFtpSettingMI.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL_MING, true));
		pnpFtpSettingMI.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MING, true));
		//SMS setting
		pnpFtpSettingMI.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_MING, true));
		pnpFtpSettingMI.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_PORT_MING, true));
		pnpFtpSettingMI.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR_MING, true));
		pnpFtpSettingMI.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS_MING, true));
		pnpFtpSettingMI.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST_MING, true));
		pnpFtpSettingMI.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT_MING, true));
		pnpFtpSettingMI.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APPCODE_MING, true));
		pnpFtpSettingMI.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RESCODE_MING, true));
		pnpFtpSettingMI.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL_MING, true));
		if(!CoreConfigReader.isPNPFtpTypeDevelop()) {
			pnpFtpSettingMI = useTrendPwMgmt(pnpFtpSettingMI);
		}
		ftpSettings.put(AbstractPnpMainEntity.SOURCE_MING, pnpFtpSettingMI);	
		
		PNPFtpSetting pnpFtpSettingEV8D = new PNPFtpSetting();
		pnpFtpSettingEV8D.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READLINES_ENCODE, true));
		pnpFtpSettingEV8D.setChannelId(AbstractPnpMainEntity.SOURCE_EVERY8D);
		pnpFtpSettingEV8D.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_EVERY8D, true));
		pnpFtpSettingEV8D.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_PORT_EVERY8D, true));
		pnpFtpSettingEV8D.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_EVERY8D, true));
		pnpFtpSettingEV8D.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_EVERY8D, true));
		pnpFtpSettingEV8D.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_EVERY8D, true));
		pnpFtpSettingEV8D.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_EVERY8D, true));
		pnpFtpSettingEV8D.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APPCODE_EVERY8D, true));
		pnpFtpSettingEV8D.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RESCODE_EVERY8D, true));
		pnpFtpSettingEV8D.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_EVERY8D, true));
		pnpFtpSettingEV8D.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH_EVERY8D, true));
		pnpFtpSettingEV8D.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_EVERY8D, true));
		pnpFtpSettingEV8D.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL_EVERY8D, true));
		pnpFtpSettingEV8D.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_EVERY8D, true));
		//SMS setting
		pnpFtpSettingEV8D.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_PORT_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APPCODE_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RESCODE_EVERY8D, true));
		pnpFtpSettingEV8D.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL_EVERY8D, true));
		if(!CoreConfigReader.isPNPFtpTypeDevelop()) {
			pnpFtpSettingEV8D = useTrendPwMgmt(pnpFtpSettingEV8D);
		}
		ftpSettings.put(AbstractPnpMainEntity.SOURCE_EVERY8D, pnpFtpSettingEV8D);	
		
		
		PNPFtpSetting pnpFtpSettingUNI = new PNPFtpSetting();
		pnpFtpSettingUNI.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READLINES_ENCODE, true));
		pnpFtpSettingUNI.setChannelId(AbstractPnpMainEntity.SOURCE_UNICA);
		pnpFtpSettingUNI.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_UNICA, true));
		pnpFtpSettingUNI.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_PORT_UNICA, true));
		pnpFtpSettingUNI.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_UNICA, true));
		pnpFtpSettingUNI.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_UNICA, true));
		pnpFtpSettingUNI.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_UNICA, true));
		pnpFtpSettingUNI.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_UNICA, true));
		pnpFtpSettingUNI.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APPCODE_UNICA, true));
		pnpFtpSettingUNI.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RESCODE_UNICA, true));
		pnpFtpSettingUNI.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_UNICA, true));
		pnpFtpSettingUNI.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH_UNICA, true));
		pnpFtpSettingUNI.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_UNICA, true));
		pnpFtpSettingUNI.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL_UNICA, true));
		pnpFtpSettingUNI.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_UNICA, true));
		//SMS setting
		pnpFtpSettingUNI.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_UNICA, true));
		pnpFtpSettingUNI.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_PORT_UNICA, true));
		pnpFtpSettingUNI.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR_UNICA, true));
		pnpFtpSettingUNI.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS_UNICA, true));
		pnpFtpSettingUNI.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST_UNICA, true));
		pnpFtpSettingUNI.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT_UNICA, true));
		pnpFtpSettingUNI.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APPCODE_UNICA, true));
		pnpFtpSettingUNI.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RESCODE_UNICA, true));
		pnpFtpSettingUNI.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL_UNICA, true));
		if(!CoreConfigReader.isPNPFtpTypeDevelop()) {
			pnpFtpSettingUNI = useTrendPwMgmt(pnpFtpSettingUNI);
		}
		ftpSettings.put(AbstractPnpMainEntity.SOURCE_UNICA, pnpFtpSettingUNI);
		
		return true;
	}
	
	public PNPFtpSetting useTrendPwMgmt(PNPFtpSetting pnpFtpSetting) {
		
		String host = pnpFtpSetting.getHost();
		String serverHostName = pnpFtpSetting.getServerHostName();
		int serverHostNamePort = pnpFtpSetting.getServerHostNamePort();
		String APPCode = pnpFtpSetting.getAPPCode();
		String RESCode = pnpFtpSetting.getRESCode();
		
		Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, APPCode, RESCode);
		logger.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" +  trendPwMgmt.get("pwd"));
		pnpFtpSetting.setAccount(trendPwMgmt.get("uid"));
		pnpFtpSetting.setPassword(trendPwMgmt.get("pwd"));
		logger.info("下載段資源密碼系統的帳號密碼登入完成");
		
		String smsHost = pnpFtpSetting.getSmsHost();
		String smsServerHostName = pnpFtpSetting.getSmsServerHostName();
		int smsServerHostNamePort = pnpFtpSetting.getSmsServerHostNamePort();
		String smsAPPCode = pnpFtpSetting.getSmsAPPCode();
		String smsRESCode = pnpFtpSetting.getSmsRESCode();
		Map<String, String> trendPwMgmtSMS = loadFtp(smsHost, smsServerHostName, smsServerHostNamePort, smsAPPCode, smsRESCode);
		logger.info("loginFTP:" + trendPwMgmtSMS.get("uid") + " PWD:" +  trendPwMgmtSMS.get("pwd"));
		pnpFtpSetting.setSmsAccount(trendPwMgmtSMS.get("uid"));
		pnpFtpSetting.setSmsPassword(trendPwMgmtSMS.get("pwd"));
		logger.info("上傳段資源密碼系統的帳號密碼登入完成");
		
		return pnpFtpSetting;
	}

	/**
	 * 取得ftp setting info
	 * 
	 * @return
	 */
	public PNPFtpSetting getFtpSettings(String source) {
		return ftpSettings.get(source);
	}
	
	

	/**
	 * 重新載入參數設定資料
	 */
	private Map<String, String> loadFtp(PNPFtpSetting ftpSetting) {
		Map<String, String> data = new HashMap<String, String>();
		logger.info("ftpSetting.getChannelId() : "+ftpSetting.getChannelId());
		logger.info("ftpSetting.getServerHostNamePort() : "+ftpSetting.getServerHostNamePort());
		logger.info("ftpSetting.getAPPCode() : "+ftpSetting.getAPPCode());
		logger.info("ftpSetting.getRESCode() : "+ftpSetting.getRESCode());
		logger.info("is64Bit : " + is64Bit);
		try {
			TrendPwMgmt lPwMgmt = new TrendPwMgmt(ftpSetting.getServerHostName(), ftpSetting.getServerHostNamePort(),
					ftpSetting.getAPPCode(), ftpSetting.getRESCode(), is64Bit);
			data.put("uid", StringUtils.trimToEmpty(lPwMgmt.getUserId()));
			data.put("pwd", StringUtils.trimToEmpty(lPwMgmt.getPassword()));
			logger.info("getHost:" + ftpSetting.getHost() + " / GetUID:" + data.get("uid") + " / GetPWD:"
					+ data.get("pwd"));
			return data;
		} catch (Exception e) {
			logger.error("TrendPwMgmt exception:" + e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 重新載入參數設定資料
	 */
	private Map<String, String> loadFtp(String host ,String serverHostName , int serverHostNamePort , String APPCode , String RESCode) {
		Map<String, String> data = new HashMap<String, String>();
		logger.info("host : "+host);
		logger.info("serverHostName : "+serverHostName);
		logger.info("serverHostNamePort : "+serverHostNamePort);
		logger.info("APPCode : "+APPCode);
		logger.info("RESCode : "+RESCode);
		logger.info("is64Bit : " + is64Bit);
		try {
			TrendPwMgmt lPwMgmt = new TrendPwMgmt(serverHostName, serverHostNamePort,
					APPCode, RESCode, is64Bit);
			data.put("uid", StringUtils.trimToEmpty(lPwMgmt.getUserId()));
			data.put("pwd", StringUtils.trimToEmpty(lPwMgmt.getPassword()));
			logger.info("Host:" + host + " / GetUID:" + data.get("uid") + " / GetPWD:"
					+ data.get("pwd"));
			return data;
		} catch (Exception e) {
			logger.error("TrendPwMgmt exception:" + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * 檢查ftpserver setting
	 * 
	 * @return
	 */
	private boolean validateDevFtpHostData(PNPFtpSetting setting) {
		if (setting.getPort() <= 0) {
			logger.error("ftp port not setting");
			return false;
		}

		if (StringUtils.isBlank(setting.getHost())) {
			logger.error("ftp setting error  host[" + setting.getHost() + "] ");
			return false;
		}
		if (StringUtils.isBlank(setting.getAccount()) || StringUtils.isBlank(setting.getPassword())) {
			logger.error(
					"ftp setting error  account[" + setting.getAccount() + "] password[" + setting.getPassword() + "]");
			return false;
		}

		if (StringUtils.isBlank(setting.getDownloadSavePath()) || StringUtils.isBlank(fileExtension)) {
			logger.error("ftp setting error  downloadSavePath[" + setting.getDownloadSavePath() + "] fileExtension[" + fileExtension
					+ "]");
			return false;
		}
		return true;
	}

	/**
	 * 檢查ftpserver setting
	 * 
	 * @return
	 */
	private boolean validateFtpHostData(PNPFtpSetting setting) {
//		if (setting.getPort() <= 0) {
//			logger.error("ftp port not setting");
//			return false;
//		}
//		if (StringUtils.isBlank(setting.getHost())) {
//			logger.error("ftp setting error  host[" + setting.getHost() + "] ");
//			return false;
//		}
//
//		if (StringUtils.isBlank(downloadSavePath) || StringUtils.isBlank(fileExtension)) {
//			logger.error("ftp setting error  downloadSavePath[" + downloadSavePath + "] fileExtension[" + fileExtension
//					+ "]");
//			return false;
//		}
		return true;
	}

	/**
	 * 根據setting 決定用FTP or FTPS
	 * 
	 * @param setting
	 * @return
	 */
	private FTPClient getFtpClient(PNPFtpSetting setting) {
		FTPClient fTPClient = new FTPClient();
		if (setting.getProtocol().equalsIgnoreCase("ftps")) {
			fTPClient = new FTPSClient(true);
		}
		return fTPClient;
	}

	/**
	 * 進行FTPClient Login
	 * 
	 * @param pFTPClient 要進行登入的FTPClient
	 * @param pType      FTPClient Type
	 * @return Login 狀態
	 * @throws FTPException FTP 連線異常
	 */
	private boolean loginFTP(FTPClient pFTPClient, PNPFtpSetting setting) throws Exception {
		try {

			pFTPClient.setDefaultTimeout(1000000);
			pFTPClient.connect(setting.getHost(), setting.getPort());

			int replyCode = pFTPClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {

			}
			// T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
			try {
				boolean lStatus = false;
				String account = setting.getAccount();
				String password = setting.getPassword();
				if ((account != null && account.length() > 0) && (password != null && password.length() > 0)) {
					lStatus = pFTPClient.login(account, password);
					if (lStatus) {
						logger.info("loginFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
					} else {
						logger.error("loginFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
								+ setting.getRESCode() + "]，重新載入FTP連線參數!");
					}
				}

				if (!lStatus) { // T 重新載入一次設定

					pFTPClient.disconnect();
					// T 重新取得FTP 參數設定值
					Map<String, String> trendPwMgmt = loadFtp(setting);
					logger.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" +  trendPwMgmt.get("pwd"));
					pFTPClient.connect(setting.getHost(), setting.getPort());
					lStatus = pFTPClient.login(trendPwMgmt.get("uid"), trendPwMgmt.get("pwd"));
					if (lStatus) {
						logger.info("loginFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
					} else {
						logger.error("loginFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
								+ setting.getRESCode() + "]，改以原系統記錄帳號密碼進行登入!");
					}

					// T 以系統預設密碼登入
					if (!lStatus) {
						lStatus = pFTPClient.login("ACCOUNT", "PASSWORD");
						if (lStatus) {
							logger.info("loginFTP:" +  setting.getHost() + "原系統記錄帳號密碼登入完成");
						} else {
							logger.error("loginFTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
						}
					}
					return lStatus;
				}
				return lStatus;
			} catch (Exception lIOE) {
				lIOE.printStackTrace();
				logger.error("loginFTP:" +  lIOE.getMessage());
			}
		} catch (Exception lSE) {
			lSE.printStackTrace();
			logger.error("loginFTP:" + lSE.getMessage()  );
		}
		return false;
	}
	
	/**
	 * SMS進行FTPClient Login
	 * 
	 * @param pFTPClient 要進行登入的FTPClient
	 * @param pType      FTPClient Type
	 * @return Login 狀態
	 * @throws FTPException FTP 連線異常
	 */
	private boolean smsLoginFTP(FTPClient pFTPClient, PNPFtpSetting setting) throws Exception {
		try {
			
			pFTPClient.setDefaultTimeout(1000000);
			pFTPClient.connect(setting.getHost(), setting.getPort());
			
			int replyCode = pFTPClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				
			}
			// T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
			try {
				boolean lStatus = false;
				String account = setting.getSmsAccount();
				String password = setting.getSmsPassword();
				if ((account != null && account.length() > 0) && (password != null && password.length() > 0)) {
					lStatus = pFTPClient.login(account, password);
					if (lStatus) {
						logger.info("smsLoginFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入完成");
					} else {
						logger.error("smsLoginFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getSmsAPPCode() + "] RES["
								+ setting.getSmsRESCode() + "]，重新載入FTP連線參數!");
					}
				}
				
				if (!lStatus) { // T 重新載入一次設定
					
					pFTPClient.disconnect();
					// T 重新取得FTP 參數設定值
					String host = setting.getSmsHost();
					String serverHostName = setting.getSmsServerHostName();
					int serverHostNamePort = setting.getSmsServerHostNamePort();
					String APPCode = setting.getSmsAPPCode();
					String RESCode = setting.getSmsRESCode();
					
					Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, APPCode, RESCode);
					logger.info("重新載入設定 smsLoginFTP:" + trendPwMgmt.get("uid") + " PWD:" +  trendPwMgmt.get("pwd"));
					pFTPClient.connect(setting.getSmsHost(), setting.getSmsPort());
					lStatus = pFTPClient.login(trendPwMgmt.get("uid"), trendPwMgmt.get("pwd"));
					if (lStatus) {
						logger.info("重新載入設定 loginFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入完成");
					} else {
						logger.error("loginFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getSmsAPPCode() + "] RES["
								+ setting.getSmsRESCode() + "]，改以原系統記錄帳號密碼進行登入!");
					}
					
					// T 以系統預設密碼登入
					if (!lStatus) {
						lStatus = pFTPClient.login("ACCOUNT", "PASSWORD");
						if (lStatus) {
							logger.info("smsLoginFTP:" +  setting.getHost() + "原系統記錄帳號密碼登入完成");
						} else {
							logger.error("smsLoginFTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
						}
					}
					return lStatus;
				}
				return lStatus;
			} catch (Exception lIOE) {
				lIOE.printStackTrace();
				logger.error("smsLoginFTP:" +  lIOE.getMessage());
			}
		} catch (Exception lSE) {
			lSE.printStackTrace();
			logger.error("smsLoginFTP:" + lSE.getMessage()  );
		}
		return false;
	}

	/**
	 * 進行SFTPClient Login
	 * 
	 * 
	 */
	private Session loginSFTP(PNPFtpSetting setting) throws Exception {
		boolean lStatus = false;
		Session session = null;
		try {
			JSch jsch = new JSch();
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");

			// T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
			String account = setting.getAccount();
			String password = setting.getPassword();
			if ((account != null && account.length() > 0) && (password != null && password.length() > 0)) {
				session = jsch.getSession(account, setting.getHost(), setting.getPort());
				session.setPassword(password);
				session.setConfig(sshConfig);
				session.connect();
				lStatus = session.isConnected();
				if (lStatus) {
					logger.info("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
				} else {
					logger.error("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
							+ setting.getRESCode() + "]，重新載入FTP連線參數!");
				}
			}

			if (!lStatus) { // T 重新載入一次設定
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				session = null;
				// T 重新取得FTP 參數設定值
				Map<String, String> trendPwMgmt = loadFtp(setting);
				logger.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" +  trendPwMgmt.get("pwd"));
				session = jsch.getSession(trendPwMgmt.get("uid"), setting.getHost(), setting.getPort());
				session.setPassword(trendPwMgmt.get("pwd"));
				session.setConfig(sshConfig);
				session.connect();
				lStatus = session.isConnected();
				if (lStatus) {
					logger.info("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
				} else {
					logger.error("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
							+ setting.getRESCode() + "]，改以原系統記錄帳號密碼進行登入!");
				}

				// T 以系統預設密碼登入
				if (!lStatus) {
					session = jsch.getSession("ACCOUNT", setting.getHost(), setting.getPort());
					session.setPassword("PASSWORD");
					session.setConfig(sshConfig);
					session.connect();
					lStatus = session.isConnected();
					if (lStatus) {
						logger.info("loginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入完成");
					} else {
						logger.error("loginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
					}
				}
			}
		} catch (Exception ex) {
			logger.error("loginSFTP Error: " + ex.getMessage());
			ex.printStackTrace();
		}

		if (lStatus) {
			return session;
		} else {
			session = null;
		}

		return null;
	}

	/**
	 * SMS 進行SFTPClient Login
	 * 
	 * 
	 */
	private Session smsLoginSFTP(PNPFtpSetting setting) throws Exception {
		boolean lStatus = false;
		Session session = null;
		try {
			JSch jsch = new JSch();
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");

			// T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
			String account = setting.getSmsAccount();
			String password = setting.getSmsPassword();
			if ((account != null && account.length() > 0) && (password != null && password.length() > 0)) {
				session = jsch.getSession(account, setting.getSmsHost(), setting.getSmsPort());
				session.setPassword(password);
				session.setConfig(sshConfig);
				session.connect();
				lStatus = session.isConnected();
				if (lStatus) {
					logger.info("SmsLoginSFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入完成");
				} else {
					logger.error("SmsloginSFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getSmsAPPCode() + "] RES["
							+ setting.getSmsRESCode() + "]，重新載入FTP連線參數!");
				}
			}

			if (!lStatus) { // T 重新載入一次設定
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				session = null;
				// T 重新取得FTP 參數設定值
				String host = setting.getSmsHost();
				String serverHostName = setting.getSmsServerHostName();
				int serverHostNamePort = setting.getSmsServerHostNamePort();
				String APPCode = setting.getSmsAPPCode();
				String RESCode = setting.getSmsRESCode();
				
				Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, APPCode, RESCode);
				logger.info("smsLoginSFTP:" + trendPwMgmt.get("uid") + " PWD:" +  trendPwMgmt.get("pwd"));
				session = jsch.getSession(trendPwMgmt.get("uid"), setting.getHost(), setting.getPort());
				session.setPassword(trendPwMgmt.get("pwd"));
				session.setConfig(sshConfig);
				session.connect();
				lStatus = session.isConnected();
				if (lStatus) {
					logger.info("重新取得參數 smsLoginSFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入完成");
				} else {
					logger.error("重新取得參數 smsLoginSFTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getSmsAPPCode() + "] RES["
							+ setting.getSmsRESCode() + "]，改以原系統記錄帳號密碼進行登入!");
				}

				// T 以系統預設密碼登入
				if (!lStatus) {
					session = jsch.getSession("ACCOUNT", setting.getSmsHost(), setting.getSmsPort());
					session.setPassword("PASSWORD");
					session.setConfig(sshConfig);
					session.connect();
					lStatus = session.isConnected();
					if (lStatus) {
						logger.info("smsLoginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入完成");
					} else {
						logger.error("smsLoginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
					}
				}
			}
		} catch (Exception ex) {
			logger.error("smsLoginSFTP Error: " + ex.getMessage());
			ex.printStackTrace();
		}

		if (lStatus) {
			return session;
		} else {
			session = null;
		}

		return null;
	}
	
	
	
	/**
	 * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	public Map<String, byte[]> downloadMutipleFileInFTPForDev(String pDirectory, String extension, PNPFtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		FTPClient FTPClient = getFtpClient(setting);
		
		/**
		 *  三竹來源  SOURCE_MITAKE = "1";
		 *	互動來源  SOURCE_EVERY8D = "2";
		 *	明宣來源  SOURCE_MING = "3";
		 *	UNICA來源   SOURCE_UNICA = "4";
		 */
		String source = setting.getChannelId();//判斷來源 
		
		try {
			FTPClient.connect(setting.getHost(), setting.getPort());
			FTPClient.login(setting.getAccount(), setting.getPassword());
			FTPClient.enterLocalPassiveMode();
			FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClient.setAutodetectUTF8(true);
			FTPClient.setControlEncoding(setting.getFileEncoding());
			FTPClient.changeWorkingDirectory(pDirectory);
			FTPClient.setStrictReplyParsing(true);// 新加設定解決org.apache.commons.net.MalformedServerReplyException: Truncated server reply: ).
			// 取得FTP中的files
			FTPFile[] files = FTPClient.listFiles();
			
			if(!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
				//三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
				for (FTPFile file : files) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt")) {
						logger.info("start rename!!!!");
						logger.info("fileName :"+fileName);
						logger.info("ftpClient.printWorkingDirectory() :"+FTPClient.printWorkingDirectory());
						logger.info(FTPClient.rename(fileName, fileName+".ok"));
					}
				}
				FTPFile[] filesOK = FTPClient.listFiles();
				for (FTPFile file : filesOK) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
						ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
						FTPClient.retrieveFile(fileName, lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(fileName, lDataTemp.toByteArray());
						lDataTemp.close();
					}
				}
				
			}else {
				for (FTPFile file : files) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
						fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
						ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
						FTPClient.retrieveFile(fileName, lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(fileName, lDataTemp.toByteArray());
						lDataTemp.close();
					}
				}
			}
			
		} catch (Exception ex) {
			logger.error("downloadMutipleFileInFTPForDev Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (FTPClient.isConnected()) {
					FTPClient.logout();
					FTPClient.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("downloadMutipleFileInFTPForDev Error: " + ex.getMessage());
			}
		}
		return lReturnDatas;
	}

	/**
	 * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) 根據類型指向不同FTP
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	public Map<String, byte[]> downloadMutipleFileByType(String source , String directory, String extension, PNPFtpSetting setting) {
		if (setting.getProtocol().equalsIgnoreCase("sftp")) {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				return downloadMutipleFileInSFTP(directory, extension, setting);
			} else {
				return downloadMutipleFileInSFTPForDev(directory, extension, setting);
			}
		} else {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				return downloadMutipleFileInFTP(directory, extension, setting);
			} else {
				return downloadMutipleFileInFTPForDev(directory, extension, setting);
			}
		}
	}

	/**
	 * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	private Map<String, byte[]> downloadMutipleFileInFTP(String pDirectory, String extension, PNPFtpSetting setting) {
		FTPClient FTPClient = null;
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		
		/**
		 *  三竹來源  SOURCE_MITAKE = "1";
		 *	互動來源  SOURCE_EVERY8D = "2";
		 *	明宣來源  SOURCE_MING = "3";
		 *	UNICA來源   SOURCE_UNICA = "4";
		 */
		String source = setting.getChannelId();//判斷來源 
		
		
		try {
			FTPClient = getFtpClient(setting);
			if (loginFTP(FTPClient, setting)) {
				FTPClient.changeWorkingDirectory(pDirectory);
				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				FTPFile[] lFiles = FTPClient.listFiles();
				logger.info("downloadMutipleFileInFTP:" + pDirectory + " File size:" + lFiles.length);
				Map<String, Long> lFileSize = new HashMap<String, Long>();
				if(!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
					//三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
					for (FTPFile file : lFiles) {
						String fileName = file.getName();
						if (!file.isDirectory() && fileName.toUpperCase().endsWith("TXT")) {
							logger.info("start rename!!!!");
							logger.info("fileName :"+fileName);
							logger.info("ftpClient.printWorkingDirectory() :"+FTPClient.printWorkingDirectory());
							logger.info(FTPClient.rename(fileName, fileName+".ok"));
						}
					}
					FTPFile[] filesOK = FTPClient.listFiles();
					for (FTPFile file : filesOK) {
						String fileName = file.getName();
						if (!file.isDirectory() && fileName.toUpperCase().endsWith("TXT.OK")) {
							ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
							FTPClient.retrieveFile(fileName, lDataTemp);
							lDataTemp.flush();
							lReturnDatas.put(fileName, lDataTemp.toByteArray());
							lDataTemp.close();
						}
					}
					
					//檢查檔案大小確定是否上傳完畢版本先保留
//					for (FTPFile lFtpFile : lFiles) {
//						String fileName = lFtpFile.getName();
//						logger.info("downloadMutipleFileInFTP:" + pDirectory + " File :" + fileName);
//						if (!lFtpFile.isDirectory() && fileName.endsWith(extension)) {
//							lFileSize.put(lFtpFile.getName(), lFtpFile.getSize());
//						}
//					}
//					// T 隔五秒再重新查詢FTP ，判斷兩者的長度
//					try {
//						BigDecimal lWaitingTime = new BigDecimal("5");
//						if (lWaitingTime.intValue() <= 0) {
//							lWaitingTime = new BigDecimal("5");
//						}
//						Thread.sleep(lWaitingTime.intValue());
//					} catch (InterruptedException lIE) {
//						lIE.printStackTrace();
//					}
//					lFiles = FTPClient.listFiles();
//					ByteArrayOutputStream lDataTemp = null;
//					for (FTPFile lFtpFile : lFiles) {
//						// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
//						if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
//								&& lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
//							lDataTemp = new ByteArrayOutputStream();
//							FTPClient.retrieveFile(lFtpFile.getName(), lDataTemp);
//							lDataTemp.flush();
//							lReturnDatas.put(lFtpFile.getName(), lDataTemp.toByteArray());
//							lDataTemp.close();
//
//						}
//					}
					
				}else {
					ByteArrayOutputStream lDataTemp = null;
					for (FTPFile lFtpFile : lFiles) {
						String fileName = lFtpFile.getName();
						logger.info("downloadMutipleFileInFTP:" + pDirectory + " File :" + fileName);
						if (!lFtpFile.isDirectory() && fileName.toUpperCase().endsWith(extension.toUpperCase()+".OK")) {
							lDataTemp = new ByteArrayOutputStream();
							fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
							FTPClient.retrieveFile(fileName, lDataTemp);
							lDataTemp.flush();
							lReturnDatas.put(lFtpFile.getName(), lDataTemp.toByteArray());
							lDataTemp.close();
						}
					}
				}
				
				return lReturnDatas;
			}
		} catch (Exception e) {
			try {
				if (FTPClient != null) {
					FTPClient.disconnect();
				}
			} catch (Exception lEXP) {
				lEXP.printStackTrace();
				logger.error("downloadMutipleFileInFTP Exception: " + lEXP.getMessage());
			}
			logger.error(e);
		} finally {
			try {
				if (FTPClient != null) {
					FTPClient.disconnect();
				}
			} catch (Exception lEXP) {
				lEXP.printStackTrace();
				logger.error("downloadMutipleFileInFTP Exception: " + lEXP.getMessage());
			}
		}
		return lReturnDatas;
	}
	
	/**
	 * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	private Map<String, byte[]> downloadMutipleFileInSFTP(String pDirectory, String extension, PNPFtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		ChannelSftp channelSftp = null;
		Session session = null;
		/**
		 *  三竹來源  SOURCE_MITAKE = "1";
		 *	互動來源  SOURCE_EVERY8D = "2";
		 *	明宣來源  SOURCE_MING = "3";
		 *	UNICA來源   SOURCE_UNICA = "4";
		 */
		String source = setting.getChannelId();//判斷來源 
		
		try {
			session = this.loginSFTP(setting);
			if (session == null) {
				logger.error("downloadMutipleFileInSFTP session null");
				return lReturnDatas;
			}
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			if (channelSftp.isConnected()) {
				channelSftp.cd(pDirectory);
				// 取得FTP中的files
				if(!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
					//三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
					Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
					for (ChannelSftp.LsEntry lFtpFile : list) {
						logger.info("sftp start rename!!!!");
						String fileName = lFtpFile.getFilename();
						logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
						channelSftp.rename(fileName, fileName+".ok");
					}
					
					Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension+".ok");
					for (ChannelSftp.LsEntry lFtpFile : list) {
						String fileName = lFtpFile.getFilename();
						logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
						ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
						channelSftp.get(fileName, lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(fileName, lDataTemp.toByteArray());
						lDataTemp.close();
					}
					
				}else {
					Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension+".ok");
					for (ChannelSftp.LsEntry lFtpFile : listOk) {
						String fileName = lFtpFile.getFilename();
						fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
						logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
						ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
						channelSftp.get(fileName, lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(fileName, lDataTemp.toByteArray());
						lDataTemp.close();
					}
				}
				
				//檢查檔案大小確認檔案是否完成上傳版本
//				Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
//				Map<String, Long> lFileSizeAndName = new HashMap<String, Long>();
//				for (ChannelSftp.LsEntry lFtpFile : list) {
//					String fileName = lFtpFile.getFilename();
//					logger.info("downloadMutipleFileInSFTP:" + pDirectory + " File :" + fileName);
//					lFileSizeAndName.put(lFtpFile.getFilename(), lFtpFile.getAttrs().getSize());
//				}
//				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
//				try {
//					BigDecimal lWaitingTime = new BigDecimal("5");
//					if (lWaitingTime.intValue() <= 0) {
//						lWaitingTime = new BigDecimal("5");
//					}
//					Thread.sleep(lWaitingTime.intValue());
//				} catch (InterruptedException lIE) {
//					lIE.printStackTrace();
//				}
//				list = channelSftp.ls("*." + extension);
//				ByteArrayOutputStream lDataTemp = null;
//				for (ChannelSftp.LsEntry lFtpFile : list) {
//					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
//					if (lFileSizeAndName.containsKey(lFtpFile.getFilename())
//							&& lFtpFile.getAttrs().getSize() == lFileSizeAndName.get(lFtpFile.getFilename())) {
//						lDataTemp = new ByteArrayOutputStream();
//						channelSftp.get(lFtpFile.getFilename(), lDataTemp);
//						lDataTemp.flush();
//						lReturnDatas.put(lFtpFile.getFilename(), lDataTemp.toByteArray());
//						lDataTemp.close();
//					}
//				}
			}else {
				logger.error("downloadMutipleFileInSFTP channelSftp false");
			}
			

		} catch (Exception ex) {
			logger.error("downloadMutipleFileInSFTP Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
				}
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				channelSftp = null;
				session = null;
			} catch (Exception ex) {
				logger.error("downloadMutipleFileInSFTP Error: " + ex.getMessage());
			}
		}
		
		return lReturnDatas;
	}

	/**
	 * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	public Map<String, byte[]> downloadMutipleFileInSFTPForDev(String pDirectory, String extension,
			PNPFtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		ChannelSftp channelSftp = null;
		Session session = null;
		
		/**
		 *  三竹來源  SOURCE_MITAKE = "1";
		 *	互動來源  SOURCE_EVERY8D = "2";
		 *	明宣來源  SOURCE_MING = "3";
		 *	UNICA來源   SOURCE_UNICA = "4";
		 */
		String source = setting.getChannelId();//判斷來源 
		
		
		try {
			JSch jsch = new JSch();
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session = jsch.getSession(setting.getAccount(), setting.getHost(), setting.getPort());
			session.setPassword(setting.getPassword());
			session.setConfig(sshConfig);
			session.connect();
			if (session.isConnected()) {
				channelSftp = (ChannelSftp) session.openChannel("sftp");
				channelSftp.connect();
				if (channelSftp.isConnected()) {
					channelSftp.cd(pDirectory);
					// 取得FTP中的files
					if(!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
						//三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
						Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
						for (ChannelSftp.LsEntry lFtpFile : list) {
							logger.info("sftp start rename!!!!");
							String fileName = lFtpFile.getFilename();
							logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
							channelSftp.rename(fileName, fileName+".ok");
						}
						
						Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension+".ok");
						for (ChannelSftp.LsEntry lFtpFile : list) {
							String fileName = lFtpFile.getFilename();
							logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
							ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
							channelSftp.get(fileName, lDataTemp);
							lDataTemp.flush();
							lReturnDatas.put(fileName, lDataTemp.toByteArray());
							lDataTemp.close();
						}
						
					}else {
						Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension+".ok");
						for (ChannelSftp.LsEntry lFtpFile : listOk) {
							String fileName = lFtpFile.getFilename();
							fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
							logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
							ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
							channelSftp.get(fileName, lDataTemp);
							lDataTemp.flush();
							lReturnDatas.put(fileName, lDataTemp.toByteArray());
							lDataTemp.close();
						}
					}
				}else {
					logger.error(" downloadMutipleFileInSFTPForDev channelSftp isConnected faile ");
				}
				
			}else {
				logger.error(" downloadMutipleFileInSFTPForDev session isConnected faile ");
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(" downloadMutipleFileInSFTPForDev Error: " + ex.getMessage());
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
				}
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				channelSftp = null;
				session = null;
			} catch (Exception ex) {
				logger.error(" downloadMutipleFileInSFTPForDev Error: " + ex.getMessage());
			}
		}
		return lReturnDatas;
	}

	/**
	 * 刪除指定檔案 根據類型指向不同FTP
	 * 
	 * @param directory  原始目錄
	 * @param pFileNames 要刪除的檔案名稱
	 * @param pFtpConfig FTP Config
	 */
	public void deleteFileByType(String directory, String[] pFileNames, PNPFtpSetting setting) {
		if (setting.getProtocol().equalsIgnoreCase("sftp")) {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				deleteFileInSFTP(directory, pFileNames, setting);
			} else {
				deleteFileInSFTPForDev(directory, pFileNames, setting);
			}
		} else {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				deleteFileInFTP(directory, pFileNames, setting);
			} else {
				deleteFileInFTPForDev(directory, pFileNames, setting);
			}
		}
	}

	/**
	 * 刪除指定檔案
	 * 
	 * @param pDirectory 原始目錄
	 * @param pFileNames 要刪除的檔案名稱
	 * @param pFtpConfig FTP Config
	 */
	private void deleteFileInSFTP(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
		String source = setting.getChannelId();
		ChannelSftp channelSftp = null;
		Session session = null;
		try {
			session = loginSFTP(setting);
			if (session == null) {
				logger.error("deleteFileInSFTP connection failed");
				return;
			}
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			if (channelSftp.isConnected()) {
				channelSftp.cd(pDirectory);
				for (String lFileName : pFileNames) {
					channelSftp.rm(lFileName);
				}
				if(source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D)||source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
					for (String lFileName : pFileNames) {
						channelSftp.rm(lFileName+".ok");
					}
				}
			}else {
				logger.error("deleteFileInSFTP channelSftp: " + channelSftp.isConnected());
			}

		} catch (Exception ex) {
			logger.error("deleteFileInSFTP Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
				}
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				channelSftp = null;
				session = null;
			} catch (Exception ex) {
				logger.error("deleteFileInSFTP Error: " + ex.getMessage());
			}
		}
	}

	/**
	 * 刪除SFTP指定檔案 (測試環境用)
	 * 
	 * @param pDirectory 原始目錄
	 * @param pFileNames 要刪除的檔案名稱
	 * @param pFtpConfig FTP Config
	 */
	private void deleteFileInSFTPForDev(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
		String source = setting.getChannelId();
		ChannelSftp channelSftp = null;
		Session session = null;
		try {
			JSch jsch = new JSch();
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session = jsch.getSession(setting.getAccount(), setting.getHost(), setting.getPort());
			session.setPassword(setting.getPassword());
			session.setConfig(sshConfig);
			session.connect();
			if (session.isConnected()) {
				channelSftp = (ChannelSftp) session.openChannel("sftp");
				channelSftp.connect();
				if (channelSftp.isConnected()) {
					channelSftp.cd(pDirectory);
					for (String lFileName : pFileNames) {
						channelSftp.rm(lFileName);
					}
					if(source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D)||source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
						for (String lFileName : pFileNames) {
							channelSftp.rm(lFileName+".ok");
						}
					}
				}else {
					logger.error("deleteFileInSFTPForDev channelSftp: " + channelSftp.isConnected());
				}
			}else {
				logger.error("deleteFileInSFTPForDev session: " + session.isConnected());
			}

		} catch (Exception ex) {
			logger.error("deleteFileInSFTPForDev Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
				}
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				channelSftp = null;
				session = null;
			} catch (Exception ex) {
				logger.error("deleteFileInSFTPForDev Error: " + ex.getMessage());
			}
		}
	}

	/**
	 * 刪除FTP/FTPS指定檔案 (測試環境用)
	 * 
	 * @param pDirectory 原始目錄
	 * @param pFileNames 要刪除的檔案名稱
	 * @param pFtpConfig FTP Config
	 */
	private void deleteFileInFTPForDev(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
		String source = setting.getChannelId();
		FTPClient FTPClient = getFtpClient(setting);
		try {
			FTPClient.connect(setting.getHost(), setting.getPort());
			FTPClient.login(setting.getAccount(), setting.getPassword());
			FTPClient.enterLocalPassiveMode();
			FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClient.setAutodetectUTF8(true);
			FTPClient.setControlEncoding(setting.getFileEncoding());
			FTPClient.changeWorkingDirectory(pDirectory);
			for (String procfileName : pFileNames) {
				boolean success = false; 
				success = FTPClient.deleteFile(procfileName);
				if (!success) {
					logger.error("remove fail: " + procfileName);
				}
				if(source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D)||source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
					success = FTPClient.deleteFile(procfileName+".ok");
					if (!success) {
						logger.error("remove fail: " + procfileName+".ok");
					}
				}
			}
		} catch (Exception ex) {
			logger.error("deleteFileInFTPForDev Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (FTPClient.isConnected()) {
					FTPClient.logout();
					FTPClient.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("deleteFileInFTPForDev Error: " + ex.getMessage());
			}
		}
	}

	/**
	 * 刪除指定檔案
	 * 
	 * @param pDirectory 原始目錄
	 * @param pFileNames 要刪除的檔案名稱
	 * @param pFtpConfig FTP Config
	 */
	private void deleteFileInFTP(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
		String source = setting.getChannelId();
		FTPClient FTPClient = null;
		try {
			FTPClient = getFtpClient(setting);
			if (loginFTP(FTPClient, setting)) {
				FTPClient.changeWorkingDirectory(pDirectory);

				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				for (String procfileName : pFileNames) {
					boolean success = false; 
					success = FTPClient.deleteFile(procfileName);
					if (!success) {
						logger.error("remove fail: " + procfileName);
					}
					if(source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D)||source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
						success = FTPClient.deleteFile(procfileName+".ok");
						if (!success) {
							logger.error("remove fail: " + procfileName+".ok");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("deleteFileInFTP Exception" + e.getMessage());
			try {
				if (FTPClient != null) {
					FTPClient.disconnect();
				}
			} catch (Exception lEXP) {
				logger.error("deleteFileInFTP disconnect Exception" + lEXP.getMessage());
			}

		} finally {
			try {
				if (FTPClient != null) {
					FTPClient.disconnect();
				}
			} catch (Exception lEXP) {
				logger.error("deleteFileInFTP Exception" + lEXP.getMessage());
			}
		}
	}

	
	/**
	 * 上傳指定檔案 根據類型指向不同FTP
	 * 
	 * @param uploadIs   上傳檔案
	 * @param directory  上傳目錄
	 * @param pFileNames 要上傳的檔案名稱
	 * @param pFtpConfig FTP Config
	 * @throws IOException 
	 */
	public void uploadFileByType(InputStream uploadIs,String fileName,String targetDir,PNPFtpSetting setting) throws IOException {
		if (setting.getProtocol().equalsIgnoreCase("sftp")) {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				uploadFileInSFTP(uploadIs,fileName,targetDir, setting);
			} else {
				uploadFileInSFTP(uploadIs,fileName,targetDir, setting);
			}
		} else {
			if (!CoreConfigReader.isPNPFtpTypeDevelop()) { // 正式環境時使用
				uploadFileInFTP(uploadIs,fileName,targetDir, setting);
			} else {
				uploadFileInFTP(uploadIs,fileName,targetDir, setting);
			}
		}
	}
	
	public void uploadFileInFTP(InputStream targetStream,String fileName,String targetDir,PNPFtpSetting setting) throws IOException {
		logger.info("start uploadFileInFTP ");
		
		logger.info(" fileName...."+fileName);
		
		FTPClient FTPClient = new FTPClient();
		try {
			FTPClient.connect(setting.getSmsHost(), setting.getSmsPort());
			logger.info("loginFTP : " + (smsLoginFTP(FTPClient, setting) ? "OK" : "fail"));
			FTPClient.enterLocalPassiveMode();
			FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClient.setAutodetectUTF8(true);
			FTPClient.setControlEncoding(setting.getFileEncoding());
			FTPClient.changeWorkingDirectory(targetDir);
			
            //上傳檔案
            FTPClient.storeFile(fileName, targetStream);

            //關閉檔案
            targetStream.close();
		}catch (Exception e) {
			logger.error("uploadFileInFTP Exception" + e.getMessage());
			e.printStackTrace();
		}finally {
            if (FTPClient != null) {
                //登出
            	FTPClient.logout();
                //關閉連線
            	FTPClient.disconnect();
            }
        }
	}
	
	public void uploadFileInSFTP(InputStream uploadIs,String fileName,String targetDir,PNPFtpSetting setting) {
		logger.info("start uploadFileInSFTP ");
		ChannelSftp channelSftp = null;
		Session session = null;
		try {
			session = smsLoginSFTP(setting);
			if (session == null) {
				logger.error("uploadFileInSFTP connection failed");
				return;
			}
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			if (channelSftp.isConnected()) {
		        try {
		            Vector content = channelSftp.ls(targetDir);
		            if(content == null){
		                this.mkdir(targetDir,channelSftp);
		            }
		        }catch (SftpException e) {
			        this.mkdir(targetDir,channelSftp);
			    }
		        channelSftp.cd(targetDir);
	            channelSftp.put(uploadIs, new String(fileName.getBytes(),setting.getFileEncoding()));
			}else {
				logger.error("uploadFileInSFTP channelSftp: " + channelSftp.isConnected());
			}
		
		}catch (Exception e) {
			logger.error("uploadFileInSFTP Exception" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public boolean mkdir(String path , ChannelSftp sftpChannel) {
	    try {
	        if(path.startsWith("/")) {
	            if(!isExists(path,sftpChannel)) {
	                String[] arr = path.substring(1).split("/");
	                
	                StringBuffer sb = new StringBuffer();
	                for(int i = 0; i < arr.length; i++) {
	                    sb.append("/"+ arr[i]);
	                    if(!isExists(sb.toString(),sftpChannel)) {
	                        sftpChannel.mkdir(sb.toString());
	                    }
	                }
	            }
	            
	            return true;
	        }
        } catch (SftpException e) {}
	    
	    return false; 
	}
	
	private boolean isExists(String path, ChannelSftp sftpChannel) {
	    try {
            if(sftpChannel.ls(path) != null) {
                return true;
            }
        } catch (SftpException e) {}
	    
	    return false;
	}
	
// 以下台新提供的Sample code 目前沒用到 暫留註記
//	/**
//	 * 查詢有效下載檔案清單
//	 * 
//	 * @param pDirectory 要下載檔案所在路徑
//	 * @param pFtpConfig FTP Config
//	 * @return 可供下載的檔案名稱
//	 */
//	public Map<String, Long> getFileName(String pDirectory, FtpSetting setting) {
//		FTPClient FTPClient = null;
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(pDirectory);
//
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				FTPFile[] lFiles = FTPClient.listFiles();
//				Map<String, Long> lFileSize = new HashMap<String, Long>();
//				for (FTPFile lFtpFile : lFiles) {
//					lFileSize.put(lFtpFile.getName(), lFtpFile.getSize());
//				}
//				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException lIE) {
//					lIE.printStackTrace();
//				}
//				lFiles = FTPClient.listFiles();
//				// T 有效可下載的檔案資料
//				Map<String, Long> lFileNames = new HashMap<String, Long>();
//				for (FTPFile lFtpFile : lFiles) {
//					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
//					if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
//							&& lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
//						lFileNames.put(lFtpFile.getName(), lFtpFile.getSize());
//					}
//				}
//				return lFileNames;
//			}
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//		}
//		return new HashMap<String, Long>();
//	}

//	public List<String> listFileName(String pDirectory, FtpSetting setting) {
//		FTPClient FTPClient = null;
//		List<String> lReturnDatas = new ArrayList<String>();
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(pDirectory);
//
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				FTPFile[] lFiles = FTPClient.listFiles();
//				Map<String, Long> lFileSize = new HashMap<String, Long>();
//				for (FTPFile lFtpFile : lFiles) {
//					lFileSize.put(lFtpFile.getName(), lFtpFile.getSize());
//				}
//				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException lIE) {
//					lIE.printStackTrace();
//				}
//				lFiles = FTPClient.listFiles();
//				for (FTPFile lFtpFile : lFiles) {
//					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
//					if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
//							&& lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
//						lReturnDatas.add(lFtpFile.getName());
//					}
//				}
//				return lReturnDatas;
//			}
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//		}
//		return lReturnDatas;
//	}

//	/**
//	 * 單檔下傳
//	 * 
//	 * @param directory        指定Server端FTP特定目錄(e.g "/","/20081031/demo")
//	 * @param fileNameInServer Server端要下傳的檔名(e.g. "downloadFromServer.txt")
//	 * @return 下傳的File Object
//	 * 
//	 * @throws 異常時以RuntimeException拋出，並自動中斷FTP連線
//	 */
//	public byte[] downloadSingleFile(String directory, String pFileName, FtpSetting setting) {
//		FTPClient FTPClient = null;
//		byte[] lData = null;
//		try {
//			FTPClient = getFtpClient(setting);
//			ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(directory);
//
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				FTPClient.retrieveFile(pFileName, lDataTemp);
//				lDataTemp.flush();
//				lData = lDataTemp.toByteArray();
//			}
//			return lData;
//		} catch (Exception e) {
//			logger.error("downloadFile :" + e.getMessage());
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//			throw new RuntimeException(e);
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//		}
//	}

//	/**
//	 * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
//	 * 
//	 * @param pDirectory 要下載檔案所在路徑
//	 * @param pFileCount 要下載的檔案數
//	 * @param pFtpType   Ftp Server Type
//	 * @return 下載的檔案內容
//	 */
//	private Map<String, byte[]> downloadMutipleFile(String pDirectory, int pFileCount, FtpSetting setting) {
//		FTPClient FTPClient = null;
//		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(pDirectory);
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				FTPFile[] lFiles = FTPClient.listFiles();
//				Map<String, Long> lFileSize = new HashMap<String, Long>();
//				for (FTPFile lFtpFile : lFiles) {
//					lFileSize.put(lFtpFile.getName(), lFtpFile.getSize());
//				}
//				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
//				try {
//					BigDecimal lWaitingTime = new BigDecimal("5");
//					if (lWaitingTime.intValue() <= 0) {
//						lWaitingTime = new BigDecimal("5");
//					}
//					Thread.sleep(lWaitingTime.intValue());
//				} catch (InterruptedException lIE) {
//					lIE.printStackTrace();
//				}
//				lFiles = FTPClient.listFiles();
//				ByteArrayOutputStream lDataTemp = null;
//				int lFileIndex = 0;
//				for (FTPFile lFtpFile : lFiles) {
//					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
//					if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
//							&& lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
//						lDataTemp = new ByteArrayOutputStream();
//						FTPClient.retrieveFile(lFtpFile.getName(), lDataTemp);
//						lDataTemp.flush();
//						lReturnDatas.put(lFtpFile.getName(), lDataTemp.toByteArray());
//						if (++lFileIndex >= pFileCount) {
//							break;
//						}
//					}
//				}
//				return lReturnDatas;
//			}
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//		}
//		return lReturnDatas;
//	}
//
//	/**
//	 * 單檔下傳
//	 * 
//	 * @param directory        指定Server端FTP特定目錄(e.g "/","/20081031/demo")
//	 * @param fileNameInServer Server端要下傳的檔名(e.g. "downloadFromServer.txt")
//	 * @param fileNameInClient Client端要產生的實體下傳檔案完整路徑(e.g.
//	 *                         "c:/demo/downloadToClient.txt")
//	 * @param pFtpConfig       FTP Config
//	 * @return 下傳的File Object
//	 * 
//	 * @throws 異常時以RuntimeException拋出，並自動中斷FTP連線
//	 */
//	public File downloadFile(String directory, String fileNameInServer, String fileNameInClient, FtpSetting setting) {
//		File returnFile = null;
//		FTPClient FTPClient = null;
//
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(directory);
//
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//				returnFile = new File(fileNameInClient);
//				FTPClient.retrieveFile(fileNameInServer, new FileOutputStream(returnFile));
//			}
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//			throw new RuntimeException(e);
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//		}
//		return returnFile;
//	}

//	/**
//	 * 從FTP伺服器上下載指定名稱的檔案
//	 * 
//	 * @param pDirectory 要下載檔案所在路徑
//	 * @param pFileNames 要下載的檔案名稱
//	 * @param pFileCount 要下載的檔案數
//	 * @param pFtpConfig FTP Config
//	 * @return 下載的檔案內容
//	 */
//	public Map<String, byte[]> downloadFileWithoutCheck(String pDirectory, Collection<String> pFileNames,
//			FtpSetting setting) {
//		FTPClient FTPClient = null;
//		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(pDirectory);
//
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				ByteArrayOutputStream lDataTemp = null;
//				for (String lFileName : pFileNames) {
//					lDataTemp = new ByteArrayOutputStream();
//					FTPClient.retrieveFile(lFileName, lDataTemp);
//					lDataTemp.flush();
//					lReturnDatas.put(lFileName, lDataTemp.toByteArray());
//				}
//				return lReturnDatas;
//			}
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//
//			}
//		}
//		return lReturnDatas;
//	}
//
//	/**
//	 * 列出當前FTP SERVER 上的目錄結構
//	 * 
//	 * @param pFTPClient Ftp Client
//	 * @return pResult 測試結果
//	 * @throws Java 例外
//	 */
//	private static String printFtpStructure(FTPClient pFTPClient) throws Exception {
//		StringBuffer lResult = new StringBuffer();
//		try {
//			pFTPClient.changeWorkingDirectory("");
//			FTPFile[] lFiles = pFTPClient.listFiles();
//			for (FTPFile lFtpFile : lFiles) {
//				if (lFtpFile.isDirectory()) {
//					lResult.append("<BR>/" + printFtpFold(pFTPClient, lFtpFile));
//					pFTPClient.changeWorkingDirectory("../");
//				} else {
//					// T 要顯示檔案可以解除以下註解，但要小心因檔案過多造成的速度緩慢的問題
//					/*
//					 * if("2".equals(SystemProperties.getProperty("TEST_FTP_FILE_MODE"))){
//					 * lResult.append("<BR>/ 檔案 :"+lFtpFile.getName()); }
//					 */
//				}
//			}
//			return lResult.toString();
//		} catch (IOException lIOE) {
//			lResult.append(lIOE.getMessage());
//		}
//		return lResult.toString();
//	}
//
//	/**
//	 * 列印FTP SERVER 上的目錄結構
//	 * 
//	 * @param pFTPClient FTP CLIENT
//	 * @param pFtpFile   FTP FILE
//	 * @return 目錄資訊
//	 * @throws IOException Java IO Exception
//	 */
//	private static String printFtpFold(FTPClient pFTPClient, FTPFile pFtpFile) throws IOException {
//		StringBuffer lResult = new StringBuffer();
//		lResult.append(pFtpFile.getName());
//		pFTPClient.changeWorkingDirectory(pFtpFile.getName());
//		FTPFile[] lFiles = pFTPClient.listFiles();
//		for (FTPFile lFtpFile : lFiles) {
//			if (lFtpFile.isDirectory()) {
//				lResult.append(" <BR>" + pFtpFile.getName() + " 的次目錄 " + printFtpFold(pFTPClient, lFtpFile));
//			} else {
//				// T 要顯示檔案可以解除以下註解，但要小心因檔案過多造成的速度緩慢的問題
//				/*
//				 * if("2".equals(SystemProperties.getProperty("TEST_FTP_FILE_MODE"))){
//				 * lResult.append(" <BR>檔案:"+lFtpFile.getName()); }
//				 */
//			}
//		}
//		return lResult.toString();
//	}

//	/**
//	 * 單檔上傳
//	 * 
//	 * @param directory 指定Server端FTP特定目錄(e.g "/","/20081031/demo")
//	 * @param fileName  包含Client端實體路徑的完整檔名(e.g "c:/test/upload1.txt")
//	 * @param pFtpTpye  Ftp Server Type
//	 * @return 上傳成功或失敗
//	 * 
//	 * @throws 異常時以RuntimeException拋出，並自動中斷FTP連線
//	 */
//	public boolean uploadFile(String directory, String fileName, FtpSetting setting) {
//		List<String> fileNameList = new ArrayList<String>();
//		fileNameList.add(fileName);
//
//		return uploadFiles(directory, fileNameList, setting);
//	}
//
//	/**
//	 * 上傳檔案
//	 * 
//	 * @param pFileName    檔名
//	 * @param pFileContent 檔案InputStream
//	 * @param pFtpConfig   FTP Config
//	 * @return 上傳結果
//	 * @throws FTPException FTP Exception
//	 */
//	public boolean uploadFile(String pFileName, InputStream pFileContent, FtpSetting setting) throws Exception {
//		Map<String, InputStream> lCondition = new HashMap<String, InputStream>();
//		lCondition.put(pFileName, pFileContent);
//		return uploadFile(lCondition, setting).get(pFileName);
//	}
//
//	/**
//	 * 上傳檔案
//	 * 
//	 * @param pFileContents 檔案內容List<檔名，檔案InputStream>
//	 * @param pFtpConfig    FTP Config
//	 * @return 上傳結果
//	 * @throws FTPException FTP Exception
//	 */
//	public Map<String, Boolean> uploadFile(Map<String, InputStream> pFileContents, FtpSetting setting)
//			throws Exception {
//		return uploadFile("", pFileContents, setting);
//	}

//	/**
//	 * 上傳檔案
//	 * 
//	 * @param pDirectory    指定上傳目錄
//	 * @param pFileContents 檔案內容List<檔名，檔案InputStream>
//	 * @param pFtpConfig    FTP Config
//	 * @return 上傳結果
//	 * @throws FTPException FTP Exception
//	 */
//	public Map<String, Boolean> uploadByteArray(String pDirectory, Map<String, byte[]> pFileContents,
//			FtpSetting setting) throws Exception {
//		Map<String, InputStream> lResults = new HashMap<String, InputStream>();
//		for (String lFileName : pFileContents.keySet()) {
//			lResults.put(lFileName, new ByteArrayInputStream(pFileContents.get(lFileName)));
//		}
//		return uploadFile(pDirectory, lResults, setting);
//	}
//
//	/**
//	 * 上傳檔案
//	 * 
//	 * @param pFtpDirectory FTP Server 分類資料夾
//	 * @param pFileContents 檔案內容List<檔名，檔案InputStream>
//	 * @param pFtpConfig    FTP Config
//	 * @return 上傳結果
//	 * @throws FTPException FTP Exception
//	 */
//	public Map<String, Boolean> uploadFile(String pFtpDirectory, Map<String, InputStream> pFileContents,
//			FtpSetting setting) throws Exception {
//		FTPClient FTPClient = null;
//		Map<String, Boolean> lResult = new HashMap<String, Boolean>();
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(pFtpDirectory);
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				for (String lFileName : pFileContents.keySet()) {
//					if (pFileContents.get(lFileName) == null) {
//						lResult.put(lFileName, Boolean.FALSE);
//					} else {
//						lResult.put(lFileName, FTPClient.storeFile(lFileName, pFileContents.get(lFileName)));
//					}
//				}
//			}
//			return lResult;
//		} catch (Exception lSE) {
//
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//		}
//		return lResult;
//	}
//
//	/**
//	 * 多檔上傳
//	 * 
//	 * @param directory    指定Server端FTP特定目錄(e.g "/","/20081031/demo")
//	 * @param fileNameList 包含Client端實體路徑的完整檔名清單(e.g
//	 *                     包含"c:/test/upload1.txt","c:/test/upload2.txt"二字串的List)
//	 * @param pFtpConfig   FTP Config
//	 * @return 上傳成功或失敗
//	 * 
//	 * @throws 異常時以RuntimeException拋出，並自動中斷FTP連線
//	 */
//	public boolean uploadFiles(String directory, List<String> fileNameList, FtpSetting setting) {
//		boolean isUploadSuccess = false;
//		FTPClient FTPClient = null;
//
//		try {
//			FTPClient = getFtpClient(setting);
//			if (login(FTPClient, setting)) {
//				FTPClient.changeWorkingDirectory(directory);
//				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
//				for (String fileName : fileNameList) {
//					File uploadFile = new File(fileName);
//					FTPClient.storeFile(uploadFile.getName(), new FileInputStream(uploadFile));
//				}
//			}
//			isUploadSuccess = true;
//		} catch (Exception e) {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//			throw new RuntimeException(e);
//		} finally {
//			try {
//				logout(FTPClient);
//			} catch (Exception lEXP) {
//			}
//		}
//		return isUploadSuccess;
//	}

//	/**
//	 * 測試FTP 連線
//	 * 
//	 * @param pFtpConfig FTP Config
//	 */
//	public String testFtpConnection(FtpSetting setting) {
//		FTPClient lFTPClient = null;
//		StringBuffer lResult = new StringBuffer();
//		try {
//			lFTPClient = getFtpClient(setting);
//			if (login(lFTPClient, setting)) {
//				lResult.append(printFtpStructure(lFTPClient));
//				lResult.insert(0, "連線正常，帳號:" + setting.getAccount() + "(資)，，檔案結構如下:");
//				return lResult.toString();
//			}
//		} catch (Exception lEXP) {
//			lResult.append("測試連線異常，");
//			lResult.append(lEXP.getMessage());
//		} finally {
//			try {
//				lFTPClient.logout();
//				lFTPClient.disconnect();
//			} catch (Exception lEXP) {
//				lEXP.printStackTrace();
//			}
//		}
//		return lResult.toString();
//	}

}
