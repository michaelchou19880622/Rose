package com.bcs.core.taishin.circle.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class FtpSetting {

	private String channelId;
	private String host;
	private int port;
	private int serverHostNamePort;
	private String serverHostName;
	private String APPCode;
	private String RESCode;
	public String account;
	public String password;
	private String path;
	public String protocol;
	private String fileEncoding;

	private List<String> fileNames = new ArrayList<>();
	
	
	
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
			fileEncoding = "UTF-8";
		}
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

}
