package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 批次讀取前方來源系統(FTP)中訊息
 * 1. 明宣(Ming)
 * 2. 山竹(Mitake)
 * 3. Unica
 * 4. 互動(Every8D)
 *
 * @author ???
 * @see com.bcs.web.init.controller.InitController#init()
 */
@Slf4j
@Service
public class LoadFtpPnpDataTask {

    private static final String TAG = "\\&";
    private static final String MING_TAG = "\\;;";

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-FTP-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture = null;

    /**
     * PNP FTP 服務
     */
    private PNPFtpService pnpFtpService;

    /**
     * PNP 訊息本體-UNICA格式
     */
    private PnpRepositoryCustom pnpRepositoryCustom;

    /**
     * PNP 訊息表頭-UNICA格式
     */
    private PnpMainUnicaRepository pnpMainUnicaRepository;

    /**
     * PNP 訊息本體-UNICA格式
     */
    private PnpDetailUnicaRepository pnpDetailUnicaRepository;

    /**
     * PNP 訊息表頭-互動格式(Every8D)
     */
    private PnpMainEvery8dRepository pnpMainEvery8dRepository;

    /**
     * PNP 訊息本體-互動格式(Every8D)
     */
    private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;

    /**
     * PNP 訊息表頭-明宣格式
     */
    private PnpMainMingRepository pnpMainMingRepository;

    /**
     * PNP 訊息本體-明宣格式
     */
    private PnpDetailMingRepository pnpDetailMingRepository;

    /**
     * PNP 訊息表頭-山竹格式
     */
    private PnpMainMitakeRepository pnpMainMitakeRepository;

    /**
     * PNP 訊息本體-山竹格式
     */
    private PnpDetailMitakeRepository pnpDetailMitakeRepository;

    private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository;

    /**
     * 是否驗證白名單
     */
    private boolean whiteListValidate = CoreConfigReader.getBoolean(CONFIG_STR.PNP_WHITELIST_VALIDATE, true, false);

    @Autowired
    public LoadFtpPnpDataTask(PNPFtpService pnpFtpService,
                              PnpRepositoryCustom pnpRepositoryCustom,
                              PnpMainUnicaRepository pnpMainUnicaRepository,
                              PnpDetailUnicaRepository pnpDetailUnicaRepository,
                              PnpMainEvery8dRepository pnpMainEvery8dRepository,
                              PnpDetailEvery8dRepository pnpDetailEvery8dRepository,
                              PnpMainMingRepository pnpMainMingRepository,
                              PnpDetailMingRepository pnpDetailMingRepository,
                              PnpMainMitakeRepository pnpMainMitakeRepository,
                              PnpDetailMitakeRepository pnpDetailMitakeRepository,
                              PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository) {
        this.pnpFtpService = pnpFtpService;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
        this.pnpMainUnicaRepository = pnpMainUnicaRepository;
        this.pnpDetailUnicaRepository = pnpDetailUnicaRepository;
        this.pnpMainEvery8dRepository = pnpMainEvery8dRepository;
        this.pnpDetailEvery8dRepository = pnpDetailEvery8dRepository;
        this.pnpMainMingRepository = pnpMainMingRepository;
        this.pnpDetailMingRepository = pnpDetailMingRepository;
        this.pnpMainMitakeRepository = pnpMainMitakeRepository;
        this.pnpDetailMitakeRepository = pnpDetailMitakeRepository;
        this.pnpMaintainAccountModelRepository = pnpMaintainAccountModelRepository;
    }

    static {
        /* FIXME Alan: 測試資料? */
        String ftpInfo = CoreConfigReader.getString(CONFIG_STR.SYSTEM_PNP_FTP_INFO);

        /* FIXME Alan: JSON? */
        if (StringUtils.isNotBlank(ftpInfo)) {
            try {
                List<Map<String, Object>> ftpInfoList = new ObjectMapper().readValue(ftpInfo, new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (IOException e) {
                log.error(ErrorRecord.recordError(e));
            }
        }
    }

    /**
     * Start Schedule
     *
     * @see com.bcs.web.init.controller.InitController#init()
     */
    public void startCircle() {
        /* 批次執行時間單位 */
        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        /* 批次開始執行時間 */
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);

        /* 檢核時間合理性 */
        if (time == -1) {
            log.error("TimeUnit error :" + time + unit);
            return;
        }

        /* 進行批次排程 */
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                log.info("ftpProcessHandler SOURCE_MITAKE....");
                try {
                    ftpProcessHandler(AbstractPnpMainEntity.SOURCE_MITAKE);
                } catch (Exception e) {
                    log.error("Exception", e);
                }

                log.info("ftpProcessHandler SOURCE_MING....");
                try {
                    ftpProcessHandler(AbstractPnpMainEntity.SOURCE_MING);
                } catch (Exception e) {
                    log.error("Exception", e);
                }

                log.info("ftpProcessHandler SOURCE_EVERY8D....");
                try {
                    ftpProcessHandler(AbstractPnpMainEntity.SOURCE_EVERY8D);
                } catch (Exception e) {
                    log.error("Exception", e);
                }

                log.info("ftpProcessHandler SOURCE_UNICA....");
                try {
                    ftpProcessHandler(AbstractPnpMainEntity.SOURCE_UNICA);
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }, 0, time, TimeUnit.valueOf(unit));
    }

    /**
     * 執行FTP取資料流程
     *
     * @param source 來源("1":三竹 "2":互動 "3":明宣 "4":Unica)
     * @see this#startCircle()
     */
    private void ftpProcessHandler(String source) {
        log.info(PNPFTPType.getNameByCode(source).toUpperCase() + " => StartCircle!!");
        int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);

        switch (bigSwitch) {
            case 0:
                log.info(bigSwitch + ": Stop Process!!");
                break;
            case 1:
                /* 停止排程並轉發SMS */
                log.info(bigSwitch + ": Start put file to SMS FTP path process !!");
                // 1.1 將檔案rename(加L)放到SMS指定路徑
                // 因為路徑可用UI換，所以每次都要重拿連線資訊
                transFileToSMSFlow(source);
                log.info(bigSwitch + ": Transfer File To SMS Flow Complete!");
                break;
            default:
                /* 解析資料存到DB */
                log.info(bigSwitch + ": Parse Data Flow To Database!!");
                parseDataFlow(source);
                break;
        }
    }

    /**
     * 執行解析檔案資料到DB流程
     *
     * @see this#ftpProcessHandler
     */
    private void parseDataFlow(String source) {

        // 跟據來源不同取各自連線資訊
        PNPFtpSetting pnpFtpSetting = pnpFtpService.getFtpSettings(source);
        log.info("FTP Setting:\n" + DataUtils.toPrettyJsonUseJackson(pnpFtpSetting));
        try {
            /* 至FTP取得資料 */
            Map<String, byte[]> returnDataMap = pnpFtpService.downloadMultipleFileByType(source, pnpFtpSetting.getPath(), "TXT", pnpFtpSetting);
            pnpFtpSetting.clearFileNames();
            for (String fileName : returnDataMap.keySet()) {
                pnpFtpSetting.addFileNames(fileName);
                log.info("FTP--" + pnpFtpSetting.getChannelId() + ":" + fileName);
            }
            Map<String, byte[]> lReturnDataMap = new HashMap<>(returnDataMap);

            // 2. parse data to object
            List<Object> mains = new ArrayList<>();

            for (Map.Entry<String, byte[]> entry : lReturnDataMap.entrySet()) {
                String fileName = entry.getKey();
                byte[] fileData = entry.getValue();

                String encoding = "";
                if (pnpFtpSetting.containsFileName(fileName)) {
                    encoding = pnpFtpSetting.getFileEncoding();
                    log.info("Process Expect encoding : {}", encoding);
                    log.info("{} encoding: {}", fileName, encoding);
                }
                log.info(" LoadFtbPnpDataTask handle file: {}", pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
                File targetFile = new File(pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
                FileUtils.writeByteArrayToFile(targetFile, fileData);
                try (InputStream targetStream = new FileInputStream(targetFile)) {

                    if (!validateWhiteListHeader(source, fileName)) {
                        /* 白名單檢核錯誤 To SMS */
                        log.error("Valid WhiteList Header Fail!! To SMS");
                        uploadFileToSms(source, targetStream, fileName);
                        continue;
                    }
                    log.info("Valid WhiteList Header Success!! ");
                    /* 檢核白名單檔名上的帳號是否與來源對應 */
                    List<String> fileContents = IOUtils.readLines(targetStream, encoding);
                    log.info("File Content List : " + fileContents.toString());
                    // 依來源解成各格式
                    List<Object> pnpMains = parseFtpFile(source, fileName, fileContents);
                    if (ListUtils.isEmpty(pnpMains)) {
                        log.error("Valid WhiteList Content Fail!! To SMS");
                        uploadFileToSms(source, targetStream, fileName);
                        continue;
                    }
                    log.info("Valid WhiteList Content Success!! ");
                    mains.addAll(pnpMains);

                } catch (Exception e) {
                    log.warn("Exception", e);
                }
            }
            saveDraftToDatabaseBySource(source, mains);
            removeFtpFile(pnpFtpSetting, lReturnDataMap);

        } catch (Exception ex) {
            log.error("Exception", ex);
        }
    }

    /**
     * 3. Save Draft To Database By Source
     *
     * @param source source
     * @param mains  mains
     */
    private void saveDraftToDatabaseBySource(String source, List<Object> mains) {
        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                processMitakeData(mains);
                break;
            case AbstractPnpMainEntity.SOURCE_MING:
                processMingData(mains);
                break;
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                processEvery8dData(mains);
                break;
            case AbstractPnpMainEntity.SOURCE_UNICA:
                processUnicaData(mains);
                break;
            default:
                break;
        }
    }

    /**
     * 4. Remove FTP File
     *
     * @param pnpFtpSetting  FTP 連線設定檔
     * @param lReturnDataMap lReturnDataMap
     */
    private void removeFtpFile(PNPFtpSetting pnpFtpSetting, Map<String, byte[]> lReturnDataMap) {
        if (!lReturnDataMap.isEmpty()) {
            pnpFtpService.deleteFileByType(pnpFtpSetting.getPath(), pnpFtpSetting.getFileNames().toArray(new String[0]), pnpFtpSetting);
        }
    }

    /**
     * 執行將檔案轉移到SMS FTP流程
     **/
    private void transFileToSMSFlow(String source) {

        log.info("start transfer File To SMS path");
        PNPFtpSetting pnpFtpSetting = pnpFtpService.getFtpSettings(source);
        try {
            // 1. ftp get file
            Map<String, byte[]> returnDataMap = pnpFtpService.downloadMultipleFileByType(source, pnpFtpSetting.getPath(), "txt", pnpFtpSetting);
            pnpFtpSetting.clearFileNames();
            for (String fileName : returnDataMap.keySet()) {
                pnpFtpSetting.addFileNames(fileName);
                log.info(" FTP--" + pnpFtpSetting.getChannelId() + ":" + fileName);
            }
            Map<String, byte[]> lReturnDataMap = new HashMap<>(returnDataMap);


            for (Map.Entry<String, byte[]> entry : lReturnDataMap.entrySet()) {
                String fileName = entry.getKey();
                byte[] fileData = entry.getValue();
                log.info(" LoadFtbPnpDataTask handle file:" + pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
                File targetFile = new File(pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
                FileUtils.writeByteArrayToFile(targetFile, fileData);
                try (InputStream targetStream = new FileInputStream(targetFile)) {
                    log.info("uploadFileToSMS : " + fileName);
                    uploadFileToSms(source, targetStream, fileName);
                }
            }

            // 4. remove file
            removeFtpFile(pnpFtpSetting, lReturnDataMap);

        } catch (Exception ex) {
            log.error("Exception", ex);
        }
    }


    /**
     * @param mains mains
     * @see this#parseDataFlow
     */
    private void processMitakeData(List<Object> mains) {
        /* Update Status DRAFT */
        for (Object pnpMainMitake : mains) {
            saveMitakeDB(pnpMainMitake);
        }
        /* Update Status WAIT */
        for (Object pnpMainMitake : mains) {
            updateMitakeMainStatus(pnpMainMitake);
        }
    }

    /**
     * @param mains mains
     * @see this#parseDataFlow
     */
    private void processEvery8dData(List<Object> mains) {
        /* Update Status DRAFT */
        for (Object pnpMainEvery8d : mains) {
            saveEvery8dDB(pnpMainEvery8d);
        }
        /* Update Status WAIT */
        for (Object pnpMainEvery8d : mains) {
            updateEvery8dMainStatus(pnpMainEvery8d);
        }
    }

    /**
     * @param mains mains
     * @see this#parseDataFlow
     */
    private void processMingData(List<Object> mains) {
        /* Update Status DRAFT */
        for (Object pnpMainMing : mains) {
            saveMingDB(pnpMainMing);
        }
        /* Update Status WAIT */
        for (Object pnpMainMing : mains) {
            updateMingMainStatus(pnpMainMing);
        }
    }

    /**
     * @param mains mains
     * @see this#parseDataFlow
     */
    private void processUnicaData(List<Object> mains) {
        /* Update Status DRAFT */
        for (Object pnpMainUnica : mains) {
            saveUnicaDB(pnpMainUnica);
        }
        /* Update Status WAIT */
        for (Object pnpMainUnica : mains) {
            updateUnicaMainStatus(pnpMainUnica);
        }
    }

    /**
     * 轉換明宣物件
     *
     * @param fileContent 檔案內容
     * @return 明宣物件清單
     * @throws Exception Exception
     * @see this#parseMingFiles
     */
    private List<? super PnpDetail> parsePnpDetailMing(String fileContent, String flexTemplateId, String scheduleTime) throws Exception {
        log.info("flexTemplateId : " + flexTemplateId);
        List<? super PnpDetail> details = new ArrayList<>();
        /* 明宣沒有header所以從0開始 */
        final int columnSize = 9;
        if (StringUtils.isNotBlank(fileContent)) {
            String[] detailData = fileContent.split(MING_TAG, columnSize);
            if (detailData.length == columnSize) {

                /*
                 * 0.SN 流水號
                 * 1.收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
                 * 2.Content 簡訊訊息內容
                 * 3.預約時間
                 * 4.批次帳號1
                 * 5.批次帳號2
                 * 6.Variable1 擴充欄位1(可為空值)
                 * 7.Variable2 擴充欄位2(可為空值)
                 * 8.有效秒數
                 */
                PnpDetailMing detail = new PnpDetailMing();
                detail.setSn(detailData[0]);
                detail.setPhone(detailData[1]);
                detail.setPhoneHash(toSha256(detailData[1]));
                detail.setMsg(detailData[2]);
                detail.setDetailScheduleTime(detailData[3] == null ? scheduleTime : detailData[3]);
                detail.setAccount1(detailData[4]);
                detail.setAccount2(detailData[5]);
                detail.setVariable1(detailData[6]);
                detail.setVariable2(detailData[7]);
                detail.setKeepSecond(detailData[8]);
                detail.setFlexTemplateId(flexTemplateId);

                LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
                String mid = lineUserService.getMidByMobile(detail.getPhone());
                detail.setUid(mid);
                details.add(detail);

            } else {
                log.error("parsePnpDetailMing error Data:" + Arrays.toString(detailData));
            }
        }
        return details;
    }

    /**
     * 轉換Mitake物件
     *
     * @param fileContents 檔案內容清單
     * @return Mitake物件清單
     * @throws Exception Exception
     */
    private List<? super PnpDetail> parsePnpDetailMitake(List<String> fileContents, String flexTemplateId, String scheduleTime) throws Exception {
        List<? super PnpDetail> details = new ArrayList<>();
        /* Mitake有header所以從1開始 */
        for (int i = 1; i < fileContents.size(); i++) {
            if (StringUtils.isNotBlank(fileContents.get(i))) {
                final int columnSize = 4;
                String[] detailData = fileContents.get(i).split(TAG, columnSize);
                if (detailData.length == columnSize) {
                    /*
                     * 0.DestCategory 掛帳代碼
                     * 1.DestName  請填入系統有意義之流水號(open端可辯示之唯一序號)
                     * 2.DestNo 手機門號/請填入09帶頭的手機號碼。
                     * 3.MsgData 簡訊訊息內容
                     */
                    PnpDetailMitake detail = new PnpDetailMitake();
                    detail.setDestCategory(detailData[0]);
                    detail.setDestName(detailData[1]);
                    detail.setPhone(detailData[2]);
                    detail.setPhoneHash(toSha256(detailData[2]));
                    detail.setMsg(detailData[3]);
                    detail.setFlexTemplateId(flexTemplateId);
                    detail.setDetailScheduleTime(scheduleTime);
                    LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
                    String mid = lineUserService.getMidByMobile(detail.getPhone());
                    detail.setUid(mid);
                    details.add(detail);
                } else {
                    log.error("parsePnpDetailMitake error Data:" + Arrays.toString(detailData));
                }
            }
        }

        return details;
    }

    /**
     * 轉換Every8d物件
     *
     * @param fileContents 檔案內容清單
     * @return Every8d物件清單
     * @throws Exception Exception
     */
    private List<? super PnpDetail> parsePnpDetailEvery8d(List<String> fileContents, String flexTemplateId, String scheduleTime) throws Exception {
        List<? super PnpDetail> details = new ArrayList<>();
        /* Every8D有header所以從1開始 */
        for (int i = 1; i < fileContents.size(); i++) {
            if (StringUtils.isNotBlank(fileContents.get(i))) {
                final int columnSize = 10;
                String[] detailData = fileContents.get(i).split(TAG, columnSize);
                if (detailData.length == columnSize) {


                    /*
                     * 0.SN 序號
                     * 1.DestName 收件者名稱
                     * 2.DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
                     * 3.Content 簡訊訊息內容
                     * 4.PID 身份字號
                     * 5.CampaignID 行銷活動代碼(可為空值)
                     * 6.SegmentID 客群代號(可為空值)
                     * 7.ProgramID 階段代號(可為空值)
                     * 8.Variable1 擴充欄位1(可為空值)
                     * 9.Variable2 擴充欄位2(可為空值)
                     */

                    PnpDetailEvery8d detail = new PnpDetailEvery8d();
                    detail.setSn(detailData[0]);
                    detail.setDestName(detailData[1]);
                    detail.setPhone(detailData[2]);
                    detail.setPhoneHash(toSha256(detailData[2]));
                    detail.setMsg(detailData[3]);
                    detail.setPid(detailData[4]);
                    detail.setCampaignId(detailData[5]);
                    detail.setSegmentId(detailData[6]);
                    detail.setProgramId(detailData[7]);
                    detail.setVariable1(detailData[8]);
                    detail.setVariable2(detailData[9]);
                    detail.setDetailScheduleTime(scheduleTime);
                    detail.setFlexTemplateId(flexTemplateId);
                    LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
                    String mid = lineUserService.getMidByMobile(detail.getPhone());
                    detail.setUid(mid);
                    details.add(detail);
                } else {
                    log.error("parsePnpDetailEvery8d error Data:" + Arrays.toString(detailData));
                }
            }
        }
        return details;
    }

    /**
     * 轉換Unica物件
     *
     * @param fileContents 檔案內容清單
     * @return Unica物件清單
     * @throws Exception Exception
     */
    private List<? super PnpDetail> parsePnpDetailUnica(List<String> fileContents, String flexTemplateId, String scheduleTime) throws Exception {
        List<? super PnpDetail> details = new ArrayList<>();
        /* Unica有header所以從1開始 */
        for (int i = 1; i < fileContents.size(); i++) {
            if (StringUtils.isNotBlank(fileContents.get(i))) {
                final int columnSize = 10;
                String[] detailData = fileContents.get(i).split(TAG, columnSize);
                if (detailData.length == columnSize) {

                    /*
                     * 0.SN 序號
                     * 1.DestName 收件者名稱
                     * 2.DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
                     * 3.Content 簡訊訊息內容
                     * 4.PID 身份字號
                     * 5.CampaignID 行銷活動代碼(可為空值)
                     * 6.SegmentID 客群代號(可為空值)
                     * 7.ProgramID 階段代號(可為空值)
                     * 8.Variable1 擴充欄位1(可為空值)
                     * 9.Variable2 擴充欄位2(可為空值)
                     */
                    PnpDetailUnica detail = new PnpDetailUnica();
                    detail.setSn(detailData[0]);
                    detail.setDestName(detailData[1]);
                    detail.setPhone(detailData[2]);
                    detail.setPhoneHash(toSha256(detailData[2]));
                    detail.setMsg(detailData[3]);
                    detail.setPid(detailData[4]);
                    detail.setCampaignId(detailData[5]);
                    detail.setSegmentId(detailData[6]);
                    detail.setProgramId(detailData[7]);
                    detail.setVariable1(detailData[8]);
                    detail.setVariable2(detailData[9]);
                    detail.setFlexTemplateId(flexTemplateId);
                    detail.setDetailScheduleTime(scheduleTime);
                    LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
                    String mid = lineUserService.getMidByMobile(detail.getPhone());
                    detail.setUid(mid);
                    details.add(detail);
                } else {
                    log.error("parsePnpDetailUnica error Data:" + Arrays.toString(detailData));
                }
            }
        }
        return details;
    }

    /**
     * 依分類轉換 FTP 簡訊 TXT 格式資料至物件
     *
     * @param source       前方來源系統格式
     * @param origFileName 原始TXT文字檔名
     * @param fileContents 檔案內容
     * @return 轉換後內容清單
     * @throws Exception Exception
     * @see this#parseDataFlow
     */
    private List<Object> parseFtpFile(String source, String origFileName, List<String> fileContents) throws Exception {
        log.info("Source: " + source);
        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                return parseMitakeFiles(origFileName, fileContents);
            case AbstractPnpMainEntity.SOURCE_MING:
                return parseMingFiles(origFileName, fileContents);
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                return parseEvery8dFiles(origFileName, fileContents);
            case AbstractPnpMainEntity.SOURCE_UNICA:
                return parseUnicaFiles(origFileName, fileContents);
            default:
                return Collections.emptyList();
        }
    }

    /*================================================================================*/

    /**
     * 轉換Ming檔案
     *
     * @param origFileName 原始檔名
     * @param fileContents 檔案內容
     * @return 轉換後物件清單
     * @throws Exception Exception
     * @see this#parseFtpFile
     */
    private List<Object> parseMingFiles(String origFileName, List<String> fileContents) throws Exception {
        if (fileContents.isEmpty()) {
            log.error("parseMingFiles fileContents.isEmpty");
            return Collections.emptyList();
        }
        log.info("fileContent List Size: " + fileContents.size());
        String[] contentSp = fileContents.get(0).split(MING_TAG, 9);
        log.info("Content Array: " + Arrays.toString(contentSp));
        if (contentSp.length < 3) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }
        String content1 = contentSp[2];
        PNPMaintainAccountModel accountModel = validateWhiteListContent(AbstractPnpMainEntity.SOURCE_MING, origFileName, content1);
        log.info("accountModel1:" + accountModel);
        if (null == accountModel) {
            log.error("parseMingFiles fileContents validate is failed");
            return Collections.emptyList();
        }
        List<Object> mains = new ArrayList<>();
        for (String content : fileContents) {
            PnpMainMing pnpMainMing = new PnpMainMing();
            pnpMainMing.setOrigFileName(origFileName);
            pnpMainMing.setSource(AbstractPnpMainEntity.SOURCE_MING);
            pnpMainMing.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
            pnpMainMing.setProcFlow(accountModel.getPathway());
            pnpMainMing.setProcStage(getDefaultStage(accountModel.getPathway()));
            pnpMainMing.setPnpMaintainAccountId(accountModel.getId());

            String[] detailData = content.split(MING_TAG, 9);
            String orderTime = detailData[3];
            pnpMainMing.setSendType(getSendType(orderTime, "yyyy-MM-dd HH:mm:ss"));
            if (AbstractPnpMainEntity.SEND_TYPE_DELAY.equals(pnpMainMing.getSendType())
                    || AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED.equals(pnpMainMing.getSendType())) {
                pnpMainMing.setScheduleTime(orderTime);
            }
            pnpMainMing.setPnpDetails(parsePnpDetailMing(content, accountModel.getTemplate(), pnpMainMing.getScheduleTime()));
            mains.add(pnpMainMing);
        }
        return mains;
    }

    /**
     * 轉換Mitake檔案
     *
     * @param origFileName 原始檔名
     * @param fileContents 檔案內容
     * @return 轉換後物件清單
     * @throws Exception Exception
     * @see this#parseFtpFile
     */
    private List<Object> parseMitakeFiles(String origFileName, List<String> fileContents) throws Exception {
        if (fileContents.isEmpty()) {
            log.error("parseMitakeFiles fileContents.isEmpty");
            return Collections.emptyList();
        }
        log.info("fileContent List Size: " + fileContents.size());
        String[] contentSp = fileContents.get(1).split(TAG, 4);
        log.info("Content Array: " + Arrays.toString(contentSp));
        if (contentSp.length < 4) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }
        String content = contentSp[3];
        PNPMaintainAccountModel accountModel = validateWhiteListContent(AbstractPnpMainEntity.SOURCE_MITAKE, origFileName, content);
        if (null == accountModel) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }
        String header = fileContents.get(0);
        log.info("Header Content      : " + header);
        String[] splitHeaderData = header.split(TAG, 6);
        log.info("Header Content Array: " + Arrays.toString(splitHeaderData));
        String groupID = splitHeaderData[0];
        String username = splitHeaderData[1];
        String userPassword = splitHeaderData[2];
        String orderTime = splitHeaderData[3];
        String validityTime = splitHeaderData[4];
        String msgType = splitHeaderData[5];
        PnpMainMitake pnpMainMitake = new PnpMainMitake();
        pnpMainMitake.setOrigFileName(origFileName);
        pnpMainMitake.setSource(AbstractPnpMainEntity.SOURCE_MITAKE);
        pnpMainMitake.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        pnpMainMitake.setProcFlow(accountModel.getPathway());
        pnpMainMitake.setProcStage(getDefaultStage(accountModel.getPathway()));
        pnpMainMitake.setPnpMaintainAccountId(accountModel.getId());
        pnpMainMitake.setSendType(getSendType(orderTime, "yyyyMMddHHmmss"));
        // 原生欄位
        pnpMainMitake.setGroupIDSource(groupID);
        pnpMainMitake.setUsername(username);
        pnpMainMitake.setUserPassword(userPassword);
        pnpMainMitake.setOrderTime(orderTime);
        pnpMainMitake.setValidityTime(validityTime);
        pnpMainMitake.setMsgType(msgType);

        if (AbstractPnpMainEntity.SEND_TYPE_DELAY.equals(pnpMainMitake.getSendType())
                || AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED.equals(pnpMainMitake.getSendType())) {
            pnpMainMitake.setScheduleTime(orderTime);
        }

        pnpMainMitake.setPnpDetails(parsePnpDetailMitake(fileContents, accountModel.getTemplate(), pnpMainMitake.getScheduleTime()));
        List<Object> mains = new ArrayList<>();
        mains.add(pnpMainMitake);
        return mains;
    }

    /**
     * 轉換Every8d檔案
     *
     * @param origFileName 原始檔名
     * @param fileContents 檔案內容
     * @return 轉換後物件清單
     * @throws Exception Exception
     * @see this#parseFtpFile
     */
    private List<Object> parseEvery8dFiles(String origFileName, List<String> fileContents) throws Exception {
        if (fileContents.isEmpty()) {
            log.error("parseEvery8DFiles fileContents.isEmpty");
            return Collections.emptyList();
        }
        log.info("fileContent List Size: " + fileContents.size());
        String[] contentSp = fileContents.get(1).split(TAG, 10);
        log.info("Content Array: " + Arrays.toString(contentSp));
        if (contentSp.length < 4) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }
        String content = contentSp[3];
        PNPMaintainAccountModel accountModel = validateWhiteListContent(AbstractPnpMainEntity.SOURCE_EVERY8D, origFileName, content);
        if (null == accountModel) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }

        String header = fileContents.get(0);
        log.info("Header Content      : " + header);
        String[] splitHeaderData = header.split(TAG, 7);
        log.info("Header Content Array: " + Arrays.toString(splitHeaderData));
        String subject = splitHeaderData[0];
        String userId = splitHeaderData[1];
        String password = splitHeaderData[2];
        String orderTime = splitHeaderData[3];
        String expireTime = splitHeaderData[4];
        String msgType = splitHeaderData[5];
        /* 簡訊平台保留欄位，請勿填入資料 */
        String batchId = splitHeaderData[6];
        PnpMainEvery8d pnpMainEvery8d = new PnpMainEvery8d();
        pnpMainEvery8d.setOrigFileName(origFileName);
        pnpMainEvery8d.setSource(AbstractPnpMainEntity.SOURCE_EVERY8D);
        pnpMainEvery8d.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        pnpMainEvery8d.setProcFlow(accountModel.getPathway());
        pnpMainEvery8d.setProcStage(getDefaultStage(accountModel.getPathway()));
        pnpMainEvery8d.setPnpMaintainAccountId(accountModel.getId());
        pnpMainEvery8d.setSendType(getSendType(orderTime, "yyyyMMddHHmmss"));
        // 原生欄位
        pnpMainEvery8d.setSubject(subject);
        pnpMainEvery8d.setUserID(userId);
        pnpMainEvery8d.setPassword(password);
        pnpMainEvery8d.setOrderTime(orderTime);
        pnpMainEvery8d.setExprieTime(expireTime);
        pnpMainEvery8d.setMsgType(msgType);
        pnpMainEvery8d.setBatchID(batchId);

        if (AbstractPnpMainEntity.SEND_TYPE_DELAY.equals(pnpMainEvery8d.getSendType())
                || AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED.equals(pnpMainEvery8d.getSendType())) {
            pnpMainEvery8d.setScheduleTime(orderTime);
        }

        pnpMainEvery8d.setPnpDetails(parsePnpDetailEvery8d(fileContents, accountModel.getTemplate(), pnpMainEvery8d.getScheduleTime()));
        List<Object> mains = new ArrayList<>();
        mains.add(pnpMainEvery8d);
        return mains;
    }


    /**
     * 轉換Unica檔案
     *
     * @param origFileName 原始檔名
     * @param fileContents 檔案內蓉
     * @return 轉換後物件清單
     * @throws Exception Exception
     * @see this#parseFtpFile
     */
    private List<Object> parseUnicaFiles(String origFileName, List<String> fileContents) throws Exception {
        if (fileContents.isEmpty()) {
            log.error("parseUnicaFiles fileContents.isEmpty");
            return Collections.emptyList();
        }
        log.info("fileContent List Size: " + fileContents.size());
        String[] contentSp = fileContents.get(1).split(TAG, 10);
        log.info("Content Array: " + Arrays.toString(contentSp));
        if (contentSp.length < 4) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }
        String content1 = contentSp[3];
        PNPMaintainAccountModel accountModel = validateWhiteListContent(AbstractPnpMainEntity.SOURCE_UNICA, origFileName, content1);
        if (null == accountModel) {
            log.error("File Contents validate is failed");
            return Collections.emptyList();
        }

        String header = fileContents.get(0);
        log.info("Header Content      : " + header);
        String[] splitHeaderData = header.split(TAG, 7);
        log.info("Header Content Array: " + Arrays.toString(splitHeaderData));
        String subject = splitHeaderData[0];
        String userId = splitHeaderData[1];
        String password = splitHeaderData[2];
        String orderTime = splitHeaderData[3];
        String expireTime = splitHeaderData[4];
        String msgType = splitHeaderData[5];
        // 簡訊平台保留欄位，請勿填入資料
        String batchId = splitHeaderData[6];
        PnpMainUnica pnpMainUnica = new PnpMainUnica();
        pnpMainUnica.setOrigFileName(origFileName);
        pnpMainUnica.setSource(AbstractPnpMainEntity.SOURCE_UNICA);
        pnpMainUnica.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        pnpMainUnica.setProcFlow(accountModel.getPathway());
        pnpMainUnica.setProcStage(getDefaultStage(accountModel.getPathway()));
        pnpMainUnica.setPnpMaintainAccountId(accountModel.getId());

        pnpMainUnica.setSendType(getSendType(orderTime, "yyyyMMddHHmmss"));
        // 原生欄位
        pnpMainUnica.setSubject(subject);
        pnpMainUnica.setUserID(userId);
        pnpMainUnica.setPassword(password);
        pnpMainUnica.setOrderTime(orderTime);
        pnpMainUnica.setExprieTime(expireTime);
        pnpMainUnica.setMsgType(msgType);
        pnpMainUnica.setBatchID(batchId);

        if (AbstractPnpMainEntity.SEND_TYPE_DELAY.equals(pnpMainUnica.getSendType())
                || AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED.equals(pnpMainUnica.getSendType())) {
            pnpMainUnica.setScheduleTime(orderTime);
        }

        pnpMainUnica.setPnpDetails(parsePnpDetailUnica(fileContents, accountModel.getTemplate(), pnpMainUnica.getScheduleTime()));
        List<Object> mains = new ArrayList<>();
        mains.add(pnpMainUnica);
        return mains;

    }

    /*================================================================================*/

    /**
     * Return Default Process Flow
     *
     * @param processFlow BC, BC_SMS, BC_PNP_SMS
     * @return Default Process Flow
     * @see this#parseMingFiles
     * @see this#parseMitakeFiles
     * @see this#parseEvery8dFiles
     * @see this#parseUnicaFiles
     */
    private String getDefaultStage(String processFlow) {
        switch (processFlow) {
            case AbstractPnpMainEntity.PROC_FLOW_BC:
            case AbstractPnpMainEntity.PROC_FLOW_BC_SMS:
            case AbstractPnpMainEntity.PROC_FLOW_BC_PNP_SMS:
            default:
                return "BC";
        }
    }

    /**
     * 判斷預約時間決定發送排程
     *
     * @param orderTime 預約時間
     * @return 發送排程
     * @see this#parseMitakeFiles
     * @see this#parseEvery8dFiles
     * @see this#parseMingFiles
     * @see this#parseUnicaFiles
     */
    private String getSendType(String orderTime, String format) {
        if (StringUtils.isBlank(orderTime)) {
            /* 沒有預約時間則立即發送 */
            return AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE;
        }

        Date scheduleTime = DataUtils.convStrToDate(orderTime, format);
        if (DataUtils.isPast(scheduleTime)) {
            /* 排程時間小於現在時間則視為立即發送 */
            return AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED;
        } else {
            /* 排程時間大於現在時間則加入排程 */
            return AbstractPnpMainEntity.SEND_TYPE_DELAY;
        }
    }

    /*================================================================================*/

    /**
     * Mitake
     * Save Database and Change Status = DRAFT
     *
     * @param sourceMain sourceMain
     * @see this#processMitakeData
     */
    private void saveMitakeDB(Object sourceMain) {
        PnpMainMitake pnpMainMitake = (PnpMainMitake) sourceMain;
        List<? super PnpDetail> originalDetails = pnpMainMitake.getPnpDetails();
        log.info(" saveMitakeDB MitakeDetails size:" + originalDetails.size());
        /* 變更檔名為.OK */
        pnpMainMitake.setOrigFileName(pnpMainMitake.getOrigFileName().replace(".ok", ""));
        pnpMainMitake = pnpMainMitakeRepository.save(pnpMainMitake);
        List<PnpDetailMitake> details = new ArrayList<>();
        for (Object detail : originalDetails) {
            PnpDetailMitake pnpDetailMitake = (PnpDetailMitake) detail;
            pnpDetailMitake.setPnpMainId(pnpMainMitake.getPnpMainId());
            pnpDetailMitake.setProcFlow(pnpMainMitake.getProcFlow());
            pnpDetailMitake.setProcStage(pnpMainMitake.getProcStage());
            pnpDetailMitake.setSource(AbstractPnpMainEntity.SOURCE_MITAKE);
            pnpDetailMitake.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
            details.add(pnpDetailMitake);
        }
        if (CollectionUtils.isNotEmpty(details)) {
            List<List<PnpDetailMitake>> detailsPartitionList = Lists.partition(details, CircleEntityManagerControl.batchSize);
            for (List<PnpDetailMitake> detailList : detailsPartitionList) {
                pnpDetailMitakeRepository.save(detailList);
            }
            log.info("Update Status : " + AbstractPnpMainEntity.SOURCE_MING);
        }
    }


    /**
     * Unica
     * Save Database and Change Status = DRAFT
     *
     * @param sourceMain sourceMain
     * @see this#processUnicaData
     */
    private void saveUnicaDB(Object sourceMain) {
        PnpMainUnica pnpMainUnica = (PnpMainUnica) sourceMain;
        List<? super PnpDetail> originalDetails = pnpMainUnica.getPnpDetails();
        log.info(" saveEvery8dDB UnicaDetails size:" + originalDetails.size());

        pnpMainUnica = pnpMainUnicaRepository.save(pnpMainUnica);
        List<PnpDetailUnica> details = new ArrayList<>();
        for (Object detail : originalDetails) {
            PnpDetailUnica pnpDetailUnica = (PnpDetailUnica) detail;
            pnpDetailUnica.setPnpMainId(pnpMainUnica.getPnpMainId());
            pnpDetailUnica.setProcFlow(pnpMainUnica.getProcFlow());
            pnpDetailUnica.setProcStage(pnpMainUnica.getProcStage());
            pnpDetailUnica.setSource(AbstractPnpMainEntity.SOURCE_UNICA);
            pnpDetailUnica.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
            details.add(pnpDetailUnica);
        }
        if (!details.isEmpty()) {
            List<List<PnpDetailUnica>> detailsPartitionList = Lists.partition(details, CircleEntityManagerControl.batchSize);
            for (List<PnpDetailUnica> detailList : detailsPartitionList) {
                pnpDetailUnicaRepository.save(detailList);
            }
            log.info("Update Status : " + AbstractPnpMainEntity.SOURCE_MING);
        }
    }


    /**
     * Every8d
     * Save Database and Change Status = DRAFT
     *
     * @param sourceMain sourceMain
     * @see this#processEvery8dData
     */
    private void saveEvery8dDB(Object sourceMain) {
        PnpMainEvery8d pnpMainEvery8d = (PnpMainEvery8d) sourceMain;
        List<? super PnpDetail> originalDetails = pnpMainEvery8d.getPnpDetails();
        log.info(" saveEvery8dDB Every8dDetails size:" + originalDetails.size());

        pnpMainEvery8d = pnpMainEvery8dRepository.save(pnpMainEvery8d);
        List<PnpDetailEvery8d> details = new ArrayList<>();
        for (Object detail : originalDetails) {
            PnpDetailEvery8d pnpDetailEvery8d = (PnpDetailEvery8d) detail;
            pnpDetailEvery8d.setPnpMainId(pnpMainEvery8d.getPnpMainId());
            pnpDetailEvery8d.setProcFlow(pnpMainEvery8d.getProcFlow());
            pnpDetailEvery8d.setProcStage(pnpMainEvery8d.getProcStage());
            pnpDetailEvery8d.setSource(AbstractPnpMainEntity.SOURCE_EVERY8D);
            pnpDetailEvery8d.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
            details.add(pnpDetailEvery8d);
        }
        if (!details.isEmpty()) {
            List<List<PnpDetailEvery8d>> detailsPartitionList = Lists.partition(details, CircleEntityManagerControl.batchSize);
            for (List<PnpDetailEvery8d> detailList : detailsPartitionList) {
                pnpDetailEvery8dRepository.save(detailList);
            }
            log.info("Update Status : " + AbstractPnpMainEntity.SOURCE_MING);
        }
    }


    /**
     * Ming
     * Save Database and Change Status = DRAFT
     *
     * @param sourceMain sourceMain
     * @see this#processMingData
     */
    private void saveMingDB(Object sourceMain) {
        PnpMainMing pnpMainMing = (PnpMainMing) sourceMain;
        List<? super PnpDetail> originalDetails = pnpMainMing.getPnpDetails();
        log.info(" saveMingDB MingDetails size:" + originalDetails.size());
        pnpMainMing.setOrigFileName(pnpMainMing.getOrigFileName().replace(".ok", ""));
        pnpMainMing = pnpMainMingRepository.save(pnpMainMing);
        log.info(" saveMingDB pnpMainMing id:" + pnpMainMing.getPnpMainId());
        List<PnpDetailMing> details = new ArrayList<>();
        for (Object detail : originalDetails) {
            PnpDetailMing pnpDetail = (PnpDetailMing) detail;
            pnpDetail.setPnpMainId(pnpMainMing.getPnpMainId());
            pnpDetail.setProcFlow(pnpMainMing.getProcFlow());
            pnpDetail.setProcStage(pnpMainMing.getProcStage());
            pnpDetail.setSource(AbstractPnpMainEntity.SOURCE_MING);
            pnpDetail.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
            log.info(DataUtils.toPrettyJsonUseJackson(pnpDetail));
            details.add(pnpDetail);
        }
        if (!details.isEmpty()) {
            List<List<PnpDetailMing>> detailsPartitionList = Lists.partition(details, CircleEntityManagerControl.batchSize);
            for (List<PnpDetailMing> detailList : detailsPartitionList) {
                pnpDetailMingRepository.save(detailList);
            }
            log.info("Update Status : " + AbstractPnpMainEntity.SOURCE_MING);
        }
    }


    /*================================================================================*/

    /**
     * 三竹
     * 資料解析完狀態改為 WAIT
     *
     * @param sourceMain sourceMain
     * @see this#processMitakeData
     */
    private void updateMitakeMainStatus(Object sourceMain) {
        PnpMainMitake pnpMainMitake = (PnpMainMitake) sourceMain;
        Long mainId = pnpMainMitake.getPnpMainId();
        String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
        Date now = Calendar.getInstance().getTime();
        pnpMainMitakeRepository.updatePnpMainMitakeStatus(status, now, mainId);
        pnpDetailMitakeRepository.updateStatusByMainId(status, now, mainId);
        log.info("Update Status : " + status);
    }

    /**
     * 互動
     * 資料解析完狀態改為 WAIT
     *
     * @param sourceMain sourceMain
     */
    private void updateEvery8dMainStatus(Object sourceMain) {
        PnpMainEvery8d pnpMainEvery8d = (PnpMainEvery8d) sourceMain;
        Long mainId = pnpMainEvery8d.getPnpMainId();
        String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
        Date now = Calendar.getInstance().getTime();
        pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(status, now, mainId);
        pnpDetailEvery8dRepository.updateStatusByMainId(status, now, mainId);
        log.info("Update Status : " + status);
    }

    /**
     * Unica
     * 資料解析完狀態改為 WAIT
     *
     * @param sourceMain sourceMain
     */
    private void updateUnicaMainStatus(Object sourceMain) {
        PnpMainUnica pnpMainUnica = (PnpMainUnica) sourceMain;
        Long mainId = pnpMainUnica.getPnpMainId();
        String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
        Date now = Calendar.getInstance().getTime();
        pnpMainUnicaRepository.updatePnpMainUnicaStatus(status, now, mainId);
        pnpDetailUnicaRepository.updateStatusByMainId(status, now, mainId);
        log.info("Update Status : " + status);
    }

    /**
     * 明宣
     * 資料解析完狀態改為 WAIT
     *
     * @param sourceMain sourceMain
     */
    private void updateMingMainStatus(Object sourceMain) {
        PnpMainMing pnpMainMing = (PnpMainMing) sourceMain;
        Long mainId = pnpMainMing.getPnpMainId();
        String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
        Date now = Calendar.getInstance().getTime();
        int i = pnpMainMingRepository.updatePnpMainMingStatus(status, now, mainId);
        log.info("Return Int :" + i);
        pnpDetailMingRepository.updateStatusByMainId(status, now, mainId);
        log.info("Update Status : " + status);
    }

    /*================================================================================*/

    /**
     * 白名單帳號檢核
     * 比對檔名上的帳號與白名單設定上的帳號是否相同
     *
     * @param source   來源
     * @param fileName 檔名
     * @return 是否相符
     */
    private boolean validateWhiteListHeader(String source, String fileName) {
        if (!whiteListValidate) {
            log.info("====== Ignore Valid Account Pccode ======");
            return true;
        }

        log.info(String.format("Source: %s, FileName: %s", source, fileName));

        /*
         * 0: String accountClass
         * 1: String sourceSystem
         * 2: String account
         * 3: String dateTime
         *  */
        String[] fileNameSp = fileName.split("_");
        log.info("fileName Array: {}", Arrays.toString(fileNameSp));
        String account = fetchAccount(fileNameSp);
        String sourceSystem = fileNameSp[1];

        List<PNPMaintainAccountModel> accountList = pnpMaintainAccountModelRepository.findByAccountAndSourceSystem(account, sourceSystem);
        if (accountList != null) {
            log.info("Find Account List Size: " + accountList.size());
        }
        return CollectionUtils.isNotEmpty(accountList);
    }

    private String fetchAccount(String[] contentArray) {
        log.info("contentArray Length : {}", contentArray.length);
        if (contentArray.length <= 3) {
            return null;
        }
        List<String> list = new ArrayList<>(Arrays.asList(contentArray));
        log.info("Array              : {}", Arrays.toString(contentArray));

        list.remove(contentArray[0]);
        log.info("Remove Array[0]    : {}", contentArray[0]);
        list.remove(contentArray[1]);
        log.info("Remove Array[1]    : {}", contentArray[1]);
        list.remove(contentArray[contentArray.length - 1]);
        log.info("Remove Array[Last] : {}", contentArray[contentArray.length - 1]);

        if (list.isEmpty()) {
            log.info("List is Empty!!");
            return null;
        }

        log.info("Account List is : {}", DataUtils.toPrettyJsonUseJackson(list));
        String[] array = list.toArray(new String[0]);
        String account = String.join("_", array);
        log.info("Account is : {}", account);
        return account;
    }

    /**
     * 白名單內容檢核
     * 比對簡訊內容是否相同
     *
     * @param source   來源
     * @param fileName 檔名
     * @param content  內容
     * @return PNPMaintainAccountModel
     */
    private PNPMaintainAccountModel validateWhiteListContent(String source, String fileName, String content) {
        log.info(String.format("Source : %s, fileName : %s, content : %s", source, fileName, content));

        if (!whiteListValidate) {
            log.info("======跳過白名單Content檢核======");
            PNPMaintainAccountModel accountModel = new PNPMaintainAccountModel();
            accountModel.setPathway("3");
            return accountModel;
        }

        /*
         * 0: String accountClass
         * 1: String sourceSystem
         * 2: String account
         * 3: String comeTime
         *  */
        String[] fileNameSp = fileName.split("_");
        String sourceSystem = fileNameSp[1];
        String account = fetchAccount(fileNameSp);
        log.info("fileNameSP1:" + Arrays.toString(fileNameSp));

        List<PNPMaintainAccountModel> accountList = pnpMaintainAccountModelRepository.findByAccountAndSourceSystemAndStatus(account, sourceSystem, true);
        log.info("accountList1:" + accountList);
        if (CollectionUtils.isNotEmpty(accountList)) {
            for (PNPMaintainAccountModel accountModel : accountList) {
                String pnpContentPattern = accountModel.getPnpContent();
                log.info("accountModel ID :" + accountModel.getId());
                log.info("isMatch         :" + isMatch(content, pnpContentPattern));
                if (isMatch(content, pnpContentPattern)) {
                    return accountModel;
                }
            }
            log.error("validateWhiteList failed!!! account :" + account + " sourceSystem : " + sourceSystem);
            for (PNPMaintainAccountModel accountModel : accountList) {
                log.info("accountModel ID :" + accountModel.getId());
            }

            return null;
        } else {
            log.error("validateWhiteList failed!!! account :" + account + " sourceSystem : " + sourceSystem + " CAN NOT FIND ANY SETTING!!");
            return null;
        }

    }

    private static boolean isMatch(String content, String pattern) {
        log.info("Content           : " + content);
        log.info("ContentPattern    : " + pattern);
        printCharArrayCode(content);
        printCharArrayCode(pattern);
        // >><<為規定好的萬用字元
        pattern = pattern.replace("(*)", "¿¡");

        int i = 0;
        int j = 0;
        int iStar = -1;
        int jStar = -1;

        int patternLength = pattern.length();


        while (i < content.length()) {
            if (j < patternLength && (content.charAt(i) == pattern.charAt(j) || pattern.charAt(j) == '¿')) {
                ++i;
                ++j;
            } else if (j < patternLength && pattern.charAt(j) == '¡') {
                iStar = i;
                jStar = j++;
            } else if (iStar >= 0) {
                i = ++iStar;
                j = jStar + 1;
            } else {
                return false;
            }
        }
        while (j < patternLength && pattern.charAt(j) == '¡') {
            ++j;
        }
        return j == patternLength;
    }

    private static void printCharArrayCode(String str) {
        char[] contentArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : contentArray) {
            sb.append((int) c);
            sb.append(',');
        }
        log.info(sb.toString());
    }


    private String toSha256(String phone) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // phone有些可能已轉成e.164格式，0開頭的再做轉換
        if ("0".equals(phone.substring(0, 1))) {
            // 改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
            phone = "+886" + phone.substring(1);
        }

        byte[] hash = digest.digest(phone.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();

        for (byte hashByte : hash) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }


    private void uploadFileToSms(String source, InputStream targetStream, String fileName) {
        log.info("start uploadFileToSMS ");

        PNPFtpSetting setting = pnpFtpService.getFtpSettings(source);

        log.info(" fileName...." + fileName);

        try {
            pnpFtpService.uploadFileByType(targetStream, fileName, setting.getUploadPath(), setting);
        } catch (Exception e) {
            log.error("Exception", e);
            log.error("SMS uploadFileToSMS error:" + e.getMessage());
        }
    }


    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     **/
    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info(" LoadFtbPnpDataTask cancel....");
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            log.info(" LoadFtbPnpDataTask shutdown....");
            scheduler.shutdown();
        }

    }

}
