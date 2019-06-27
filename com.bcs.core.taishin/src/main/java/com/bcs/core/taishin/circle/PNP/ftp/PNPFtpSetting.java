package com.bcs.core.taishin.circle.PNP.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;

public class PNPFtpSetting {

	private String channelId;
	private String host;
	private int port;
	private int serverHostNamePort;
	private String serverHostName;
	private String APPCode;
	private String RESCode;
	public String account;
	public String password;
	public String protocol;
	private String path;
	private String uploadPath;
	private String fileEncoding;
	private String downloadSavePath;
	private String flow;
	private String smsHost;
	private int smsPort;
	private int smsServerHostNamePort;
	private String smsServerHostName;
	private String smsAPPCode;
	private String smsRESCode;
	public String smsAccount;
	public String smsPassword;
	public String smsProtocol;
	

	private List<String> fileNames = new ArrayList<>();
	
	public static PNPFtpSetting build(PNPFTPType type, boolean isMitakeSMS){
		PNPFtpSetting pnpFtpSetting = new PNPFtpSetting();
		pnpFtpSetting.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READLINES_ENCODE, true, false));
		pnpFtpSetting.setChannelId(type.getSource());
		pnpFtpSetting.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_.toString() + type.toString(), true, false));
		pnpFtpSetting.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVERHOSTNAME_PORT_.toString() + type.toString(), true, false));
		pnpFtpSetting.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_.toString() + type.toString(), true, false));
		pnpFtpSetting.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_.toString() + type.toString(), true, false));
		pnpFtpSetting.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_.toString() + type.toString(), true, false));
		pnpFtpSetting.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_.toString() + type.toString(), true, false));
		pnpFtpSetting.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APPCODE_.toString() + type.toString(), true, false));
		pnpFtpSetting.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RESCODE_.toString() + type.toString(), true, false));
		pnpFtpSetting.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_.toString() + type.toString(), true, false));
		pnpFtpSetting.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH_.toString() + type.toString(), true, false));
		pnpFtpSetting.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_.toString() + type.toString(), true, false));
		pnpFtpSetting.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL_.toString() + type.toString(), true, false));
		pnpFtpSetting.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_.toString() + type.toString(), true, false));
		//SMS setting
		pnpFtpSetting.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVERHOSTNAME_PORT_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APPCODE_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RESCODE_.toString() + type.toString(), true, false));
		pnpFtpSetting.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL_.toString() + type.toString(), true, false));
		return pnpFtpSetting;
	}
	
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean containsFileName(String file) {
		return fileNames.contains(file);
	}

	public List<String> getFileNames() {
		return fileNames;
	}
	
	public void removeFileNames(String file) {
		if (fileNames.contains(file)) {
			fileNames.remove(file);
		}
	}

	public void addFileNames(String file) {
		fileNames.add(file);
	}

	public void clearFileNames() {
		fileNames.clear();
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getServerHostNamePort() {
		return serverHostNamePort;
	}

	public void setServerHostNamePort(int serverHostNamePort) {
		this.serverHostNamePort = serverHostNamePort;
	}

	public String getServerHostName() {
		return serverHostName;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	public String getAPPCode() {
		return APPCode;
	}

	public void setAPPCode(String aPPCode) {
		APPCode = aPPCode;
	}

	public String getRESCode() {
		return RESCode;
	}

	public void setRESCode(String rESCode) {
		RESCode = rESCode;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		if (StringUtils.isBlank(path)) {
			path = "/";
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getFileEncoding() {
		if (StringUtils.isBlank(fileEncoding)) {
			fileEncoding = "big5";
		}
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public String getUploadPath() {
		if (StringUtils.isBlank(uploadPath)) {
			uploadPath = "/";
		}
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public String getDownloadSavePath() {
		if (StringUtils.isBlank(downloadSavePath)) {
			downloadSavePath = "/";
		}
		return downloadSavePath;
	}

	public void setDownloadSavePath(String downloadSavePath) {
		this.downloadSavePath = downloadSavePath;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public String getSmsHost() {
		return smsHost;
	}

	public void setSmsHost(String smsHost) {
		this.smsHost = smsHost;
	}

	public int getSmsPort() {
		return smsPort;
	}

	public void setSmsPort(int smsPort) {
		this.smsPort = smsPort;
	}

	public int getSmsServerHostNamePort() {
		return smsServerHostNamePort;
	}

	public void setSmsServerHostNamePort(int smsServerHostNamePort) {
		this.smsServerHostNamePort = smsServerHostNamePort;
	}

	public String getSmsServerHostName() {
		return smsServerHostName;
	}

	public void setSmsServerHostName(String smsServerHostName) {
		this.smsServerHostName = smsServerHostName;
	}

	public String getSmsAPPCode() {
		return smsAPPCode;
	}

	public void setSmsAPPCode(String smsAPPCode) {
		this.smsAPPCode = smsAPPCode;
	}

	public String getSmsRESCode() {
		return smsRESCode;
	}

	public void setSmsRESCode(String smsRESCode) {
		this.smsRESCode = smsRESCode;
	}

	public String getSmsAccount() {
		return smsAccount;
	}

	public void setSmsAccount(String smsAccount) {
		this.smsAccount = smsAccount;
	}

	public String getSmsPassword() {
		return smsPassword;
	}

	public void setSmsPassword(String smsPassword) {
		this.smsPassword = smsPassword;
	}

	public String getSmsProtocol() {
		return smsProtocol;
	}

	public void setSmsProtocol(String smsProtocol) {
		this.smsProtocol = smsProtocol;
	}

	
}
