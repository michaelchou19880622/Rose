package com.bcs.core.taishin.circle.PNP.ftp;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.scheduler.LoadFtpPnpDataTask;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.tsb.util.TrendPwMgmt;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * FTP Service
 *
 * @author ???
 */
@Service
public class PNPFtpService {
    private static Logger logger = Logger.getLogger(PNPFtpService.class);
    private static boolean is64Bit = CoreConfigReader.getBoolean(CONFIG_STR.PNP_FTP_IS64BIT, true, false);

    /**
     * 所有FTP設定對照表
     */
    private Map<String, PNPFtpSetting> ftpSettings = new HashMap<>();


    public PNPFtpService() {
        initFtpSettings();
    }

    /**
     * 取得所有FTP相關設定
     */
    private void initFtpSettings() {
        for (PNPFTPType type : PNPFTPType.values()) {
            PNPFtpSetting pnpFtpSetting = PNPFtpSetting.build(type);

            /* 如果不是開發環境則進行資源密碼系統取得帳號密碼 */
            if (CoreConfigReader.isPNPFtpTypeDevelop()) {
                pnpFtpSetting = useTrendPwMgmt(pnpFtpSetting);
            }
            ftpSettings.put(type.getSource(), pnpFtpSetting);
        }
        logger.info("initFtpSettings...OK");
    }

    /**
     * 至資源密碼系統下載帳號密碼
     * @param pnpFtpSetting pnpFtpSetting
     * @return new PNPFtpSetting
     */
    private PNPFtpSetting useTrendPwMgmt(PNPFtpSetting pnpFtpSetting) {

        String host = pnpFtpSetting.getHost();
        String serverHostName = pnpFtpSetting.getServerHostName();
        int serverHostNamePort = pnpFtpSetting.getServerHostNamePort();
        String appCode = pnpFtpSetting.getAPPCode();
        String resCode = pnpFtpSetting.getRESCode();

        Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, appCode, resCode);
        logger.info(String.format("loginFTP: %s PWD: %s", trendPwMgmt.get("uid"), trendPwMgmt.get("pwd")));
        pnpFtpSetting.setAccount(trendPwMgmt.get("uid"));
        pnpFtpSetting.setPassword(trendPwMgmt.get("pwd"));
        logger.info("Download Side FTP Resource ID Password Login Done!!");
        logger.info("下載段資源密碼系統的帳號密碼登入完成");

        String smsHost = pnpFtpSetting.getSmsHost();
        String smsServerHostName = pnpFtpSetting.getSmsServerHostName();
        int smsServerHostNamePort = pnpFtpSetting.getSmsServerHostNamePort();
        String smsAPPCode = pnpFtpSetting.getSmsAPPCode();
        String smsRESCode = pnpFtpSetting.getSmsRESCode();
        Map<String, String> trendPwMgmtSMS = loadFtp(smsHost, smsServerHostName, smsServerHostNamePort, smsAPPCode, smsRESCode);
        logger.info(String.format("login FTP: %s PWD: %s", trendPwMgmtSMS.get("uid"), trendPwMgmtSMS.get("pwd")));
        pnpFtpSetting.setSmsAccount(trendPwMgmtSMS.get("uid"));
        pnpFtpSetting.setSmsPassword(trendPwMgmtSMS.get("pwd"));
        logger.info("Upload Side FTP Resource ID Password Login Done!!");
        logger.info("上傳段資源密碼系統的帳號密碼登入完成");

        return pnpFtpSetting;
    }

    /**
     * 取得FTP Setting info
     *
     * @param source source
     * @return Setting
     */
    public PNPFtpSetting getFtpSettings(String source) {
        return ftpSettings.get(source);
    }


    /**
     * 重新載入參數設定資料
     */
    private Map<String, String> loadFtp(PNPFtpSetting ftpSetting) {
        Map<String, String> data = new HashMap<>();
        logger.info("ftpSetting.getChannelId() : " + ftpSetting.getChannelId());
        logger.info("ftpSetting.getServerHostNamePort() : " + ftpSetting.getServerHostNamePort());
        logger.info("ftpSetting.getAPPCode() : " + ftpSetting.getAPPCode());
        logger.info("ftpSetting.getRESCode() : " + ftpSetting.getRESCode());
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
     * 透過App Code Res Code 取得帳號密碼
     */
    private Map<String, String> loadFtp(String host, String serverHostName, int serverHostNamePort, String APPCode, String RESCode) {
        logger.info("Host               : " + host);
        logger.info("ServerHostName     : " + serverHostName);
        logger.info("ServerHostNamePort : " + serverHostNamePort);
        logger.info("APPCode            : " + APPCode);
        logger.info("RESCode            : " + RESCode);
        logger.info("Is64Bit            : " + is64Bit);
        try {
            /* 透過TrendPwMgmt 取得帳號密碼 */
            TrendPwMgmt trendPwMgmt = new TrendPwMgmt(serverHostName, serverHostNamePort, APPCode, RESCode, is64Bit);
            Map<String, String> data = new HashMap<>();
            data.put("uid", StringUtils.trimToEmpty(trendPwMgmt.getUserId()));
            data.put("pwd", StringUtils.trimToEmpty(trendPwMgmt.getPassword()));
            logger.info(String.format("Host: %s, Uid: %s, Pwd: %s", host, data.get("uid"), data.get("pwd")));
            return data;
        } catch (Exception e) {
            logger.error("trendPwMgmt exception:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根據setting 決定用FTP or FTPS
     *
     * @param setting setting
     * @return FTPClient
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
     * @param setting    FTPClient Type
     * @return Login 狀態
     */
    private boolean loginFTP(FTPClient pFTPClient, PNPFtpSetting setting) {
        try {

            pFTPClient.setDefaultTimeout(1000000);
            pFTPClient.connect(setting.getHost(), setting.getPort());

            boolean lStatus = false;
            String account = setting.getAccount();
            String password = setting.getPassword();
            // 第一次登入
            logger.info("FTP Client Connecting 1...");
            if (account != null && !account.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
                lStatus = pFTPClient.login(account, password);
            }

            if (lStatus) {
                logger.info(String.format("FTP Host Name: %s Login Success!!", setting.getHost()));
                return true;
            } else {
                logger.error(String.format("FTP Host Name: %s Login Fail!! APP[%s] RES[%s]，重新載入FTP連線參數!",
                        setting.getHost(), setting.getAPPCode(), setting.getRESCode()));
            }

            // T 登入失敗重新載入一次設定
            pFTPClient.disconnect();
            logger.info("FTP Client Disconnect!!");
            // T 重新取得FTP 參數設定值
            Map<String, String> ftpSetting = loadFtp(setting);
            logger.info(String.format("ID: %s, PWD: %s", ftpSetting.get("uid"), ftpSetting.get("pwd")));
            logger.info("FTP Client Connecting 2...");
            pFTPClient.connect(setting.getHost(), setting.getPort());
            lStatus = pFTPClient.login(ftpSetting.get("uid"), ftpSetting.get("pwd"));

            if (lStatus) {
                logger.info(String.format("FTP Host Name: %s Login Success!!", setting.getHost()));
            } else {
                logger.error(String.format("FTP Host Name: %s Login Fail!! APP[%s] RES[%s]，重新載入FTP連線參數!",
                        setting.getHost(), setting.getAPPCode(), setting.getRESCode()));
            }

            // T 以系統預設密碼登入
            if (!lStatus) {
                final String DEF_ID = "ACCOUNT";
                final String DEF_PW = "PASSWORD";
                lStatus = pFTPClient.login(DEF_ID, DEF_PW);
                if (lStatus) {
                    logger.info(String.format("FTP Host Name: %s Login Success with Default ID and Password!!", setting.getHost()));
                } else {
                    logger.error(String.format("FTP Host Name: %s Login Fail with Default ID and Password!! Stop Login!!", setting.getHost()));
                }
            }
            return lStatus;

        } catch (Exception e) {
            logger.error("loginFTP:" + e.getMessage());
            logger.error("Exception", e);
            return false;
        }
    }

    /**
     * SMS進行FTPClient Login
     *
     * @param pFTPClient 要進行登入的FTPClient
     * @param setting    FTPClient Type
     * @return Login 狀態
     */
    private boolean smsLoginFTP(FTPClient pFTPClient, PNPFtpSetting setting) {
        try {

            pFTPClient.setDefaultTimeout(1000000);
            pFTPClient.connect(setting.getHost(), setting.getPort());

            // T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
            boolean lStatus = false;
            String account = setting.getSmsAccount();
            String password = setting.getSmsPassword();

            // 第一次登入
            if (account != null && !account.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
                lStatus = pFTPClient.login(account, password);
            }

            if (lStatus) {
                logger.info(String.format("SMS Login FTP: %s 資源密碼系統的帳號密碼登入完成", setting.getSmsHost()));
                return true;
            } else {
                logger.error(String.format("SMS Login FTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，重新載入FTP連線參數!",
                        setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
            }

            // T 登入失敗重新載入一次設定
            pFTPClient.disconnect();
            // T 重新取得FTP 參數設定值
            String host = setting.getSmsHost();
            String serverHostName = setting.getSmsServerHostName();
            int serverHostNamePort = setting.getSmsServerHostNamePort();
            String appCode = setting.getSmsAPPCode();
            String resCode = setting.getSmsRESCode();

            Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, appCode, resCode);
            logger.info("重新載入設定 SMS Login FTP:" + trendPwMgmt.get("uid") + " PWD:" + trendPwMgmt.get("pwd"));
            pFTPClient.connect(setting.getSmsHost(), setting.getSmsPort());
            lStatus = pFTPClient.login(trendPwMgmt.get("uid"), trendPwMgmt.get("pwd"));
            if (lStatus) {
                logger.info("重新載入設定 Login FTP:" + setting.getSmsHost() + "資源密碼系統的帳號密碼登入完成");
            } else {
                logger.error(String.format("loginFTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，改以原系統記錄帳號密碼進行登入!",
                        setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
            }

            // T 以系統預設密碼登入
            if (!lStatus) {
                lStatus = pFTPClient.login("ACCOUNT", "PASSWORD");
                if (lStatus) {
                    logger.info("SMS Login FTP:" + setting.getHost() + "原系統記錄帳號密碼登入完成");
                } else {
                    logger.error("SMS Login FTP:" + setting.getHost() + "原系統記錄帳號密碼登入失敗，停止執行");
                }
            }
            return lStatus;
        } catch (Exception e) {
            logger.error("smsLoginFTP:" + e.getMessage());
            return false;
        }
    }

    /**
     * 進行SFTPClient Login
     */
    private Session loginSFTP(PNPFtpSetting setting) {
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
                    logger.info(String.format("Login SFTP: %s 資源密碼系統的帳號密碼登入完成", setting.getSmsHost()));
                } else {
                    logger.error(String.format("Login SFTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，重新載入FTP連線參數!",
                            setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
                }
            }
            // T 登入失敗重新載入一次設定
            if (!lStatus) {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                session = null;
                // T 重新取得FTP 參數設定值
                Map<String, String> trendPwMgmt = loadFtp(setting);
                logger.info("loginFTP:" + trendPwMgmt.get("uid") + " PWD:" + trendPwMgmt.get("pwd"));
                session = jsch.getSession(trendPwMgmt.get("uid"), setting.getHost(), setting.getPort());
                session.setPassword(trendPwMgmt.get("pwd"));
                session.setConfig(sshConfig);
                session.connect();
                lStatus = session.isConnected();
                if (lStatus) {
                    logger.info(String.format("Login SFTP: %s 資源密碼系統的帳號密碼登入完成", setting.getSmsHost()));
                } else {
                    logger.error(String.format("Login SFTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，改以原系統記錄帳號密碼進行登入!",
                            setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
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
        }
        return lStatus ? session : null;
    }

    /**
     * SMS 進行SFTPClient Login
     */
    private Session smsLoginSFTP(PNPFtpSetting setting) {
        boolean lStatus = false;
        Session session = null;
        try {
            JSch jsch = new JSch();
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");

            // T 先以資源密碼系統取得的帳號密碼進行登入，登入失敗再以系統原本設定的帳號密碼登入系統
            String account = setting.getSmsAccount();
            String password = setting.getSmsPassword();
            if ((account != null && !account.trim().isEmpty()) && (password != null && !password.trim().isEmpty())) {
                session = jsch.getSession(account, setting.getSmsHost(), setting.getSmsPort());
                session.setPassword(password);
                session.setConfig(sshConfig);
                session.connect();
                lStatus = session.isConnected();
                if (lStatus) {
                    logger.info(String.format("SMS Login SFTP: %s 資源密碼系統的帳號密碼登入完成", setting.getSmsHost()));
                } else {
                    logger.error(String.format("SMS Login SFTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，重新載入FTP連線參數!",
                            setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
                }
            }
            // T 登入失敗重新載入一次設定
            if (!lStatus) {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
                session = null;
                // T 重新取得FTP 參數設定值
                String host = setting.getSmsHost();
                String serverHostName = setting.getSmsServerHostName();
                int serverHostNamePort = setting.getSmsServerHostNamePort();
                String appCode = setting.getSmsAPPCode();
                String resCode = setting.getSmsRESCode();

                Map<String, String> trendPwMgmt = loadFtp(host, serverHostName, serverHostNamePort, appCode, resCode);
                logger.info(String.format("SMS Login SFTP: %s PWD: %s", trendPwMgmt.get("uid"), trendPwMgmt.get("pwd")));

                session = jsch.getSession(trendPwMgmt.get("uid"), setting.getHost(), setting.getPort());
                session.setPassword(trendPwMgmt.get("pwd"));
                session.setConfig(sshConfig);
                session.connect();
                lStatus = session.isConnected();
                if (lStatus) {
                    logger.info(String.format("SMS Login SFTP: %s 資源密碼系統的帳號密碼登入完成", setting.getSmsHost()));
                } else {
                    logger.error(String.format("SMS Login SFTP: %s 資源密碼系統的帳號密碼登入失敗 APP[%s] RES[%s]，重新載入FTP連線參數!",
                            setting.getSmsHost(), setting.getSmsAPPCode(), setting.getSmsRESCode()));
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
        }
        return lStatus ? session : null;
    }


    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @param setting    Ftp Server Type
     * @return 下載的檔案內容
     */
    private Map<String, byte[]> downloadMultipleFileInFTPForDev(String pDirectory, String extension, PNPFtpSetting setting) {
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        FTPClient ftpClient = getFtpClient(setting);

        /*
         *  三竹來源  MITAKE = "1"
         *  互動來源  EVERY8D = "2"
         *  明宣來源  MING = "3"
         *  UNICA來源   UNICA = "4"
         */
        String source = setting.getChannelId();

        try {
            ftpClient.connect(setting.getHost(), setting.getPort());
            ftpClient.login(setting.getAccount(), setting.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);
            ftpClient.setControlEncoding(setting.getFileEncoding());
            ftpClient.changeWorkingDirectory(pDirectory);
            ftpClient.setStrictReplyParsing(true);
            // 取得FTP中的files
            FTPFile[] files = ftpClient.listFiles();

            if (!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                //三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    if (!file.isDirectory() && fileName.endsWith("txt")) {
                        logger.info("start rename!!!!");
                        logger.info("fileName :" + fileName);
                        logger.info("ftpClient.printWorkingDirectory() :" + ftpClient.printWorkingDirectory());
                        logger.info(ftpClient.rename(fileName, fileName + ".ok"));
                    }
                }
                FTPFile[] filesOK = ftpClient.listFiles();
                for (FTPFile file : filesOK) {
                    String fileName = file.getName();
                    if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
                        ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                        ftpClient.retrieveFile(fileName, lDataTemp);
                        lDataTemp.flush();
                        lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                        lDataTemp.close();
                    }
                }

            } else {
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
                        //ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                        ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                        ftpClient.retrieveFile(fileName, lDataTemp);
                        lDataTemp.flush();
                        lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                        lDataTemp.close();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.error("downloadMultipleFileInFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
                logger.error(ex);
                logger.error("downloadMultipleFileInFTPForDev Error: " + ex.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) 根據類型指向不同FTP
     *
     * @param source    來源
     * @param directory 要下載檔案所在路徑
     * @param extension 要下載檔案副檔名
     * @param setting   各來源各自連線資訊
     * @return 下載的檔案內容Map
     * @see LoadFtpPnpDataTask#parseDataFlow
     * @see LoadFtpPnpDataTask#transFileToSMSFlow
     */
    public Map<String, byte[]> downloadMultipleFileByType(String source, String directory, String extension, PNPFtpSetting setting) {
        logger.info(String.format("Protocol           : %s", setting.getProtocol().equalsIgnoreCase("sftp")));
        logger.info(String.format("IsPNPFtpTypeDevelop: %s", CoreConfigReader.isPNPFtpTypeDevelop()));

        if (setting.getProtocol().equalsIgnoreCase("sftp")) {
            if (CoreConfigReader.isPNPFtpTypeDevelop()) {
                /* 開發環境FTP */
                return downloadMultipleFileInSFTPForDev(directory, extension, setting);
            } else {
                /* 正式環境FTP */
                return downloadMultipleFileInSFTP(directory, extension, setting);
            }
        } else {
            if (CoreConfigReader.isPNPFtpTypeDevelop()) {
                /* 開發環境FTP */
                return downloadMultipleFileInFTPForDev(directory, extension, setting);
            } else {
                /* 正式環境FTP */
                return downloadMultipleFileInFTP(directory, extension, setting);
            }
        }
    }

    /**
     * 從FTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @param setting    Ftp Server Type
     * @return 下載的檔案內容
     */
    private Map<String, byte[]> downloadMultipleFileInFTP(String pDirectory, String extension, PNPFtpSetting setting) {
        FTPClient ftpClient = null;
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        // 判斷來源
        String source = setting.getChannelId();
        try {
            ftpClient = getFtpClient(setting);
            if (loginFTP(ftpClient, setting)) {
                ftpClient.changeWorkingDirectory(pDirectory);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                FTPFile[] lFiles = ftpClient.listFiles();
                logger.info("downloadMultipleFileInFTP:" + pDirectory + " File size:" + lFiles.length);
                if (!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                    //三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
                    for (FTPFile file : lFiles) {
                        String fileName = file.getName();
                        if (!file.isDirectory() && fileName.toUpperCase().endsWith("TXT")) {
                            logger.info("start rename!!!!");
                            logger.info("fileName :" + fileName);
                            logger.info("ftpClient.printWorkingDirectory() :" + ftpClient.printWorkingDirectory());
                            logger.info(ftpClient.rename(fileName, fileName + ".ok"));
                        }
                    }
                    FTPFile[] filesOk = ftpClient.listFiles();
                    for (FTPFile file : filesOk) {
                        String fileName = file.getName();
                        if (!file.isDirectory() && fileName.toUpperCase().endsWith("TXT.OK")) {
                            ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                            ftpClient.retrieveFile(fileName, lDataTemp);
                            lDataTemp.flush();
                            lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                            lDataTemp.close();
                        }
                    }
                } else {
                    ByteArrayOutputStream lDataTemp;
                    for (FTPFile lFtpFile : lFiles) {
                        String fileName = lFtpFile.getName();
                        logger.info("downloadMultipleFileInFTP:" + pDirectory + " File :" + fileName);
                        if (!lFtpFile.isDirectory() && fileName.toUpperCase().endsWith(extension.toUpperCase() + ".OK")) {
                            lDataTemp = new ByteArrayOutputStream();
                            //ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
                            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                            ftpClient.retrieveFile(fileName, lDataTemp);
                            lDataTemp.flush();
                            lReturnDataMap.put(lFtpFile.getName(), lDataTemp.toByteArray());
                            lDataTemp.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.error("downloadMultipleFileInFTP Exception: " + e.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @param setting    Ftp Server Type
     * @return 下載的檔案內容
     */
    private Map<String, byte[]> downloadMultipleFileInSFTP(String pDirectory, String extension, PNPFtpSetting setting) {
        Map<String, byte[]> lReturnDatas = new HashMap<>();
        ChannelSftp channelSftp = null;
        Session session = null;
        //判斷來源
        String source = setting.getChannelId();

        try {
            session = this.loginSFTP(setting);
            if (session == null) {
                logger.error("downloadMultipleFileInSFTP session null");
                return lReturnDatas;
            }
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            if (channelSftp.isConnected()) {
                channelSftp.cd(pDirectory);
                // 取得FTP中的files
                if (!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                    //三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
                    Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
                    for (ChannelSftp.LsEntry lFtpFile : list) {
                        logger.info("sftp start rename!!!!");
                        String fileName = lFtpFile.getFilename();
                        logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                        channelSftp.rename(fileName, fileName + ".ok");
                    }

                    Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension + ".ok");
                    for (ChannelSftp.LsEntry lFtpFile : list) {
                        String fileName = lFtpFile.getFilename();
                        logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                        ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                        channelSftp.get(fileName, lDataTemp);
                        lDataTemp.flush();
                        lReturnDatas.put(fileName, lDataTemp.toByteArray());
                        lDataTemp.close();
                    }

                } else {
                    Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension + ".ok");
                    for (ChannelSftp.LsEntry lFtpFile : listOk) {
                        String fileName = lFtpFile.getFilename();
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
                        logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
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
//					logger.info("downloadMultipleFileInSFTP:" + pDirectory + " File :" + fileName);
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
            } else {
                logger.error("downloadMultipleFileInSFTP channelSftp false");
            }


        } catch (Exception ex) {
            logger.error("downloadMultipleFileInSFTP Error: " + ex.getMessage());
            logger.error(ex);
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception ex) {
                logger.error("downloadMultipleFileInSFTP Error: " + ex.getMessage());
            }
        }

        return lReturnDatas;
    }

    /**
     * 從SFTP伺服器上下載指定數量檔案(下載檔案順序採檔案建立日期，先進先出) (測試環境用)
     *
     * @param pDirectory 要下載檔案所在路徑
     * @param extension  要下載檔案副檔名
     * @param setting    Ftp Server Type
     * @return 下載的檔案內容
     * @see this#downloadMultipleFileByType
     */
    private Map<String, byte[]> downloadMultipleFileInSFTPForDev(String pDirectory, String extension,
                                                                 PNPFtpSetting setting) {
        Map<String, byte[]> lReturnDataMap = new HashMap<>();
        ChannelSftp channelSftp = null;
        Session session = null;

        /*
         *  三竹來源  SOURCE_MITAKE = "1"
         *  互動來源  SOURCE_EVERY8D = "2"
         *  明宣來源  SOURCE_MING = "3"
         *  UNICA來源   SOURCE_UNICA = "4"
         */
        String source = setting.getChannelId();

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
                    if (!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                        //三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
                        Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension);
                        for (ChannelSftp.LsEntry lFtpFile : list) {
                            logger.info("sftp start rename!!!!");
                            String fileName = lFtpFile.getFilename();
                            logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                            channelSftp.rename(fileName, fileName + ".ok");
                        }

                        Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension + ".ok");
                        for (ChannelSftp.LsEntry lFtpFile : list) {
                            String fileName = lFtpFile.getFilename();
                            logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                            ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                            channelSftp.get(fileName, lDataTemp);
                            lDataTemp.flush();
                            lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                            lDataTemp.close();
                        }

                    } else {
                        Vector<ChannelSftp.LsEntry> listOk = channelSftp.ls("*." + extension + ".ok");
                        for (ChannelSftp.LsEntry lFtpFile : listOk) {
                            String fileName = lFtpFile.getFilename();
                            //ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
                            fileName = fileName.substring(0, fileName.lastIndexOf("."));
                            logger.info("downloadMultipleFileInSFTPForDev fileName:" + fileName);
                            ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
                            channelSftp.get(fileName, lDataTemp);
                            lDataTemp.flush();
                            lReturnDataMap.put(fileName, lDataTemp.toByteArray());
                            lDataTemp.close();
                        }
                    }
                } else {
                    logger.error(" downloadMultipleFileInSFTPForDev channelSftp isConnected fail ");
                }

            } else {
                logger.error(" downloadMultipleFileInSFTPForDev session isConnected fail ");
            }


        } catch (Exception ex) {
            logger.error(ex);
            logger.error(" downloadMultipleFileInSFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception ex) {
                logger.error(" downloadMultipleFileInSFTPForDev Error: " + ex.getMessage());
            }
        }
        return lReturnDataMap;
    }

    /**
     * 刪除指定檔案 根據類型指向不同FTP
     *
     * @param directory  原始目錄
     * @param pFileNames 要刪除的檔案名稱
     * @param setting    FTP Config
     * @see LoadFtpPnpDataTask#parseDataFlow
     */
    public void deleteFileByType(String directory, String[] pFileNames, PNPFtpSetting setting) {
        if (setting.getProtocol().equalsIgnoreCase("sftp")) {
            if (CoreConfigReader.isPNPFtpTypeDevelop()) {
                /* 開發環境 */
                deleteFileInSFTPForDev(directory, pFileNames, setting);
            } else {
                /* 正式環境 */
                deleteFileInSFTP(directory, pFileNames, setting);
            }
        } else {
            if (CoreConfigReader.isPNPFtpTypeDevelop()) {
                /* 開發環境 */
                deleteFileInFTPForDev(directory, pFileNames, setting);
            } else {
                /* 正式環境 */
                deleteFileInFTP(directory, pFileNames, setting);
            }
        }
    }

    /**
     * 刪除指定檔案
     *
     * @param pDirectory 原始目錄
     * @param pFileNames 要刪除的檔案名稱
     * @param setting    FTP Config
     * @see this#deleteFileByType
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
                    setting.removeFileNames(lFileName);
                }
                if (source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) || source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                    for (String lFileName : pFileNames) {
                        channelSftp.rm(lFileName + ".ok");
                    }
                }
            } else {
                logger.error("deleteFileInSFTP channelSftp: " + channelSftp.isConnected());
            }

        } catch (Exception ex) {
            logger.error(ex);
            logger.error("deleteFileInSFTP Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
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
     * @param setting    FTP Config
     * @see this#deleteFileByType
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

            /* Session is Not Connected */
            if (!session.isConnected()) {
                logger.error("deleteFileInSFTPForDev session: " + session.isConnected());
                return;
            }

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            /* Channel is Not Connected */
            if (!channelSftp.isConnected()) {
                logger.error("deleteFileInSFTPForDev channelSftp: " + channelSftp.isConnected());
                return;
            }

            /* Session and Channel is Connected */
            channelSftp.cd(pDirectory);
            for (String lFileName : pFileNames) {
                channelSftp.rm(lFileName);
                setting.removeFileNames(lFileName);
            }
            if (source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) || source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                for (String lFileName : pFileNames) {
                    channelSftp.rm(lFileName + ".ok");
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.error("deleteFileInSFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (channelSftp != null && channelSftp.isConnected()) {
                    channelSftp.disconnect();
                }
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
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
     * @param setting    FTP Config
     */
    private void deleteFileInFTPForDev(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
        String source = setting.getChannelId();
        FTPClient ftpClient = getFtpClient(setting);
        try {
            ftpClient.connect(setting.getHost(), setting.getPort());
            ftpClient.login(setting.getAccount(), setting.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);
            ftpClient.setControlEncoding(setting.getFileEncoding());
            ftpClient.changeWorkingDirectory(pDirectory);
            for (String processFileName : pFileNames) {
                boolean success = ftpClient.deleteFile(processFileName);
                setting.removeFileNames(processFileName);
                if (!success) {
                    logger.error("remove fail: " + processFileName);
                }
                if (source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) || source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                    success = ftpClient.deleteFile(processFileName + ".ok");
                    if (!success) {
                        logger.error("remove fail: " + processFileName + ".ok");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.error("deleteFileInFTPForDev Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
                logger.error(ex);
                logger.error("deleteFileInFTPForDev Error: " + ex.getMessage());
            }
        }
    }

    /**
     * 刪除指定檔案
     *
     * @param pDirectory 原始目錄
     * @param pFileNames 要刪除的檔案名稱
     * @param setting    FTP Config
     */
    private void deleteFileInFTP(String pDirectory, String[] pFileNames, PNPFtpSetting setting) {
        String source = setting.getChannelId();
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient(setting);

            /* FTP Login Fail */
            if (!loginFTP(ftpClient, setting)) {
                return;
            }

            ftpClient.changeWorkingDirectory(pDirectory);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            for (String processFileName : pFileNames) {
                boolean success = ftpClient.deleteFile(processFileName);
                setting.removeFileNames(processFileName);
                if (!success) {
                    logger.error("remove fail: " + processFileName);
                }
                if (source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) || source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
                    success = ftpClient.deleteFile(processFileName + ".ok");
                    if (!success) {
                        logger.error("remove fail: " + processFileName + ".ok");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            logger.error("deleteFileInFTP Exception" + e.getMessage());
        } finally {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (Exception e) {
                logger.error("deleteFileInFTP Exception" + e.getMessage());
            }
        }
    }


    /**
     * 上傳指定檔案 根據類型指向不同FTP
     *
     * @param uploadIs  上傳檔案
     * @param fileName  上傳目錄
     * @param targetDir 要上傳的檔案名稱
     * @param setting   FTP Config
     * @throws IOException IOException
     */
    public void uploadFileByType(InputStream uploadIs, String fileName, String targetDir, PNPFtpSetting setting) throws IOException {
        if (setting.getProtocol().equalsIgnoreCase("sftp")) {
            uploadFileInSFTP(uploadIs, fileName, targetDir, setting);
        } else {
            uploadFileInFTP(uploadIs, fileName, targetDir, setting);
        }
    }

    private void uploadFileInFTP(InputStream targetStream, String fileName, String targetDir, PNPFtpSetting setting) throws IOException {
        logger.info("start uploadFileInFTP ");

        logger.info(" targetStream      :" + targetStream);
        logger.info(" fileName          :" + fileName);
        logger.info(" targetDir         :" + targetDir);
        logger.info(" PNPFtpSetting     :" + setting);

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(setting.getSmsHost(), setting.getSmsPort());
            logger.info("loginFTP : " + (smsLoginFTP(ftpClient, setting) ? "OK" : "fail"));
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);
            logger.info(setting.getFileEncoding());
            ftpClient.setControlEncoding(setting.getFileEncoding());
            ftpClient.changeWorkingDirectory(targetDir);

            //上傳檔案
            ftpClient.storeFile(fileName, targetStream);
        } catch (Exception e) {
            logger.error(e);
            logger.error("uploadFileInFTP Exception" + e.getMessage());
        } finally {
            //關閉檔案
            targetStream.close();
            //登出
            ftpClient.logout();
            //關閉連線
            ftpClient.disconnect();
        }
    }

    private void uploadFileInSFTP(InputStream uploadIs, String fileName, String targetDir, PNPFtpSetting setting) {
        logger.info("start uploadFileInSFTP ");
        ChannelSftp channelSftp;
        Session session;
        try {
            session = smsLoginSFTP(setting);
            if (session == null) {
                logger.error("uploadFileInSFTP connection failed");
                return;
            }
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            if (!channelSftp.isConnected()) {
                logger.error("uploadFileInSFTP channelSftp: " + channelSftp.isConnected());
                return;
            }
            try {
                Vector content = channelSftp.ls(targetDir);
                if (content == null) {
                    boolean mkdirIsSuccess = mkdir(targetDir, channelSftp);
                    logger.info("mkdirIsSuccess:" + mkdirIsSuccess);
                }
            } catch (SftpException e) {
                boolean mkdirIsSuccess = mkdir(targetDir, channelSftp);
                logger.info("mkdirIsSuccess:" + mkdirIsSuccess);
            }
            channelSftp.cd(targetDir);
            channelSftp.put(uploadIs, new String(fileName.getBytes(), setting.getFileEncoding()));
        } catch (Exception e) {
            logger.error(e);
            logger.error("uploadFileInSFTP Exception" + e.getMessage());
        }
    }

    private boolean mkdir(String path, ChannelSftp sftpChannel) {
        try {
            if (!path.startsWith("/")) {
                return false;
            }
            if (isNotExist(path, sftpChannel)) {
                String[] arr = path.substring(1).split("/");

                StringBuilder sb = new StringBuilder();
                for (String str : arr) {
                    sb.append("/");
                    sb.append(str);
                    if (isNotExist(sb.toString(), sftpChannel)) {
                        sftpChannel.mkdir(sb.toString());
                    }
                }
            }
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    private boolean isNotExist(String path, ChannelSftp sftpChannel) {
        try {
            return sftpChannel.ls(path) == null;
        } catch (SftpException e) {
            return false;
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
//	private Map<String, byte[]> downloadMultipleFile(String pDirectory, int pFileCount, FtpSetting setting) {
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
