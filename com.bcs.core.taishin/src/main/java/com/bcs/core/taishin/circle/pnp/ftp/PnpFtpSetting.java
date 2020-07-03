package com.bcs.core.taishin.circle.pnp.ftp;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "PnpRecorder")
public class PnpFtpSetting {

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
    private String uploadToEvery8dPath;


    private List<String> fileNames = new ArrayList<>();

    public static PnpFtpSetting build(PnpFtpSourceEnum type) {
        /* 前方來源系統設定 */
        String englishName = type.english;
        PnpFtpSetting pnpFtpSetting = new PnpFtpSetting();
        String fileEncoding = CoreConfigReader.getString(CONFIG_STR.PNP_READ_LINES_ENCODE, true, false);
        if (StringUtils.isBlank(fileEncoding)) {
            log.warn("Properties [pnp.readLines.encode] does not found or value is blank!!");
        }
        String serverHostName = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVER_HOST_NAME.toString() + englishName, true, false);
        if (StringUtils.isBlank(serverHostName)) {
            log.warn("Properties [pnp.ftp.serverHostName.] does not found or value is blank!!");
        }
        int serverHostPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVER_HOST_NAME_PORT.toString() + englishName, true, false);
        if (serverHostPort <= 0) {
            log.warn("Properties [pnp.ftp.serverHostName.port.] does not found or value is blank!!");
        }
        String account = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR.toString() + englishName, true, false);
        if (StringUtils.isBlank(account)) {
            log.warn("Properties [pnp.ftp.usr.] does not found or value is blank!!");
        }
        String password = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS.toString() + englishName, true, false);
        if (StringUtils.isBlank(password)) {
            log.warn("Properties [pnp.ftp.pass.] does not found or value is blank!!");
        }
        String ftpHost = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST.toString() + englishName, true, false);
        if (StringUtils.isBlank(ftpHost)) {
            log.warn("Properties [pnp.ftp.host.] does not found or value is blank!!");
        }
        int ftpPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT.toString() +englishName, true, false);
        if (ftpPort <= 0) {
            log.warn("Properties [pnp.ftp.port.] does not found or value is blank!!");
        }
        String appCode = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APP_CODE.toString() + englishName, true, false);
        if (StringUtils.isBlank(appCode)) {
            log.warn("Properties [pnp.ftp.APPCode.] does not found or value is blank!!");
        }
        String resCode = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RES_CODE.toString() + englishName, true, false);
        if (StringUtils.isBlank(resCode)) {
            log.warn("Properties [pnp.ftp.RESCode.] does not found or value is blank!!");
        }
        String downloadPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH.toString() + englishName, true, false);
        if (StringUtils.isBlank(downloadPath)) {
            log.warn("Properties [pnp.ftp.download.path] does not found or value is blank!!");
        }
        String smsUploadPath = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH.toString() + englishName, true, false);
        if (StringUtils.isBlank(smsUploadPath)) {
            log.warn("Properties [pnp.sms.upload.path.] does not found or value is blank!!");
        }
        String downloadToLocalPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH.toString() + englishName, true, false);
        if (StringUtils.isBlank(downloadToLocalPath)) {
            log.warn("Properties [pnp.ftp.download.to.local.path.] does not found or value is blank!!");
        }
        String ftpProtocol = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL.toString() + englishName, true, false);
        if (StringUtils.isBlank(ftpProtocol)) {
            log.warn("Properties [pnp.ftp.protocol.] does not found or value is blank!!");
        }
        String procFlow = CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW.toString() + englishName, true, false);
        if (StringUtils.isBlank(procFlow)) {
            log.warn("Properties [pnp.proc.flow] does not found or value is blank!!");
        }
        String smsUploadToEvery8dPath = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_TO_EVERY8D_PATH.toString() + englishName, false, true);
        if (StringUtils.isBlank(smsUploadPath)) {
            log.warn("Properties [pnp.sms.upload.to.every8d.path.{}] does not found or value is blank!!", englishName);
        }

        pnpFtpSetting.setFileEncoding(fileEncoding);
        pnpFtpSetting.setChannelId(englishName);
        pnpFtpSetting.setServerHostName(serverHostName);
        pnpFtpSetting.setServerHostNamePort(serverHostPort);
        pnpFtpSetting.setAccount(account);
        pnpFtpSetting.setPassword(password);
        pnpFtpSetting.setHost(ftpHost);
        pnpFtpSetting.setPort(ftpPort);
        pnpFtpSetting.setAPPCode(appCode);
        pnpFtpSetting.setRESCode(resCode);
        pnpFtpSetting.setPath(downloadPath);
        pnpFtpSetting.setUploadPath(smsUploadPath);
        pnpFtpSetting.setDownloadSavePath(downloadToLocalPath);
        pnpFtpSetting.setProtocol(ftpProtocol);
        pnpFtpSetting.setFlow(procFlow);
        pnpFtpSetting.setUploadToEvery8dPath(smsUploadToEvery8dPath);

        String smsServerHostName = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVER_HOST_NAME.toString() + englishName, true, false);
        int smsServerHostPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVER_HOST_NAME_PORT.toString() + englishName, true, false);
        String smsAccount = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR.toString() + englishName, true, false);
        String smsPassword = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS.toString() + englishName, true, false);
        String smsHost = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST.toString() + englishName, true, false);
        int smsPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT.toString() + englishName, true, false);
        String smsAppCode = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APP_CODE.toString() + englishName, true, false);
        String smsResCode = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RES_CODE.toString() + englishName, true, false);
        String smsProtocol = CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL.toString() + englishName, true, false);

        /* SMS系統設定檔 */
        pnpFtpSetting.setSmsServerHostName(smsServerHostName);
        pnpFtpSetting.setSmsServerHostNamePort(smsServerHostPort);
        pnpFtpSetting.setSmsAccount(smsAccount);
        pnpFtpSetting.setSmsPassword(smsPassword);
        pnpFtpSetting.setSmsHost(smsHost);
        pnpFtpSetting.setSmsPort(smsPort);
        pnpFtpSetting.setSmsAPPCode(smsAppCode);
        pnpFtpSetting.setSmsRESCode(smsResCode);
        pnpFtpSetting.setSmsProtocol(smsProtocol);
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
        fileNames.remove(file);
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

	public String getUploadToEvery8dPath() {
		return uploadToEvery8dPath;
	}

	public void setUploadToEvery8dPath(String uploadToEvery8dPath) {
		this.uploadToEvery8dPath = uploadToEvery8dPath;
	}


}
