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
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tsb.util.TrendPwMgmt;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ???
 */
@Slf4j(topic = "BNRecorder")
@Service
public class FtpService {
    private static String channelIds = CoreConfigReader.getString(CONFIG_STR.BN_FTP_CHANNEL_IDS, true, false);
    private static String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true, false);
    private static String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true, false);
    private static boolean is64Bit = CoreConfigReader.getBoolean(CONFIG_STR.BN_FTP_IS64BIT, true, false);
    private List<FtpSetting> ftpSettings = new ArrayList<>();

    /**
     * FTP密碼系統載入dll的狀態，參數為false :系統照原本設計從系統參數檔取得帳號、密碼，<BR>
     * 參數為true :系統透過JNI機制去取得帳號、密碼
     */
    public static boolean JNI_LIBRARY_STATUS = false;

    /*
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
//			log.error("SecurityException:" + lSE.getMessage());
//		} catch (UnsatisfiedLinkError lUE) {
//			JNI_LIBRARY_STATUS = false;
//			log.error("UnsatisfiedLinkError:" + lUE.getMessage());
//		} catch (NullPointerException lNPE) {
//			JNI_LIBRARY_STATUS = false;
//			log.error("NullPointerException:" + lNPE.getMessage());
//		} catch (Exception lEXP) {
//			JNI_LIBRARY_STATUS = false;
//			log.error("Exception:" + lEXP.getMessage());
//		}

        log.info("JNI_LIBRARY_STATUS: {}", JNI_LIBRARY_STATUS);
    }

    public FtpService() {
        getFtpConnectInformation();
    }

    public void getFtpConnectInformation() {
        log.info("Get Ftp Connection Information!!");
        log.info("Ftp Channel Id List: {}", channelIds);
        if (StringUtils.isBlank(channelIds)) {
            log.info("Channel id is empty!!");
            return;
        }
        for (String channel : channelIds.split(",")) {
            if (StringUtils.isBlank(channel)) {
                log.info("Channel id is blank!!");
                continue;
            }
            String host = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_HOST.toString(), true, false);
            if (StringUtils.isBlank(host)) {
                log.info("Host is blank!!");
                continue;
            }

            int port = CoreConfigReader.getInteger(channel, CONFIG_STR.BN_FTP_PORT.toString(), true, false);
            int serverHostNamePort = CoreConfigReader.getInteger(channel, CONFIG_STR.BN_FTP_SERVER_HOSTNAME_PORT.toString(), true, false);
            String serverHostName = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_SERVER_HOSTNAME.toString(), true, false);
            String appCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_APP_CODE.toString(), true, false);
            String resCode = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_RES_CODE.toString(), true, false);
            String account = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_ACCOUNT.toString(), true, false);
            String password = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PASSWORD.toString(), true, false);
            String path = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PATH.toString(), true, false);
            String fileEncoding = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_FILE_ENCODING.toString(), true, false);
            String protocol = CoreConfigReader.getString(channel, CONFIG_STR.BN_FTP_PROTOCOL.toString(), true, false);

            FtpSetting ftpSetting = new FtpSetting();
            ftpSetting.setProtocol(protocol);
            ftpSetting.setChannelId(channel);
            ftpSetting.setPath(path);
            ftpSetting.setAccount(account);
            ftpSetting.setPassword(password);
            ftpSetting.setAPPCode(appCode);
            ftpSetting.setRESCode(resCode);
            ftpSetting.setHost(host);
            ftpSetting.setPort(port);
            ftpSetting.setFileEncoding(fileEncoding);
            ftpSetting.setServerHostName(serverHostName);
            ftpSetting.setServerHostNamePort(serverHostNamePort);

            // log.info("Before connect FtpSetting info: {}", DataUtils.toPrettyJsonUseJackson(ftpSetting));

            if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
                //  log.info("Use Production Environment!!");
                if (!validateFtpHostData(ftpSetting)) {
                    throw new RuntimeException("FTP setting error!");
                }
            } else {
                // log.info("Use Develop Environment!!");
                if (!validateDevFtpHostData(ftpSetting)) {
                    throw new RuntimeException("FTP setting error!");
                }
            }

            this.ftpSettings.add(ftpSetting);

            if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
                log.info("Use Production Environment!!");
                Map<String, String> accountInfoMap = getTaishinFtpConnectionInfo(ftpSetting);

                log.info(String.format("Ftp Host: %s / UID: %s / PWD: %s",
                        ftpSetting.getHost(), accountInfoMap.get("uid"), accountInfoMap.get("pwd"))
                );

                ftpSetting.setAccount(accountInfoMap.get("uid"));
                ftpSetting.setPassword(accountInfoMap.get("pwd"));
            } else {
                log.info("Use Develop Environment!!");
            }
        }
    }

    /**
     * 取得ftp setting info
     */
    public List<FtpSetting> getFtpSettings() {
        if (this.ftpSettings == null || this.ftpSettings.isEmpty()) {
            log.info("Ftp Setting is null or empty, retry get ftp connection information!!");
            this.getFtpConnectInformation();
        }
        return this.ftpSettings;
    }

    /**
     * 重新載入參數設定資料
     */
    private Map<String, String> getTaishinFtpConnectionInfo(FtpSetting ftpSetting) {
        log.info("Get Taishin BN Ftp Connection Information Start!!");
        Map<String, String> data = new HashMap<>(2);
        try {
            TrendPwMgmt lPwMgmt = new TrendPwMgmt(
                    ftpSetting.getServerHostName(),
                    ftpSetting.getServerHostNamePort(),
                    ftpSetting.getAPPCode(),
                    ftpSetting.getRESCode(), is64Bit
            );

            data.put("uid", StringUtils.trimToEmpty(lPwMgmt.getUserId()));
            data.put("pwd", StringUtils.trimToEmpty(lPwMgmt.getPassword()));

            return data;
        } catch (Exception e) {
            log.error("TrendPwMgmt exception:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 檢查ftp server setting
     */
    private boolean validateDevFtpHostData(FtpSetting setting) {
        if (setting.getPort() <= 0) {
            log.error("ftp port not setting");
            return false;
        }

        if (StringUtils.isBlank(setting.getHost())) {
            log.error("ftp setting error  host[" + setting.getHost() + "] ");
            return false;
        }
        if (StringUtils.isBlank(setting.getAccount()) || StringUtils.isBlank(setting.getPassword())) {
            log.error("ftp setting error  account[" + setting.getAccount() + "] password[" + setting.getPassword() + "]");
            return false;
        }

        if (StringUtils.isBlank(downloadSavePath) || StringUtils.isBlank(fileExtension)) {
            log.error("ftp setting error  downloadSavePath[" + downloadSavePath + "] fileExtension[" + fileExtension + "]");
            return false;
        }
        return true;
    }

    /**
     * 檢查ftp server setting
     */
    private boolean validateFtpHostData(FtpSetting setting) {
        if (setting.getPort() <= 0) {
            log.error("ftp port not setting");
            return false;
        }
        if (StringUtils.isBlank(setting.getHost())) {
            log.error("ftp setting error  host[" + setting.getHost() + "] ");
            return false;
        }

        if (StringUtils.isBlank(downloadSavePath) || StringUtils.isBlank(fileExtension)) {
            log.error("ftp setting error  downloadSavePath[" + downloadSavePath + "] fileExtension[" + fileExtension
                    + "]");
            return false;
        }
        return true;
    }

    /**
     * 根據setting 決定用FTP or FTPS
     */
    private FTPClient getFtpClient(FtpSetting setting) {
        FTPClient fTPClient = new FTPClient();
        if ("ftps".equalsIgnoreCase(setting.getProtocol())) {
            fTPClient = new FTPSClient(true);
        }
        return fTPClient;
    }

    /**
     * 進行FTPClient Login
     */
    private boolean loginFTP(FTPClient pFtpClient, FtpSetting setting) {
        try {

            pFtpClient.setConnectTimeout(30 * 1000);
            pFtpClient.setDefaultTimeout(30 * 1000);
            pFtpClient.setDataTimeout(30 * 1000);
            pFtpClient.connect(setting.getHost(), setting.getPort());

            // T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
            boolean lStatus = false;
            String account = setting.getAccount();
            String password = setting.getPassword();
            if (StringUtils.isNotBlank(account) && StringUtils.isNotBlank(password)) {
                lStatus = pFtpClient.login(account, password);
                if (lStatus) {
                    log.info("{} 資源密碼系統的帳號密碼登入完成", setting.getHost());
                    return true;
                }
                log.error("{} 資源密碼系統的帳號密碼登入失敗 APP[{}] RES[{}], 重新載入FTP連線參數！",
                        new Object[]{setting.getHost(), setting.getAPPCode(), setting.getRESCode()});
            }

            // T 重新載入一次設定
            // T 重新取得FTP 參數設定值
            pFtpClient.disconnect();
            Map<String, String> trendPwMgmt = getTaishinFtpConnectionInfo(setting);
            log.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" + trendPwMgmt.get("pwd"));
            pFtpClient.connect(setting.getHost(), setting.getPort());
            lStatus = pFtpClient.login(trendPwMgmt.get("uid"), trendPwMgmt.get("pwd"));
            if (lStatus) {
                log.info("{} 資源密碼系統的帳號密碼登入完成", setting.getHost());
                return true;
            }
            log.error("{} 資源密碼系統的帳號密碼登入失敗 APP[{}] RES[{}], 改以原系統記錄帳號密碼進行登入!",
                    new Object[]{setting.getHost(), setting.getAPPCode(), setting.getRESCode()});

            // T 以系統預設密碼登入
            lStatus = pFtpClient.login("ACCOUNT", "PASSWORD");
            if (lStatus) {
                log.info("{} 原系統記錄帳號密碼登入完成", setting.getHost());
                return true;
            }
            log.error("{} 原系統記錄帳號密碼登入失敗，停止執行", setting.getHost());
            return false;
        } catch (Exception e) {
            log.error("loginFTP:" + e.getMessage());
        }
        return false;
    }

    /**
     * 進行SFTPClient Login
     */
    private Session loginSFTP(FtpSetting setting) {
        boolean lStatus = false;
        Session session = null;
        try {
            JSch jsch = new JSch();
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");

            // T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
            String account = setting.getAccount();
            String password = setting.getPassword();
            if (StringUtils.isNotBlank(account) && StringUtils.isNotBlank(password)) {
                session = jsch.getSession(account, setting.getHost(), setting.getPort());
                session.setPassword(password);
                session.setConfig(sshConfig);
                session.setTimeout(30 * 1000);
                session.connect();
                lStatus = session.isConnected();
                log.info("1-1 session.isConnected() = {}", lStatus);
                
                if (lStatus) {
                    log.info("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
                    return session;
                }
                log.error("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
                        + setting.getRESCode() + "]，重新載入FTP連線參數!");

            }

            // T 重新載入一次設定
            // T 重新取得FTP 參數設定值
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            session = null;
            Map<String, String> trendPwMgmt = getTaishinFtpConnectionInfo(setting);
            log.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" + trendPwMgmt.get("pwd"));
            session = jsch.getSession(trendPwMgmt.get("uid"), setting.getHost(), setting.getPort());
            session.setPassword(trendPwMgmt.get("pwd"));
            session.setConfig(sshConfig);
            session.setTimeout(30 * 1000);
            session.connect();
            lStatus = session.isConnected();
            log.info("1-2 session.isConnected() = {}", lStatus);
            if (lStatus) {
                log.info("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入完成");
                return session;
            }
            log.error("loginSFTP:" + setting.getHost() + "資源密碼系統的帳號密碼登入失敗 APP[" + setting.getAPPCode() + "] RES["
                    + setting.getRESCode() + "]，改以原系統記錄帳號密碼進行登入!");


            // T 以系統預設密碼登入
            session = jsch.getSession("ACCOUNT", setting.getHost(), setting.getPort());
            session.setPassword("PASSWORD");
            session.setConfig(sshConfig);
            session.setTimeout(30 * 1000);
            session.connect();
            lStatus = session.isConnected();
            log.info("1-3 session.isConnected() = {}", lStatus);
            if (lStatus) {
                log.info("loginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入完成");
                return session;
            }
            log.error("loginSFTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
            return null;
        } catch (Exception ex) {
            log.error("loginSFTP Error: " + ex.getMessage());
        }
        return null;
    }

    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @return 下載的檔案內容
     */
    public Map<String, byte[]> downloadMultipleFileInFTPForDev(String pDirectory, String extension, FtpSetting setting) {
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        FTPClient ftpClient = getFtpClient(setting);
        ftpClient.setConnectTimeout(30 * 1000);
        ftpClient.setDefaultTimeout(30 * 1000);
        ftpClient.setDataTimeout(30 * 1000);        
        try {
            ftpClient.connect(setting.getHost(), setting.getPort());
            ftpClient.login(setting.getAccount(), setting.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);
            ftpClient.setControlEncoding(setting.getFileEncoding());
            ftpClient.changeWorkingDirectory(pDirectory);
            // 取得FTP中的files
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile lFtpFile : files) {
                String fileName = lFtpFile.getName();
                if (!lFtpFile.isDirectory() && fileName.toUpperCase().endsWith(extension)) {
                    ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                    ftpClient.retrieveFile(lFtpFile.getName(), lDataTemp);
                    lDataTemp.flush();
                    lReturnDataMap.put(lFtpFile.getName(), lDataTemp.toByteArray());
                    lDataTemp.close();
                }
            }
            ftpClient.logout();
        } catch (Exception ex) {
            log.error("downloadMultipleFileInFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
                log.error("downloadMultipleFileInFTPForDev Error: " + ex.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) 根據類型指向不同FTP
     *
     * @param directory 要下載檔案所在路徑
     * @param extension 要下載檔案副檔名
     * @param setting   Ftp Server Type
     * @return 下載的檔案內容
     */
    public Map<String, byte[]> downloadMultipleFileByType(String directory, String extension, FtpSetting setting) {
        if ("sftp".equalsIgnoreCase(setting.getProtocol())) {
            if (CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
                return downloadMultipleFileInSFTPForDev(directory, extension, setting);
            }
            // 正式環境時使用
            return downloadMultipleFileInSFTP(directory, extension, setting);
        }
        if (CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
            return downloadMultipleFileInFTPForDev(directory, extension, setting);
        }
        // 正式環境時使用
        return downloadMultipleFileInFTP(directory, extension, setting);

    }

    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @return 下載的檔案內容
     */
    private Map<String, byte[]> downloadMultipleFileInFTP(String pDirectory, String extension, FtpSetting setting) {
        FTPClient ftpClient = null;
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        try {
            ftpClient = getFtpClient(setting);
            ftpClient.setConnectTimeout(30 * 1000);
            ftpClient.setDefaultTimeout(30 * 1000);
            ftpClient.setDataTimeout(30 * 1000);        
            if (loginFTP(ftpClient, setting)) {
                ftpClient.changeWorkingDirectory(pDirectory);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                FTPFile[] lFiles = ftpClient.listFiles();
                log.info("downloadMultipleFileInFTP:" + pDirectory + " File size:" + lFiles.length);
                Map<String, Long> lFileSize = new HashMap<>();
                for (FTPFile lFtpFile : lFiles) {
                    String fileName = lFtpFile.getName();
                    log.info("downloadMultipleFileInFTP:" + pDirectory + " File :" + fileName);
                    if (!lFtpFile.isDirectory() && fileName.toUpperCase().endsWith(extension)) {
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
                    log.error(lIE.getMessage());
                    Thread.currentThread().interrupt();
                }
                lFiles = ftpClient.listFiles();
                ByteArrayOutputStream lDataTemp = null;
                for (FTPFile lFtpFile : lFiles) {
                    // T 只有檔案在五秒間隔裡檔案大小完全一致才算完整可以處理檔案
                    if (lFtpFile.isFile() && (lFileSize.containsKey(lFtpFile.getName())
                            && lFtpFile.getSize() == lFileSize.get(lFtpFile.getName()))) {
                        lDataTemp = new ByteArrayOutputStream();
                        ftpClient.retrieveFile(lFtpFile.getName(), lDataTemp);
                        lDataTemp.flush();
                        lReturnDataMap.put(lFtpFile.getName(), lDataTemp.toByteArray());
                        lDataTemp.close();
                    }
                }
                return lReturnDataMap;
            }
            ftpClient.logout();            
        } catch (Exception e) {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception lEXP) {
                log.error("downloadMultipleFileInFTP Exception: " + lEXP.getMessage());
            }
            log.error("Exception", e);
        } finally {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception lEXP) {
                log.error("downloadMultipleFileInFTP Exception: " + lEXP.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @return 下載的檔案內容
     */
    private Map<String, byte[]> downloadMultipleFileInSFTP(String pDirectory, String extension, FtpSetting setting) {
		log.info("---------- downloadMultipleFileInSFTP ----------");
		log.info("pDirectory = {}", pDirectory);
		log.info("extension = {}", extension);
		log.info("setting = {}", setting);
    	
    	Map<String, byte[]> lReturnDataMap = new HashMap<>();
        ChannelSftp channelSftp = null;
        Session session = null;
        try {
            session = this.loginSFTP(setting);
            if (session == null) {
                log.error("downloadMultipleFileInSFTP session null");
                return lReturnDataMap;
            }
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(30 * 1000);  
            
            if (channelSftp.isConnected()) {
				log.info("channelSftp.isConnected() = {}", channelSftp.isConnected());
                channelSftp.cd(pDirectory);
                
                // 取得FTP中的files
                Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
                list.addAll(channelSftp.ls("*." + extension.toLowerCase()));
				log.info("1-1 list = {}", list.toArray());

                Map<String, Long> lFileSizeAndName = new HashMap<>();
                for (ChannelSftp.LsEntry lFtpFile : list) {
                    String fileName = lFtpFile.getFilename();
                    log.info("downloadMultipleFileInSFTP:" + pDirectory + " File :" + fileName);
                    lFileSizeAndName.put(lFtpFile.getFilename(), lFtpFile.getAttrs().getSize());
                }
                
//                // T 隔五秒再重新查詢FTP ，判斷兩者的長度
//                try {
//                    BigDecimal lWaitingTime = new BigDecimal("5");
//                    if (lWaitingTime.intValue() <= 0) {
//                        lWaitingTime = new BigDecimal("5");
//                    }
//                    
//    				log.info("Check file size after 5 seconds... [START]");
//                    Thread.sleep(lWaitingTime.intValue());
//                } catch (InterruptedException lIE) {
//                    log.error("InterruptedException: {}", lIE);
//                    Thread.currentThread().interrupt();
//                }
                
                // T 隔15秒再重新查詢FTP ，判斷兩者的長度
				log.info("Wait 15 seconds, then check file size again.");
				for (int i = 1; i <= 15; i++) {
					log.info("Wait {} seconds..", i);
					
					Thread.sleep(1000L);
				}
                
				log.info("Start to check files size again.");
                list = channelSftp.ls("*." + extension);
                list.addAll(channelSftp.ls("*." + extension.toLowerCase()));
				log.info("1-2 list = {}", list.toArray());

                ByteArrayOutputStream lDataTemp = null;
                for (ChannelSftp.LsEntry lFtpFile : list) {
                    // T 只有檔案在15秒間隔裡檔案大小完全一致才算完整可以處理檔案
                	
                	String sftpFileName = lFtpFile.getFilename();
                	long sftpFileSize = lFtpFile.getAttrs().getSize();
    				log.info("sftpFileName = {}, sftpFileSize", sftpFileName, sftpFileSize);
                	
                    if (lFileSizeAndName.containsKey(sftpFileName) && (sftpFileSize == lFileSizeAndName.get(sftpFileName))) {
						log.info("The file [{}] has been transferred and the file size [{}] has not changed", 
								lFtpFile.getFilename(), lFtpFile.getAttrs().getSize());
                    	
                    	lDataTemp = new ByteArrayOutputStream();
                        channelSftp.get(lFtpFile.getFilename(), lDataTemp);
                        lDataTemp.flush();
                        lReturnDataMap.put(lFtpFile.getFilename(), lDataTemp.toByteArray());
                        lDataTemp.close();
                    }
                }
            } else {
                log.error("downloadMultipleFileInSFTP channelSftp false");
            }

		} catch (JSchException je) {
			log.error("JSchException: {}", je);

		} catch (Exception ex) {
			log.error("downloadMultipleFileInSFTP Error: {}", ex);
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
					log.info("channelSftp disconnected");
				}
                channelSftp = null;                				
				if (session != null && session.isConnected()) {
					session.disconnect();
					log.info("session disconnected");
				}
			} catch (Exception ex) {
				log.error("downloadMultipleFileInSFTP Error: {}", ex);
			}
		}

        return lReturnDataMap;
    }

    /**
     * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @return 下載的檔案內容
     */
    public Map<String, byte[]> downloadMultipleFileInSFTPForDev(String pDirectory, String extension,
                                                                FtpSetting setting) {
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        ChannelSftp channelSftp = null;
        Session session = null;
        try {
            JSch jsch = new JSch();
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(setting.getAccount(), setting.getHost(), setting.getPort());
            session.setPassword(setting.getPassword());
            session.setConfig(sshConfig);
            session.setTimeout(30 * 1000);            
            session.connect();
            if (session.isConnected()) {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                if (channelSftp.isConnected()) {
                    channelSftp.cd(pDirectory);
                    // 取得FTP中的files
                    Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
                    list.addAll(channelSftp.ls("*." + extension));

                    for (ChannelSftp.LsEntry lFtpFile : list) {
                        String fileName = lFtpFile.getFilename();
                        log.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                        ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                        channelSftp.get(fileName, lDataTemp);
                        lDataTemp.flush();
                        lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                        lDataTemp.close();
                    }
                } else {
                    log.error(" downloadMultipleFileInSFTPForDev channelSftp isConnected faile ");
                }

            } else {
                log.error(" downloadMultipleFileInSFTPForDev session isConnected faile ");
            }


        } catch (Exception ex) {
            log.error(" downloadMultipleFileInSFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                channelSftp = null;
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                session = null;
            } catch (Exception ex) {
                log.error(" downloadMultipleFileInSFTPForDev Error: " + ex.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 刪除指定檔案 根據類型指向不同FTP
     *
     * @param directory  原始目錄
     * @param pFileNames 要刪除的檔案名稱
     */
    public void deleteFileByType(String directory, String[] pFileNames, FtpSetting setting) {

        if ("sftp".equalsIgnoreCase(setting.getProtocol())) {
            log.info("Use sFTP!!");
            if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
                // 正式環境時使用
                deleteFileInSFTP(directory, pFileNames, setting);
            } else {
                deleteFileInSFTPForDev(directory, pFileNames, setting);
            }
        } else {
            log.info("Use FTP!!");
            if (!CoreConfigReader.isBillingNoticeFtpTypeDevelop()) {
                // 正式環境時使用
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
     */
    private void deleteFileInSFTP(String pDirectory, String[] pFileNames, FtpSetting setting) {
        log.info("Delete File In sFTP Start!!");
        ChannelSftp channelSftp = null;
        Session session = null;
        try {
            session = loginSFTP(setting);
            if (session == null) {
                log.error("deleteFileInSFTP connection failed");
                return;
            }
            log.info("Ftp Connection success!!");
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            if (!channelSftp.isConnected()) {
                log.error("deleteFileInSFTP channelSftp: " + channelSftp.isConnected());
                return;
            }

            log.info("Channel is connected!!");
            channelSftp.cd(pDirectory);

            for (String lFileName : pFileNames) {
                log.info("Delete File Name is : {}", lFileName);
                channelSftp.rm(lFileName);
                setting.removeFileNames(lFileName);
            }
        } catch (Exception ex) {
            log.error("deleteFileInSFTP Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }                
                channelSftp = null;
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                session = null;

            } catch (Exception ex) {
                log.error("deleteFileInSFTP Error: " + ex.getMessage());
            }
        }
    }

    /**
     * 刪除SFTP指定檔案 (測試環境用)
     *
     * @param pDirectory 原始目錄
     * @param pFileNames 要刪除的檔案名稱
     */
    private void deleteFileInSFTPForDev(String pDirectory, String[] pFileNames, FtpSetting setting) {
        ChannelSftp channelSFtp = null;
        Session session = null;
        try {
            JSch jsch = new JSch();
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(setting.getAccount(), setting.getHost(), setting.getPort());
            session.setPassword(setting.getPassword());
            session.setConfig(sshConfig);
            session.setTimeout(30 * 1000);
            session.connect();
            if (session.isConnected()) {
                log.info("Session is connected!!");
                channelSFtp = (ChannelSftp) session.openChannel("sftp");
                channelSFtp.connect();
                if (channelSFtp.isConnected()) {
                    log.info("Channel is connected!!");
                    channelSFtp.cd(pDirectory);
                    for (String lFileName : pFileNames) {
                        log.info("Delete File Name is : {}", lFileName);
                        channelSFtp.rm(lFileName);
                        setting.removeFileNames(lFileName);
                    }
                } else {
                    log.error("deleteFileInSFTPForDev channelSftp: " + channelSFtp.isConnected());
                }
            } else {
                log.error("deleteFileInSFTPForDev session: " + session.isConnected());
            }

        } catch (Exception ex) {
            log.error("deleteFileInSFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSFtp != null && channelSFtp.isConnected()) {
                    channelSFtp.disconnect();
                }
                channelSFtp = null;
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                session = null;
            } catch (Exception ex) {
                log.error("deleteFileInSFTPForDev Error: " + ex.getMessage());
            }
        }
    }

    /**
     * 刪除FTP/FTPS指定檔案 (測試環境用)
     *
     * @param pDirectory 原始目錄
     * @param pFileNames 要刪除的檔案名稱
     */
    private void deleteFileInFTPForDev(String pDirectory, String[] pFileNames, FtpSetting setting) {
        FTPClient ftpClient = getFtpClient(setting);
        ftpClient.setConnectTimeout(30 * 1000);
        ftpClient.setDefaultTimeout(30 * 1000);
        ftpClient.setDataTimeout(30 * 1000);        
        try {
            ftpClient.connect(setting.getHost(), setting.getPort());
            ftpClient.login(setting.getAccount(), setting.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);
            ftpClient.setControlEncoding(setting.getFileEncoding());
            ftpClient.changeWorkingDirectory(pDirectory);
            for (String lFileName : pFileNames) {
                log.info("Deleting an FTP file, filename={}", lFileName);
                boolean success = ftpClient.deleteFile(lFileName);
                setting.removeFileNames(lFileName);
                if (!success) {
                    log.error(" deleteFileInFTPForDev remove fail: " + lFileName);
                }
            }
            ftpClient.logout();
        } catch (Exception ex) {
            log.error("deleteFileInFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
                log.error("deleteFileInFTPForDev Error: " + ex.getMessage());
            }
        }
    }

    /**
     * 刪除指定檔案
     *
     * @param pDirectory 原始目錄
     * @param pFileNames 要刪除的檔案名稱
     */
    private void deleteFileInFTP(String pDirectory, String[] pFileNames, FtpSetting setting) {
        log.info("Delete File In FTP Start!!");
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient(setting);
            ftpClient.setConnectTimeout(30 * 1000);
            ftpClient.setDefaultTimeout(30 * 1000);
            ftpClient.setDataTimeout(30 * 1000);        
            if (loginFTP(ftpClient, setting)) {
                log.info("Ftp Connection success!!");
                ftpClient.changeWorkingDirectory(pDirectory);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                for (String lFileName : pFileNames) {
                    log.info("Delete File Name is : {}", lFileName);
                    boolean success = ftpClient.deleteFile(lFileName);
                    setting.removeFileNames(lFileName);
                    if (!success) {
                        log.error("deleteFileInFTP remove fail: " + lFileName);
                    }
                }
            } else {
                log.info("Ftp Connection Fail!!");
            }
            ftpClient.logout();            
        } catch (Exception e) {
            log.error("deleteFileInFTP Exception" + e.getMessage());
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception lEXP) {
                log.error("deleteFileInFTP disconnect Exception" + lEXP.getMessage());
            }

        } finally {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception lEXP) {
                log.error("deleteFileInFTP Exception" + lEXP.getMessage());
            }
        }
    }
}
