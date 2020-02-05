package com.bcs.core.taishin.circle.pnp.ftp;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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


    private List<String> fileNames = new ArrayList<>();

    public static PnpFtpSetting build(PnpFtpSourceEnum type) {
        /* 前方來源系統設定 */
        String englishName = type.english;
        PnpFtpSetting pnpFtpSetting = new PnpFtpSetting();
        pnpFtpSetting.setFileEncoding(CoreConfigReader.getString(CONFIG_STR.PNP_READ_LINES_ENCODE, true, false));
        pnpFtpSetting.setChannelId(englishName);
        pnpFtpSetting.setServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_SERVER_HOST_NAME.toString() + englishName, true, false));
        pnpFtpSetting.setServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_SERVER_HOST_NAME_PORT.toString() + englishName, true, false));
        pnpFtpSetting.setAccount(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR.toString() + englishName, true, false));
        pnpFtpSetting.setPassword(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS.toString() + englishName, true, false));
        pnpFtpSetting.setHost(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST.toString() + englishName, true, false));
        pnpFtpSetting.setPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT.toString() +englishName, true, false));
        pnpFtpSetting.setAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_APP_CODE.toString() + englishName, true, false));
        pnpFtpSetting.setRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_RES_CODE.toString() + englishName, true, false));
        pnpFtpSetting.setPath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH.toString() + englishName, true, false));
        pnpFtpSetting.setUploadPath(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_UPLOAD_PATH.toString() + englishName, true, false));
        pnpFtpSetting.setDownloadSavePath(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH.toString() + englishName, true, false));
        pnpFtpSetting.setProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PROTOCOL.toString() + englishName, true, false));
        pnpFtpSetting.setFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW.toString() + englishName, true, false));

        /* SMS系統設定檔 */
        pnpFtpSetting.setSmsServerHostName(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_SERVER_HOST_NAME.toString() + englishName, true, false));
        pnpFtpSetting.setSmsServerHostNamePort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_SERVER_HOST_NAME_PORT.toString() + englishName, true, false));
        pnpFtpSetting.setSmsAccount(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_USR.toString() + englishName, true, false));
        pnpFtpSetting.setSmsPassword(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PASS.toString() + englishName, true, false));
        pnpFtpSetting.setSmsHost(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_HOST.toString() + englishName, true, false));
        pnpFtpSetting.setSmsPort(CoreConfigReader.getInteger(CONFIG_STR.PNP_SMS_PORT.toString() + englishName, true, false));
        pnpFtpSetting.setSmsAPPCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_APP_CODE.toString() + englishName, true, false));
        pnpFtpSetting.setSmsRESCode(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_RES_CODE.toString() + englishName, true, false));
        pnpFtpSetting.setSmsProtocol(CoreConfigReader.getString(CONFIG_STR.PNP_SMS_PROTOCOL.toString() + englishName, true, false));
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


}
