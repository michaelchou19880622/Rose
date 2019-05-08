package com.bcs.core.taishin.circle.ftp;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

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
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.tsb.util.TrendPwMgmt;

@Service
public class FtpService {
	/** Logger */
	private static Logger logger = Logger.getLogger(FtpService.class);
	private static String channelIds = CoreConfigReader.getString(CONFIG_STR.BN_FTP_CHANNELIDS, true);
	private static String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true);
	private static String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true);
	private static boolean is64Bit = CoreConfigReader.getBoolean(CONFIG_STR.BN_FTP_IS64BIT, true);
	private List<FtpSetting> ftpSettings = new ArrayList<>();

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
//			if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
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

	public FtpService() {
		logger.info("BN_FTP_CHANNELIDS:" + channelIds);
		if (StringUtils.isNotBlank(channelIds)) {
			for (String channel : channelIds.split(",")) {
				if (StringUtils.isNotBlank(channel)) {
					String host = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_HOST.toString(), true);
					int port = CoreConfigReader.getInteger(channel, CONFIG_STR.BN_FTP_PORT.toString(), true);
					int serverHostNamePort = CoreConfigReader.getInteger(channel,
							CONFIG_STR.BN_FTP_SERVER_HOSTNAME_PORT.toString(), true);
					String serverHostName = CoreConfigReader.getString(channel,
							CONFIG_STR.BN_FTP_SERVER_HOSTNAME.toString(), true);
					String APPCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_APP_CODE.toString(), true);
					String RESCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_RES_CODE.toString(), true);
					String account = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_ACCOUNT.toString(), true);
					String password = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PASSWORD.toString(), true);
					String path = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PATH.toString(), true);
					String fileEncoding = CoreConfigReader.getString(channel,
							CONFIG_STR.BN_FTP_FILE_ENCODING.toString(), true);
					String protocol = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PROTOCOL.toString(), true);
					if (StringUtils.isNotBlank(host)) {
						FtpSetting ftpSetting = new FtpSetting();
						ftpSetting.setProtocol(protocol);
						ftpSetting.setChannelId(channel);
						ftpSetting.setPath(path);
						ftpSetting.setAccount(account);
						ftpSetting.setPassword(password);
						ftpSetting.setAPPCode(APPCode);
						ftpSetting.setRESCode(RESCode);
						ftpSetting.setHost(host);
						ftpSetting.setPort(port);
						ftpSetting.setFileEncoding(fileEncoding);
						ftpSetting.setServerHostName(serverHostName);
						ftpSetting.setServerHostNamePort(serverHostNamePort);
						if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
							if (!validateFtpHostData(ftpSetting)) {
								throw new RuntimeException("FTP setting error!");
							}
						} else {
							if (!validateDevFtpHostData(ftpSetting)) {
								throw new RuntimeException("FTP setting error!");
							}
						}

						ftpSettings.add(ftpSetting);
						// 正式環境時使用
						if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
							loadFtp(ftpSetting);
						}
					}
				}
			}
		}
	}

	/**
	 * 取得ftp setting info
	 * 
	 * @return
	 */
	public List<FtpSetting> getFtpSettings() {
		return ftpSettings;
	}

	/**
	 * 重新載入參數設定資料
	 */
	private Map<String, String> loadFtp(FtpSetting ftpSetting) {
		Map<String, String> data = new HashMap<String, String>();
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
	 * 檢查ftpserver setting
	 * 
	 * @return
	 */
	private boolean validateDevFtpHostData(FtpSetting setting) {
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

		if (StringUtils.isBlank(downloadSavePath) || StringUtils.isBlank(fileExtension)) {
			logger.error("ftp setting error  downloadSavePath[" + downloadSavePath + "] fileExtension[" + fileExtension
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
	private boolean validateFtpHostData(FtpSetting setting) {
		if (setting.getPort() <= 0) {
			logger.error("ftp port not setting");
			return false;
		}
		if (StringUtils.isBlank(setting.getHost())) {
			logger.error("ftp setting error  host[" + setting.getHost() + "] ");
			return false;
		}

		if (StringUtils.isBlank(downloadSavePath) || StringUtils.isBlank(fileExtension)) {
			logger.error("ftp setting error  downloadSavePath[" + downloadSavePath + "] fileExtension[" + fileExtension
					+ "]");
			return false;
		}
		return true;
	}

	/**
	 * 根據setting 決定用FTP or FTPS
	 * 
	 * @param setting
	 * @return
	 */
	private FTPClient getFtpClient(FtpSetting setting) {
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
	private boolean loginFTP(FTPClient pFTPClient, FtpSetting setting) throws Exception {
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
	 * 進行SFTPClient Login
	 * 
	 * 
	 */
	private Session loginSFTP(FtpSetting setting) throws Exception {
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
	 * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
	 * 
	 * @param pDirectory 要下載檔案所在路徑
	 * @param extension  要下載檔案副檔名
	 * @param pFtpType   Ftp Server Type
	 * @return 下載的檔案內容
	 */
	public Map<String, byte[]> downloadMutipleFileInFTPForDev(String pDirectory, String extension, FtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		FTPClient FTPClient = getFtpClient(setting);
		try {
			FTPClient.connect(setting.getHost(), setting.getPort());
			FTPClient.login(setting.getAccount(), setting.getPassword());
			FTPClient.enterLocalPassiveMode();
			FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClient.setAutodetectUTF8(true);
			FTPClient.setControlEncoding("UTF-8");
			FTPClient.changeWorkingDirectory(pDirectory);
			// 取得FTP中的files
			FTPFile[] files = FTPClient.listFiles();
			for (FTPFile lFtpFile : files) {
				String fileName = lFtpFile.getName();
				if (!lFtpFile.isDirectory() && fileName.endsWith(extension)) {
					ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
					FTPClient.retrieveFile(lFtpFile.getName(), lDataTemp);
					lDataTemp.flush();
					lReturnDatas.put(lFtpFile.getName(), lDataTemp.toByteArray());
					lDataTemp.close();
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
	public Map<String, byte[]> downloadMutipleFileByType(String directory, String extension, FtpSetting setting) {
		if (setting.getProtocol().equalsIgnoreCase("sftp")) {
			if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
				return downloadMutipleFileInSFTP(directory, extension, setting);
			} else {
				return downloadMutipleFileInSFTPForDev(directory, extension, setting);
			}
		} else {
			if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
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
	private Map<String, byte[]> downloadMutipleFileInFTP(String pDirectory, String extension, FtpSetting setting) {
		FTPClient FTPClient = null;
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		try {
			FTPClient = getFtpClient(setting);
			if (loginFTP(FTPClient, setting)) {
				FTPClient.changeWorkingDirectory(pDirectory);
				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				FTPFile[] lFiles = FTPClient.listFiles();
				logger.info("downloadMutipleFileInFTP:" + pDirectory + " File size:" + lFiles.length);
				Map<String, Long> lFileSize = new HashMap<String, Long>();
				for (FTPFile lFtpFile : lFiles) {
					String fileName = lFtpFile.getName();
					logger.info("downloadMutipleFileInFTP:" + pDirectory + " File :" + fileName);
					if (!lFtpFile.isDirectory() && fileName.endsWith(extension)) {
						lFileSize.put(lFtpFile.getName(), lFtpFile.getSize());
					}
				}
				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
				try {
					BigDecimal lWaitingTime = new BigDecimal("5");
					if (lWaitingTime.intValue() <= 0) {
						lWaitingTime = new BigDecimal("5");
					}
					Thread.sleep(lWaitingTime.intValue());
				} catch (InterruptedException lIE) {
					lIE.printStackTrace();
				}
				lFiles = FTPClient.listFiles();
				ByteArrayOutputStream lDataTemp = null;
				for (FTPFile lFtpFile : lFiles) {
					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
					if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
							&& lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
						lDataTemp = new ByteArrayOutputStream();
						FTPClient.retrieveFile(lFtpFile.getName(), lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(lFtpFile.getName(), lDataTemp.toByteArray());
						lDataTemp.close();

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
	private Map<String, byte[]> downloadMutipleFileInSFTP(String pDirectory, String extension, FtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		ChannelSftp channelSftp = null;
		Session session = null;
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
				Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
				Map<String, Long> lFileSizeAndName = new HashMap<String, Long>();
				for (ChannelSftp.LsEntry lFtpFile : list) {
					String fileName = lFtpFile.getFilename();
					logger.info("downloadMutipleFileInSFTP:" + pDirectory + " File :" + fileName);
					lFileSizeAndName.put(lFtpFile.getFilename(), lFtpFile.getAttrs().getSize());
				}
				// T 隔五秒再重新查詢FTP ，判斷兩者的長度
				try {
					BigDecimal lWaitingTime = new BigDecimal("5");
					if (lWaitingTime.intValue() <= 0) {
						lWaitingTime = new BigDecimal("5");
					}
					Thread.sleep(lWaitingTime.intValue());
				} catch (InterruptedException lIE) {
					lIE.printStackTrace();
				}
				list = channelSftp.ls("*." + extension);
				ByteArrayOutputStream lDataTemp = null;
				for (ChannelSftp.LsEntry lFtpFile : list) {
					// T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
					if (lFileSizeAndName.containsKey(lFtpFile.getFilename())
							&& lFtpFile.getAttrs().getSize() == lFileSizeAndName.get(lFtpFile.getFilename())) {
						lDataTemp = new ByteArrayOutputStream();
						channelSftp.get(lFtpFile.getFilename(), lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(lFtpFile.getFilename(), lDataTemp.toByteArray());
						lDataTemp.close();
					}
				}
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
			FtpSetting setting) {
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
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
					// 取得FTP中的files
					Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
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
	public void deleteFileByType(String directory, String[] pFileNames, FtpSetting setting) {
		if (setting.getProtocol().equalsIgnoreCase("sftp")) {
			if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
				deleteFileInSFTP(directory, pFileNames, setting);
			} else {
				deleteFileInSFTPForDev(directory, pFileNames, setting);
			}
		} else {
			if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) { // 正式環境時使用
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
	private void deleteFileInSFTP(String pDirectory, String[] pFileNames, FtpSetting setting) {

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
					setting.removeFileNames(lFileName);
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
	private void deleteFileInSFTPForDev(String pDirectory, String[] pFileNames, FtpSetting setting) {
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
						setting.removeFileNames(lFileName);
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
	private void deleteFileInFTPForDev(String pDirectory, String[] pFileNames, FtpSetting setting) {
		FTPClient FTPClient = getFtpClient(setting);
		try {
			FTPClient.connect(setting.getHost(), setting.getPort());
			FTPClient.login(setting.getAccount(), setting.getPassword());
			FTPClient.enterLocalPassiveMode();
			FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPClient.setAutodetectUTF8(true);
			FTPClient.setControlEncoding("UTF-8");
			FTPClient.changeWorkingDirectory(pDirectory);
			for (String lFileName : pFileNames) {
				boolean success = FTPClient.deleteFile(lFileName);
				setting.removeFileNames(lFileName);
				if (!success) {
					logger.error(" deleteFileInFTPForDev remove fail: " + lFileName);
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
	private void deleteFileInFTP(String pDirectory, String[] pFileNames, FtpSetting setting) {
		FTPClient FTPClient = null;
		try {
			FTPClient = getFtpClient(setting);
			if (loginFTP(FTPClient, setting)) {
				FTPClient.changeWorkingDirectory(pDirectory);

				FTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				for (String lFileName : pFileNames) {
					boolean success = FTPClient.deleteFile(lFileName);
					setting.removeFileNames(lFileName);
					if (!success) {
						logger.error("deleteFileInFTP remove fail: " + lFileName);
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
