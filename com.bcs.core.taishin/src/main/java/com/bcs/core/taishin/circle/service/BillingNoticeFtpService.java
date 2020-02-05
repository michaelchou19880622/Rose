package com.bcs.core.taishin.circle.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeDetailRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeMainRepository;
import com.bcs.core.taishin.circle.ftp.FtpService;
import com.bcs.core.taishin.circle.ftp.FtpSetting;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "BNRecorder")
@Service
public class BillingNoticeFtpService {

    private BillingNoticeMainRepository billingNoticeMainRepository;
    private BillingNoticeDetailRepository billingNoticeDetailRepository;
    private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
    //    private ServerInfoService serverInfoService;
    private static FtpService ftpService = new FtpService();
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("BN-FTP-Scheduled-%d")
                    .daemon(true).build()
    );

    private ScheduledFuture<?> scheduledFuture = null;

    @Autowired
    public BillingNoticeFtpService(
            BillingNoticeMainRepository billingNoticeMainRepository,
            BillingNoticeDetailRepository billingNoticeDetailRepository,
            BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository
//            ServerInfoService serverInfoService
    ) {
        this.billingNoticeMainRepository = billingNoticeMainRepository;
        this.billingNoticeDetailRepository = billingNoticeDetailRepository;
        this.billingNoticeContentTemplateMsgRepository = billingNoticeContentTemplateMsgRepository;
//        this.serverInfoService = serverInfoService;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        String unit = CoreConfigReader.getString(CONFIG_STR.BN_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.BN_FTP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.error("BillingNoticeFtpService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::ftpProcess, 0, time, TimeUnit.valueOf(unit));
    }


    /**
     * 執行流程
     */
    private void ftpProcess() {
        log.info("StartCircle....");
        boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIG_SWITCH, true, false);
        String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true, false);
        String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true, false);

        if (!bigSwitch) {
            return;
        }

        try {
            List<FtpSetting> ftpSettingList = ftpService.getFtpSettings();
            ftpSettingList.forEach(ftpSetting -> log.info("Ftp Path: {}", ftpSetting.getPath()));
            Map<String, byte[]> lReturnDataMap = downloadFtpFile(fileExtension, ftpSettingList);
            if (lReturnDataMap.isEmpty()) {
                return;
            }
            saveObjToDb(parseDataToObj(downloadSavePath, lReturnDataMap, ftpSettingList));
            removeFtpFileProcess(lReturnDataMap, ftpSettingList);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    /**
     * 由多個FTP下載檔案
     *
     * @param fileExtension  file extension
     * @param ftpSettingList ftp setting list
     * @return 所有下載的檔案
     */
    private Map<String, byte[]> downloadFtpFile(String fileExtension, List<FtpSetting> ftpSettingList) {
        Map<String, byte[]> map = new HashMap<>();
        int i = 0;
        for (FtpSetting ftpSetting : ftpSettingList) {
            i++;
            ftpSetting.clearFileNames();

            if (StringUtils.isBlank(ftpSetting.getAccount())
                    || StringUtils.isBlank(ftpSetting.getPassword())) {
                log.error("FTP {} account or password is blank!!", i);
                continue;
            }
            Map<String, byte[]> returnDataMap = ftpService.downloadMultipleFileByType(ftpSetting.getPath(), fileExtension, ftpSetting);
            map.putAll(returnDataMap);

            returnDataMap.keySet().forEach(ftpSetting::addFileNames);
        }
        return map;
    }

    private List<BillingNoticeMain> parseDataToObj(final String downloadSavePath, final Map<String, byte[]> lReturnDataMap, List<FtpSetting> ftpSettingList) throws IOException {
        List<BillingNoticeMain> mainList = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : lReturnDataMap.entrySet()) {
            final String fileName = entry.getKey();
            final String encoding = StandardCharsets.UTF_8.name();
            ftpSettingList.stream().filter(ftpSetting -> ftpSetting.containsFileName(fileName)).forEach(ftpSetting ->
                    log.info("{} containsFileName encoding: {}", fileName, ftpSetting.getFileEncoding())
            );
            log.info("{} encoding: {}", fileName, encoding);
            final byte[] fileData = lReturnDataMap.get(fileName);
            log.info("File: {}{}{}", new Object[]{downloadSavePath, File.separator, fileName});

            final File targetFile = new File(downloadSavePath + File.separator + fileName);
            FileUtils.writeByteArrayToFile(targetFile, fileData);
            try (InputStream targetStream = new FileInputStream(targetFile)) {
                List<String> fileContents = IOUtils.readLines(targetStream, encoding);
                List<BillingNoticeMain> billingNoticeMains = parseFtpFile(fileName, fileContents);
                mainList.addAll(billingNoticeMains);
            }
        }
        return mainList;
    }

    private void saveObjToDb(List<BillingNoticeMain> mainList) {
        for (BillingNoticeMain billingNoticeMain : mainList) {
            saveDb(billingNoticeMain);
            updateStatus(billingNoticeMain);
        }
    }

    private void removeFtpFileProcess(Map<String, byte[]> lReturnDataMap, List<FtpSetting> ftpSettings) {
        if (lReturnDataMap.isEmpty()) {
            log.info("Data Map is Empty!!");
            return;
        }
        ftpSettings.forEach(ftpSetting -> ftpService.deleteFileByType(ftpSetting.getPath(), ftpSetting.getFileNames().toArray(new String[0]), ftpSetting));
    }


    /**
     * 轉換Array[1~*]的內容成物件
     *
     * @param fileContents file Contents
     * @return map
     */
    private Map<String, List<BillingNoticeFtpDetail>> parseDetail(List<String> fileContents) {
        List<BillingNoticeFtpDetail> details = new ArrayList<>();
        /* Parse Content */
        for (int i = 1; i < fileContents.size(); i++) {
            if (StringUtils.isBlank(fileContents.get(i))) {
                continue;
            }
            log.info("fileContents" + fileContents);
            String[] detailData = fileContents.get(i).split("\\|");
            switch (detailData.length) {
                case 4:
                case 3:
                    BillingNoticeFtpDetail detail = new BillingNoticeFtpDetail();
                    detail.setUid(detailData[0]);
                    detail.setTitle(detailData[1]);
                    detail.setText(detailData[2]);
                    detail.setTemplate(detailData.length == 4 ? detailData[3] : "default");
                    details.add(detail);
                    break;
                default:
                    log.error("parseFtpFile error Data:" + Arrays.toString(detailData));
                    break;
            }
        }
        log.info("details : " + details);
        Map<String, List<BillingNoticeFtpDetail>> resultMap = new HashMap<>();

        details.forEach(detail -> {
            String key = detail.getTemplate();
            if (resultMap.containsKey(key)) {
                List<BillingNoticeFtpDetail> list = resultMap.get(key);
                list.add(detail);
            } else {
                List<BillingNoticeFtpDetail> list = new ArrayList<>();
                list.add(detail);
                resultMap.put(key, list);
            }
        });
        log.info("BillingNoticeFtpDetail resultMap : " + resultMap);
        return resultMap;
    }

    /**
     * 整理txt data
     *
     * @param origFileName origFileName
     * @param fileContents fileContents
     */
    private List<BillingNoticeMain> parseFtpFile(String origFileName, List<String> fileContents) {
        log.info("Parse Ftp File Start!!");

        if (fileContents.isEmpty()) {
            log.info("File Content is Empty!!");
            return Collections.emptyList();
        }

        /* Header */
        String header = fileContents.get(0);
        if (!validateHeader(header)) {
            log.info("Header Valid Fail!!");
            return Collections.emptyList();
        }

        String[] splitHeaderData = header.split("\\|");
        log.info("Header Content Array: {}", DataUtils.toPrettyJsonUseJackson(splitHeaderData));

        // TemplateTitle => Details
        Map<String, List<BillingNoticeFtpDetail>> resultMap = parseDetail(fileContents);
        log.info("File Content Map: {}", DataUtils.toPrettyJsonUseJackson(resultMap));

        List<BillingNoticeMain> mainList = new ArrayList<>();
        for (Map.Entry<String, List<BillingNoticeFtpDetail>> entry : resultMap.entrySet()) {
            BillingNoticeContentTemplateMsg template = getBillingNoticeTemplate(entry.getKey());

            if (template == null) {
                log.error("Template Doesn't exist!! [{}]", entry.getKey());
                return Collections.emptyList();
            }

            /* Main */
            BillingNoticeMain main = setMain(
                    origFileName,
                    mainList.size(),
                    splitHeaderData[0],
                    splitHeaderData[1],
                    splitHeaderData[2],
                    template
            );

            /* Detail */
            List<BillingNoticeDetail> detailList = new ArrayList<>();
            resultMap.get(entry.getKey()).forEach(ftpDetail -> detailList.add(setDetail(main, ftpDetail)));
            main.setDetails(detailList);
            mainList.add(main);
        }
        return mainList;
    }

    private Calendar getExpireTime() {
        Calendar expiryTime = Calendar.getInstance();
        expiryTime.add(Calendar.HOUR, 3);
        return expiryTime;
    }

    private BillingNoticeContentTemplateMsg getBillingNoticeTemplate(String key) {
        List<BillingNoticeContentTemplateMsg> templateList = billingNoticeContentTemplateMsgRepository.findMainOnTemplateByTitle(key);
        if (templateList != null && !templateList.isEmpty()) {
            return templateList.get(0);
        }
        return getDefaultTemplate();
    }

    private BillingNoticeContentTemplateMsg getDefaultTemplate() {
        log.info("Use Default Template.");
        List<BillingNoticeContentTemplateMsg> defaultTemplates = billingNoticeContentTemplateMsgRepository.findByTemplateTitleAndProductSwitchOn("default");
        if (defaultTemplates != null && !defaultTemplates.isEmpty()) {
            return defaultTemplates.get(0);
        }
        log.info("Default Template Doesn't exist!");
        return null;
    }

    private BillingNoticeDetail setDetail(BillingNoticeMain main, BillingNoticeFtpDetail ftpDetail) {
        BillingNoticeDetail detail = new BillingNoticeDetail();
        detail.setMsgType(BillingNoticeDetail.MSG_TYPE_TEMPLATE);
        detail.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
        detail.setUid(ftpDetail.getUid());
        detail.setTitle(ftpDetail.getTitle());
        detail.setText(ftpDetail.getText().replaceAll("\\\\n", "\n"));
        detail.setNoticeMainId(main.getNoticeMainId());
        return detail;
    }

    private BillingNoticeMain setMain(String origFileName, int mainListSize, String originalFileType, String sendType, String scheduleTime, BillingNoticeContentTemplateMsg template) {
        BillingNoticeMain main = new BillingNoticeMain();
        main.setGroupId((long) (mainListSize + 1));
        main.setSendType(sendType.toUpperCase());
        main.setOrigFileName(origFileName);
        main.setOrigFileType(originalFileType);
        main.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
        main.setExpiryTime(getExpireTime().getTime());
        main.setTempId(template.getTemplateId());
        main.setScheduleTime(BillingNoticeMain.SENDING_MSG_TYPE_DELAY.equals(sendType) ? scheduleTime : null);
        main.setTemplate(template);
        return main;
    }

    /**
     * save db Status = DRAFT
     *
     * @param billingNoticeMain billingNoticeMain
     */
    private void saveDb(BillingNoticeMain billingNoticeMain) {
        List<BillingNoticeDetail> originalDetails = billingNoticeMain.getDetails();
        log.info("BillingNoticeFtpService BillingNoticeDetail size: {}", originalDetails.size());
        billingNoticeMain.setProcApName(DataUtils.getRandomProcApName());
        billingNoticeMain = billingNoticeMainRepository.save(billingNoticeMain);
        List<BillingNoticeDetail> detailList = new ArrayList<>();
        for (BillingNoticeDetail detail : originalDetails) {
            detail.setNoticeMainId(billingNoticeMain.getNoticeMainId());
            detailList.add(detail);
        }
        if (!detailList.isEmpty()) {
            billingNoticeDetailRepository.save(detailList);
        }
    }



    /**
     * 資料解析完狀態改為retry or wait
     *
     * @param billingNoticeMain billingNoticeMain
     */
    private void updateStatus(BillingNoticeMain billingNoticeMain) {
        BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(billingNoticeMain.getTempId());
        Long mainId = billingNoticeMain.getNoticeMainId();
        String status = BillingNoticeMain.NOTICE_STATUS_WAIT;
        // 流程開關
        if (!template.isProductSwitch()) {
            status = BillingNoticeMain.NOTICE_STATUS_RETRY;
            log.info("Curfew NOTICE_STATUS_RETRY mainId:" + mainId);
        }
        Date now = new Date();

        /* !!Priority update detail to wait */
        billingNoticeDetailRepository.updateStatusByMainId(status, now, mainId);
        /* !!Second update main to wait. Because Ap discover data with main status */
        billingNoticeMainRepository.updateBillingNoticeMainStatus(status, now, mainId);
    }

    /**
     * check header
     *
     * @param header header
     * @return boolean
     */
    private boolean validateHeader(String header) {
        if (StringUtils.isBlank(header)) {
            return false;
        }
        String[] splitData = header.split("\\|");
        int length = splitData.length;
        if (length != 3) {
            return false;
        }
        String sendType = splitData[1].toUpperCase();
        if (!sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)
                && !sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_IMMEDIATE)) {
            return false;
        }
        if (sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)) {
            String scheduleTime = splitData[2];
            Date date = DataUtils.convStrToDate(scheduleTime, "yyyy-MM-dd hh:mm:ss");
            return date != null;
        }
        return true;
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info("BillingNoticeFtpService cancel....");
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("BillingNoticeFtpService shutdown....");
            scheduler.shutdown();
        }
    }
}
