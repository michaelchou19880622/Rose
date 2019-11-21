package com.bcs.core.taishin.circle.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeDetailRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeMainRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeRepositoryCustom;
import com.bcs.core.taishin.circle.ftp.FtpService;
import com.bcs.core.taishin.circle.ftp.FtpSetting;
import com.bcs.core.utils.DataUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class BillingNoticeFtpService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(BillingNoticeFtpService.class);
    private BillingNoticeRepositoryCustom billingNoticeRepositoryCustom;
    private BillingNoticeMainRepository billingNoticeMainRepository;
    private BillingNoticeDetailRepository billingNoticeDetailRepository;
    private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
    private FtpService ftpService;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("Billing-Notice-FTP-Scheduled-%d")
                    .daemon(true).build()
    );

    private ScheduledFuture<?> scheduledFuture = null;

    @Autowired
    public BillingNoticeFtpService(
            BillingNoticeRepositoryCustom billingNoticeRepositoryCustom,
            BillingNoticeMainRepository billingNoticeMainRepository,
            BillingNoticeDetailRepository billingNoticeDetailRepository,
            BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository,
            FtpService ftpService) {
        this.billingNoticeRepositoryCustom = billingNoticeRepositoryCustom;
        this.billingNoticeMainRepository = billingNoticeMainRepository;
        this.billingNoticeDetailRepository = billingNoticeDetailRepository;
        this.billingNoticeContentTemplateMsgRepository = billingNoticeContentTemplateMsgRepository;
        this.ftpService = ftpService;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        String unit = CoreConfigReader.getString(CONFIG_STR.BN_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.BN_SCHEDULE_TIME, true, false);
        if (time == -1) {
            logger.error(" BillingNoticeFtpService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 排程工作
                logger.debug(" BillingNoticeFtpService startCircle....");
                ftpProcessHandler();
            }
        }, 0, time, TimeUnit.valueOf(unit));
    }


    /**
     * 執行流程
     */
    private void ftpProcessHandler() {
        boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIGSWITCH, true, false);
        String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true, false);
        String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true, false);

        /* 大流程關閉時不做 */
        if (!bigSwitch) {
            return;
        }

        try {
            // 1. ftp get file
            Map<String, byte[]> lReturnDataMap = new HashMap<>();
            List<FtpSetting> ftpSettings = ftpService.getFtpSettings();
            for (FtpSetting ftp : ftpSettings) {
                ftp.clearFileNames();
                Map<String, byte[]> returnDatas = ftpService.downloadMutipleFileByType(ftp.getPath(), fileExtension, ftp);
                for (String fileName : returnDatas.keySet()) {
                    ftp.addFileNames(fileName);
                    logger.info(" FTP--" + ftp.getChannelId() + ":" + fileName);
                }
                lReturnDataMap.putAll(returnDatas);
            }

            // 2. parse data to object
            List<BillingNoticeMain> mains = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : lReturnDataMap.entrySet()) {
                String fileName = entry.getKey();
                String encoding = "UTF-8";
                for (FtpSetting ftp : ftpSettings) {
                    if (ftp.containsFileName(fileName)) {
                        encoding = ftp.getFileEncoding();
                        logger.debug(fileName + " containsFileName encoding:" + encoding);
                    }
                }
                logger.info(fileName + " encoding:" + encoding);
                byte[] fileData = lReturnDataMap.get(fileName);
                logger.info(" BillingNoticeFtpService handle file:" + downloadSavePath + File.separator + fileName);
                File targetFile = new File(downloadSavePath + File.separator + fileName);
                FileUtils.writeByteArrayToFile(targetFile, fileData);
                InputStream targetStream = new FileInputStream(targetFile);
                List<String> fileContents = IOUtils.readLines(targetStream, encoding);
                List<BillingNoticeMain> billingNoticeMains = parseFtpFile(fileName, fileContents);
                mains.addAll(billingNoticeMains);

                targetStream.close();
            }
            // 3. save data to db = Status = DRAFT
            for (BillingNoticeMain billingNoticeMain : mains) {
                saveDb(billingNoticeMain);
                updateStatus(billingNoticeMain);
            }
            // 4. remove file
            if (!lReturnDataMap.isEmpty()) {
                for (FtpSetting ftp : ftpSettings) {
                    ftpService.deleteFileByType(ftp.getPath(), ftp.getFileNames().toArray(new String[0]), ftp);
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }


    private Map<String, List<BillingNoticeFtpDetail>> parseDetail(List<String> fileContents) {
        List<BillingNoticeFtpDetail> details = new ArrayList<>();
        for (int i = 1; i < fileContents.size(); i++) {
            if (StringUtils.isNotBlank(fileContents.get(i))) {
            	logger.info( "fileContents" + fileContents);          	
                String[] detailData = fileContents.get(i).split("\\|");
                if (detailData.length == 4 || detailData.length == 3) {
                    BillingNoticeFtpDetail detail = new BillingNoticeFtpDetail();
                    detail.setUid(detailData[0]);
                    detail.setTitle(detailData[1]);
                    detail.setText(detailData[2]);
                    if(detailData.length == 4 ) {
                    	detail.setTemplate(detailData[3]);
                    }else {
                    	detail.setTemplate("default");
                    }
                    details.add(detail);
                } else {
                    logger.error("parseFtpFile error Data:" + Arrays.toString(detailData));
                }
            }
        }
        logger.info("details : " + details);
        Map<String, List<BillingNoticeFtpDetail>> resultMap = new HashMap<>();

        for (BillingNoticeFtpDetail deatil : details) {
            String key = deatil.getTemplate();
            if (resultMap.containsKey(key)) {
                List<BillingNoticeFtpDetail> list = resultMap.get(key);
                list.add(deatil);

            } else {
                List<BillingNoticeFtpDetail> list = new ArrayList<>();
                list.add(deatil);
                resultMap.put(key, list);
            }

        }
        logger.info("BillingNoticeFtpDetail resultMap : " + resultMap);
        return resultMap;
    }

    /**
     * 整理txt data
     *
     * @param origFileName origFileName
     * @param fileContents fileContents
     */
    private List<BillingNoticeMain> parseFtpFile(String origFileName, List<String> fileContents) {
        List<BillingNoticeMain> mains = new ArrayList<>();
        logger.info("----------parseFtpFile---------");
        logger.info("fileContents.isEmpty() : " + fileContents.isEmpty());
        if (!fileContents.isEmpty()) {
            String header = fileContents.get(0);
            if (validateHeader(header)) {
                Calendar expiryTime = Calendar.getInstance();
                expiryTime.add(Calendar.HOUR, 3);
                String[] splitHeaderData = header.split("\\|");
                String originalFileType = splitHeaderData[0];
                String sendType = splitHeaderData[1].toUpperCase();
                String scheduleTime = splitHeaderData[2];

                // TemplateTitle => Details
                Map<String, List<BillingNoticeFtpDetail>> resultMap = parseDetail(fileContents);
                logger.info("originalFileType : " + originalFileType);
                logger.info("resultMap : " + resultMap);
                for (Map.Entry<String, List<BillingNoticeFtpDetail>> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    // get the Main Template
                    BillingNoticeContentTemplateMsg template = null;
                    List<BillingNoticeContentTemplateMsg> templates = billingNoticeContentTemplateMsgRepository.findMainOnTemplateByTitle(key);
                    if (templates == null || templates.isEmpty()) {
                        logger.info("Template :" + key + " not exist!  Use Default");
                        // 拿不到範本取default
                        List<BillingNoticeContentTemplateMsg> defaultTemplates = billingNoticeContentTemplateMsgRepository.findByTemplateTitleAndProductSwitchOn("default");
                        if (defaultTemplates == null || defaultTemplates.isEmpty()) {
                            logger.info("Default Template is not exist!");
                        } else {
                            template = defaultTemplates.get(0);
                        }
                    } else {
                        // carousel button
                        template = templates.get(0);
                    }
                    logger.info("template : "+ template);
                    if (template == null) {
                        logger.error("Template is not exist!:" + key);
                    } else {
                        // set Basics
                        BillingNoticeMain billingNoticeMain = new BillingNoticeMain();
                        billingNoticeMain.setGroupId((long) (mains.size() + 1));
                        billingNoticeMain.setSendType(sendType);
                        billingNoticeMain.setOrigFileName(origFileName);
                        billingNoticeMain.setOrigFileType(originalFileType);
                        billingNoticeMain.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
                        billingNoticeMain.setExpiryTime(expiryTime.getTime());
                        billingNoticeMain.setTempId(template.getTemplateId());
                        if (sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)) {
                            billingNoticeMain.setScheduleTime(scheduleTime);
                        }
                        billingNoticeMain.setTemplate(template);

                        // set Details
                        List<BillingNoticeFtpDetail> ftpDetails = resultMap.get(key);
                        List<BillingNoticeDetail> details = new ArrayList<>();
                        for (BillingNoticeFtpDetail ftpDetail : ftpDetails) {
                            BillingNoticeDetail detail = new BillingNoticeDetail();
                            detail.setMsgType(BillingNoticeDetail.MSG_TYPE_TEMPLATE);
                            detail.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
                            detail.setUid(ftpDetail.getUid());
                            detail.setTitle(ftpDetail.getTitle());
                            detail.setText(ftpDetail.getText().replaceAll("\\\\n", "\n"));
                            detail.setNoticeMainId(billingNoticeMain.getNoticeMainId());
                            details.add(detail);
                        }
                        billingNoticeMain.setDetails(details);
                        mains.add(billingNoticeMain);
                    }
                }
            }
        }
        return mains;
    }

    /**
     * save db Status = DRAFT
     *
     * @param billingNoticeMain billingNoticeMain
     */
    private void saveDb(BillingNoticeMain billingNoticeMain) {
        List<BillingNoticeDetail> originalDetails = billingNoticeMain.getDetails();
        logger.info(" BillingNoticeFtpService BillingNoticeDetail size:" + originalDetails.size());
        billingNoticeMain = billingNoticeMainRepository.save(billingNoticeMain);
        List<BillingNoticeDetail> details = new ArrayList<>();
        for (BillingNoticeDetail detail : originalDetails) {
            detail.setNoticeMainId(billingNoticeMain.getNoticeMainId());
            details.add(detail);
        }
        if (!details.isEmpty()) {
            billingNoticeRepositoryCustom.batchInsertBillingNoticeDetail(details);
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
            logger.info("Curfew NOTICE_STATUS_RETRY mainId:" + mainId);
        }
        Date now = Calendar.getInstance().getTime();
        billingNoticeMainRepository.updateBillingNoticeMainStatus(status, now, mainId);
        billingNoticeDetailRepository.updateStatusByMainId(status, now, mainId);
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
            logger.info(" BillingNoticeFtpService cancel....");
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            logger.info(" BillingNoticeFtpService shutdown....");
            scheduler.shutdown();
        }

    }

}
