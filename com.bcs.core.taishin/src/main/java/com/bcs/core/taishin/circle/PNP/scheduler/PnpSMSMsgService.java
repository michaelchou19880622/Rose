package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStageEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.mutable.StringBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 循環執行 回組發送失敗資料成來源資料格式  > 將TXT檔放到SMS指定位置 > 更新資料狀態
 *
 * @author Kenneth, Alan
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpSMSMsgService {

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-SMS-Scheduled-%d")
                    .daemon(true).build()
    );


    private PnpAkkaService pnpAkkaService;
    private PNPFtpService pnpFtpService;

    private PnpDetailMitakeRepository pnpDetailMitakeRepository;
    private PnpDetailMingRepository pnpDetailMingRepository;
    private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
    private PnpDetailUnicaRepository pnpDetailUnicaRepository;

    private PnpMainMitakeRepository pnpMainMitakeRepository;
    private PnpMainMingRepository pnpMainMingRepository;
    private PnpMainEvery8dRepository pnpMainEvery8dRepository;
    private PnpMainUnicaRepository pnpMainUnicaRepository;


    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public PnpSMSMsgService(PnpAkkaService pnpAkkaService,
                            PNPFtpService pnpFtpService,
                            PnpDetailMitakeRepository pnpDetailMitakeRepository,
                            PnpDetailMingRepository pnpDetailMingRepository,
                            PnpDetailEvery8dRepository pnpDetailEvery8dRepository,
                            PnpDetailUnicaRepository pnpDetailUnicaRepository,
                            PnpMainMitakeRepository pnpMainMitakeRepository,
                            PnpMainMingRepository pnpMainMingRepository,
                            PnpMainEvery8dRepository pnpMainEvery8dRepository,
                            PnpMainUnicaRepository pnpMainUnicaRepository) {
        this.pnpAkkaService = pnpAkkaService;
        this.pnpFtpService = pnpFtpService;

        this.pnpDetailMitakeRepository = pnpDetailMitakeRepository;
        this.pnpDetailMingRepository = pnpDetailMingRepository;
        this.pnpDetailEvery8dRepository = pnpDetailEvery8dRepository;
        this.pnpDetailUnicaRepository = pnpDetailUnicaRepository;

        this.pnpMainMitakeRepository = pnpMainMitakeRepository;
        this.pnpMainMingRepository = pnpMainMingRepository;
        this.pnpMainEvery8dRepository = pnpMainEvery8dRepository;
        this.pnpMainUnicaRepository = pnpMainUnicaRepository;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.error("TimeUnit error :" + time + unit);
            return;
        }
        /* 排程工作 */
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
            if (1 == bigSwitch || 0 == bigSwitch) {
                log.warn("PNP_BIG_SWITCH : " + bigSwitch + " PnpSMSMsgService stop transfer file to SMS FTP ..");
                return;
            }
            sendingSmsMainForBcFail();
            sendingSmsMainForPnpFail();
            sendingSmsMainForDeliveryExpired();
        }, 0, time, TimeUnit.valueOf(unit));
    }

    /**
     * Resent Sms
     *
     * @param detailId      Detail Id
     * @param ftpSourceEnum Ftp Source Enum
     * @return is Success
     */
    public boolean resendSms(long detailId, PnpFtpSourceEnum ftpSourceEnum) {
        if (ftpSourceEnum == null) {
            return false;
        }
        switch (ftpSourceEnum) {
            case MING:
                /* Find Detail */
                PnpDetailMing detail1 = pnpDetailMingRepository.findOne(detailId);
                List<PnpDetailMing> detailList = Collections.singletonList(detail1);

                /* Find Main */
                PnpMainMing main = pnpMainMingRepository.findOne(detail1.getPnpMainId());
                main.setPnpDetailMingList(detailList);

                /* Change Sms File Name */
                String smsFileName = changeFileName(main.getOrigFileName(), new Date());
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(ftpSourceEnum, smsMingInputStream(detailList), main.getSmsFileName());
                return true;
            case UNICA:
                /* Find Detail */
                PnpDetailUnica detail2 = pnpDetailUnicaRepository.findOne(detailId);
                List<PnpDetailUnica> detailList2 = Collections.singletonList(detail2);

                /* Find Main */
                PnpMainUnica main2 = pnpMainUnicaRepository.findOne(detail2.getPnpMainId());
                main2.setPnpDetailUnicaList(detailList2);

                /* Change Sms File Name */
                String smsFileName2 = changeFileName(main2.getOrigFileName(), new Date());
                main2.setSmsFileName(smsFileName2);

                /* Send File To SMS FTP */
                uploadFileToSms(ftpSourceEnum, smsUnicaInputStream(main2, detailList2), main2.getSmsFileName());
                return true;
            case MITAKE:
                /* Find Detail */
                PnpDetailMitake detail3 = pnpDetailMitakeRepository.findOne(detailId);
                List<PnpDetailMitake> detailList3 = Collections.singletonList(detail3);

                /* Find Main */
                PnpMainMitake main3 = pnpMainMitakeRepository.findOne(detail3.getPnpMainId());
                main3.setPnpDetailMitakeList(detailList3);

                /* Change Sms File Name */
                String smsFileName3 = changeFileName(main3.getOrigFileName(), new Date());
                main3.setSmsFileName(smsFileName3);

                /* Send File To SMS FTP */
                uploadFileToSms(ftpSourceEnum, smsMitakeInputStream(main3, detailList3), main3.getSmsFileName());
                return true;
            case EVERY8D:
                /* Find Detail */
                PnpDetailEvery8d detail4 = pnpDetailEvery8dRepository.findOne(detailId);
                List<PnpDetailEvery8d> detailList4 = Collections.singletonList(detail4);

                /* Find Main */
                PnpMainEvery8d main4 = pnpMainEvery8dRepository.findOne(detail4.getPnpMainId());
                main4.setPnpDetailEvery8dList(detailList4);

                /* Change Sms File Name */
                String smsFileName4 = changeFileName(main4.getOrigFileName(), new Date());
                main4.setSmsFileName(smsFileName4);

                /* Send File To SMS FTP */
                uploadFileToSms(ftpSourceEnum, smsEvery8dInputStream(main4, detailList4), main4.getSmsFileName());
                return true;
            default:
                break;
        }
        return true;
    }

    /**
     * 處理BC-Fail過期資料轉發SMS
     *
     * @see this#startCircle
     */
    private void sendingSmsMainForBcFail() {
        PnpStatusEnum[] statusArray = {
                PnpStatusEnum.BC_SENT_FAIL_SMS_PROCESS,
                PnpStatusEnum.BC_UID_NOT_FOUND_SMS_PROCESS,
                PnpStatusEnum.BC_USER_BLOCKED_SMS_PROCESS,
                PnpStatusEnum.BC_USER_IN_BLACK_LIST_SMS_PROCESS,
                PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE_SMS
        };
        for (PnpStatusEnum status : statusArray) {
            mitakeProcess(status);
            mingProcess(status);
            unicaProcess(status);
            every8dProcess(status);
        }
    }

    private void mitakeProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MITAKE;

        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has BC Fail To SMS Data : {}, {} =========", type, status);
            /* Find Expired Data*/
            List<PnpDetailMitake> detailList = pnpDetailMitakeRepository.findTop1ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No BC Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailMitake detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMitake> afterSaveList = pnpDetailMitakeRepository.save(detailList);
            log.info("Update Status: [BC][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailMitake detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMitake main = pnpMainMitakeRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMitakeList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMitakeInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMitake afterSaveMain = pnpMainMitakeRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMitake afterSaveDetail = pnpDetailMitakeRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void mingProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MING;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has BC Fail To SMS Data : {}, {} =========", type, status);
            /* Find Expired Data*/
            List<PnpDetailMing> detailList = pnpDetailMingRepository.findTop1ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No BC Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailMing detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMing> afterSaveList = pnpDetailMingRepository.save(detailList);
            log.info("Update Status: [BC][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailMing detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMing main = pnpMainMingRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMingList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMingInputStream(afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMing afterSaveMain = pnpMainMingRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMing afterSaveDetail = pnpDetailMingRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void unicaProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.UNICA;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has BC Fail To SMS Data : {}, {} =========", type, status);
            /* Find Expired Data*/
            List<PnpDetailUnica> detailList = pnpDetailUnicaRepository.findTop1ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No BC Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailUnica detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailUnica> afterSaveList = pnpDetailUnicaRepository.save(detailList);
            log.info("Update Status: [BC][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailUnica detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainUnica main = pnpMainUnicaRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailUnicaList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsUnicaInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainUnica afterSaveMain = pnpMainUnicaRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailUnica afterSaveDetail = pnpDetailUnicaRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void every8dProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.EVERY8D;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has BC Fail To SMS Data : {}, {} =========", type, status);
            /* Find Expired Data*/
            List<PnpDetailEvery8d> detailList = pnpDetailEvery8dRepository.findTop1ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No BC Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailEvery8d detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailEvery8d> afterSaveList = pnpDetailEvery8dRepository.save(detailList);
            log.info("Update Status: [BC][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailEvery8d detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainEvery8d main = pnpMainEvery8dRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailEvery8dList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsEvery8dInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainEvery8d afterSaveMain = pnpMainEvery8dRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailEvery8d afterSaveDetail = pnpDetailEvery8dRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * 處理PNP-Fail過期資料轉發SMS
     *
     * @see this#startCircle
     */
    private void sendingSmsMainForPnpFail() {
        PnpStatusEnum[] statusEnums = {
          PnpStatusEnum.PNP_SENT_TO_LINE_FAIL_SMS_PROCESS,
          PnpStatusEnum.PNP_USER_IN_BLACK_LIST_SMS_PROCESS
        };
        for (PnpStatusEnum status : statusEnums) {
            mitakePnpFailProcess(status);
            mingPnpFailProcess(status);
            unicaPnpFailProcess(status);
            every8dPnpFailProcess(status);
        }
    }

    private void mitakePnpFailProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MITAKE;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has PNP Fail To SMS Data : {} =========", type);
            /* Find Expired Data*/
            List<PnpDetailMitake> detailList = pnpDetailMitakeRepository.findTop1ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No PNP Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailMitake detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMitake> afterSaveList = pnpDetailMitakeRepository.save(detailList);
            log.info("Update Status: [PNP][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailMitake detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMitake main = pnpMainMitakeRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMitakeList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMitakeInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMitake afterSaveMain = pnpMainMitakeRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMitake afterSaveDetail = pnpDetailMitakeRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void mingPnpFailProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MING;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has PNP Fail To SMS Data : {} =========", type);
            /* Find Expired Data*/
            List<PnpDetailMing> detailList = pnpDetailMingRepository.findTop1ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No PNP Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailMing detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMing> afterSaveList = pnpDetailMingRepository.save(detailList);
            log.info("Update Status: [PNP][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailMing detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMing main = pnpMainMingRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMingList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMingInputStream(afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMing afterSaveMain = pnpMainMingRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMing afterSaveDetail = pnpDetailMingRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void unicaPnpFailProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.UNICA;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has PNP Fail To SMS Data : {} =========", type);
            /* Find Expired Data*/
            List<PnpDetailUnica> detailList = pnpDetailUnicaRepository.findTop1ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No PNP Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailUnica detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailUnica> afterSaveList = pnpDetailUnicaRepository.save(detailList);
            log.info("Update Status: [PNP][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailUnica detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainUnica main = pnpMainUnicaRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailUnicaList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsUnicaInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainUnica afterSaveMain = pnpMainUnicaRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailUnica afterSaveDetail = pnpDetailUnicaRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void every8dPnpFailProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.EVERY8D;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has PNP Fail To SMS Data : {} =========", type);
            /* Find Expired Data*/
            List<PnpDetailEvery8d> detailList = pnpDetailEvery8dRepository.findTop1ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(status.value);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No PNP Fail Data!!", type);
                return;
            }

            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetailEvery8d detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailEvery8d> afterSaveList = pnpDetailEvery8dRepository.save(detailList);
            log.info("Update Status: [PNP][FAIL] to [SMS][SENDING] Count : {}", afterSaveList.size());

            now = new Date();
            for (PnpDetailEvery8d detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainEvery8d main = pnpMainEvery8dRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailEvery8dList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsEvery8dInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainEvery8d afterSaveMain = pnpMainEvery8dRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailEvery8d afterSaveDetail = pnpDetailEvery8dRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }


    /**
     * 處理PNP-Delivery過期資料轉發SMS
     */
    private void sendingSmsMainForDeliveryExpired() {
        PnpStatusEnum status = PnpStatusEnum.PNP_SENT_CHECK_DELIVERY;
        mitakeDeliveryProcess(status);
        mingDeliveryProcess(status);
        unicaDeliveryProcess(status);
        every8dDeliveryProcess(status);
    }

    /**
     * mitakeDeliveryProcess
     */
    private void mitakeDeliveryProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MITAKE;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has Pnp Delivery Expired Data : {} =========", type);
            Date now = new Date();

            /* Find Expired Data*/
            List<PnpDetailMitake> detailList = pnpDetailMitakeRepository.findTop1ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(
                    status.value, now);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No Expired Data!!", type);
                return;
            }

            now = new Date();
            /* Update Expired Status */
            for (PnpDetailMitake detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_EXPIRED_FAIL_SMS_PROCESS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMitake> afterSaveList = pnpDetailMitakeRepository.save(detailList);
            log.info("{} Update Status: [PNP][CHECK_DELIVERY] to [PNP][FAIL] and [SMS][SENDING] Count : {}", type, afterSaveList);

            now = new Date();
            for (PnpDetailMitake detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMitake main = pnpMainMitakeRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMitakeList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMitakeInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMitake afterSaveMain = pnpMainMitakeRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMitake afterSaveDetail = pnpDetailMitakeRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * mingDeliveryProcess
     */
    private void mingDeliveryProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.MING;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has Pnp Delivery Expired Data : {} =========", type);
            Date now = new Date();

            /* Find Expired Data*/
            List<PnpDetailMing> detailList = pnpDetailMingRepository.findTop1ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(
                    status.value, now);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No Expired Data!!", type);
                return;
            }

            now = new Date();
            /* Update Expired Status */
            for (PnpDetailMing detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_EXPIRED_FAIL_SMS_PROCESS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailMing> afterSaveList = pnpDetailMingRepository.save(detailList);
            log.info("{} Update Status: [PNP][CHECK_DELIVERY] to [PNP][FAIL] and [SMS][SENDING] Count : {}", type, afterSaveList);

            now = new Date();
            for (PnpDetailMing detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainMing main = pnpMainMingRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailMingList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsMingInputStream(afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainMing afterSaveMain = pnpMainMingRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailMing afterSaveDetail = pnpDetailMingRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * unicaDeliveryProcess
     */
    private void unicaDeliveryProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.UNICA;
        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has Pnp Delivery Expired Data : {} =========", type);
            Date now = new Date();

            /* Find Expired Data*/
            List<PnpDetailUnica> detailList = pnpDetailUnicaRepository.findTop1ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(
                    status.value, now);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No Expired Data!!", type);
                return;
            }

            now = new Date();
            /* Update Expired Status */
            for (PnpDetailUnica detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_EXPIRED_FAIL_SMS_PROCESS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailUnica> afterSaveList = pnpDetailUnicaRepository.save(detailList);
            log.info("{} Update Status: [PNP][CHECK_DELIVERY] to [PNP][FAIL] and [SMS][SENDING] Count : {}", type, afterSaveList);

            now = new Date();
            for (PnpDetailUnica detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainUnica main = pnpMainUnicaRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailUnicaList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsUnicaInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainUnica afterSaveMain = pnpMainUnicaRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailUnica afterSaveDetail = pnpDetailUnicaRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * every8dDeliveryProcess
     */
    private void every8dDeliveryProcess(PnpStatusEnum status) {
        PnpFtpSourceEnum type = PnpFtpSourceEnum.EVERY8D;

        try {
            String procApName = pnpAkkaService.getProcessApName();
            log.info("Start Check Has Pnp Delivery Expired Data : {} =========", type);
            Date now = new Date();

            /* Find Expired Data*/
            List<PnpDetailEvery8d> detailList = pnpDetailEvery8dRepository.findTop1ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(
                    status.value, now);

            if (CollectionUtils.isEmpty(detailList)) {
                log.info("{} No Expired Data!!", type);
                return;
            }

            now = new Date();
            /* Update Expired Status */
            for (PnpDetailEvery8d detail : detailList) {
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_EXPIRED_FAIL_SMS_PROCESS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            /* Update Status */
            List<PnpDetailEvery8d> afterSaveList = pnpDetailEvery8dRepository.save(detailList);
            log.info("{} Update Status: [PNP][CHECK_DELIVERY] to [PNP][FAIL] and [SMS][SENDING] Count : {}", type, afterSaveList);

            now = new Date();
            for (PnpDetailEvery8d detail : afterSaveList) {
                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));

                /* Find Main By Detail Id */
                PnpMainEvery8d main = pnpMainEvery8dRepository.findOne(detail.getPnpMainId());
                main.setPnpDetailEvery8dList(detailList);
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setSmsFileName(smsFileName);

                /* Send File To SMS FTP */
                uploadFileToSms(type, smsEvery8dInputStream(main, afterSaveList), main.getSmsFileName());

                /* Save Main */
                main.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                main.setProcApName(procApName);
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMainEvery8d afterSaveMain = pnpMainEvery8dRepository.save(main);
                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetailEvery8d afterSaveDetail = pnpDetailEvery8dRepository.save(detail);
                log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));

                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * Change File Name (L)
     * 發送失敗的資料檔名 O_PRMSMS_250102OCSPENDING_20190624155433000.txt
     *
     * @param originFileName Origin File Name
     * @param newTime        New Time
     * @return new File Name
     * @apiNote SMS只處理當日資料，因此將檔名中的時間變更為新時間
     */
    private String changeFileName(String originFileName, Date newTime) {
        String smsFileName = String.format("%s_L_%s.txt",
                originFileName.substring(0, originFileName.lastIndexOf('_')),
                DataUtils.formatDateToString(newTime, "yyyyMMddHHmmssSSS"));
        log.info("Ori FileName : " + originFileName);
        log.info("SMS FileName : " + smsFileName);
        return smsFileName;
    }

    /**
     * Mitake
     *
     * @param main       main
     * @param detailList detail List
     * @return InputStream
     */
    private InputStream smsMitakeInputStream(PnpMainMitake main, List<PnpDetailMitake> detailList) {
        String tag = "&";
        /*
         * 三竹 header
         * 欄位            型態       長度     説明
         * GroupID        Varchar    10      群組代號 (TSBANK) 需由三竹簡訊中心設定後方能使用，使用者無法更改。/必填
         * Username       Varchar    20      使用者(帳號)新版簡訊掛帳依據
         * UserPassword   Varchar    10      此欄位(不需填寫)請保留空格(以’&’符號分隔) ex.TSBANK&jack& &2003********00&9999&0
         * OrderTime      Char       14      預約時間 ‘yyyymmddhhmmss’固定14位數簡訊預約時間。簡訊何時送達手機，格式為YYYYMMDDhhmmss
         *                                   # 預約時間大於系統時間，則為預約簡訊。
         *                                   # 預約時間已過或為空白則為即時簡訊。
         *                                   即時簡訊為儘早送出，若受到第6個宵禁欄位的限制，就不一定是立刻送出。
         * ValidityTime   Char       14      有效分鐘數(受限於電信業者：建議勿超過24 H)(只能帶數字)
         * MsgType        Char       1       訊息型態 :宵禁‘0’ 表一般通知簡訊 ->run 09:00~19:00  ‘1’  表警急通知簡訊 ->run 00:00~24:00
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(main.getGroupIDSource() + tag);
        header.append(main.getUsername() + "_L" + tag);
        header.append(main.getUserPassword() + tag);
        header.append(main.getOrderTime() + tag);
        header.append(main.getValidityTime() + tag);
        header.append(main.getMsgType() + "\r\n");

        log.info("Mitake Header: {}", DataUtils.toPrettyJsonUseJackson(header));

        /*
         * 三竹  body
         * 欄位             型態      長度  説明
         * DestCategory    Char      8    "掛帳代碼" =>  PCC Code
         * DestName        Varchar   20   請填入系統有意義之流水號(open端可辯示之唯一序號)
         * DestNo          Varchar   20   手機門號/請填入09帶頭的手機號碼。
         * MsgData         Varchar   333  請勿輸入 % $ ' 字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。若有換行的需求，請以ASCII Code 6代表換行。必填。
         */

        StringBuilder body = new StringBuilder();

        for (PnpDetailMitake detail : detailList) {
            body.append(detail.getDestCategory() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + "\r\n");
        }
        log.info("Mitake Body: {}", DataUtils.toPrettyJsonUseJackson(header));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    /**
     * Every8d
     *
     * @param main       main
     * @param detailList detailList
     * @return InputStream
     */
    private InputStream smsEvery8dInputStream(PnpMainEvery8d main, List<PnpDetailEvery8d> detailList) {
        String tag = "&";
        /*
         * 互動 header
         * 名稱          屬性       長度      Null?    說明
         * Subject      NVARCHAR   200      Y        簡訊主旨
         * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
         * Password     CHAR                Y        使用者密碼(可不填)
         * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
         * ExpireTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
         * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
         * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(main.getSubject() + tag);
        header.append(main.getUserID() + "_L" + tag);
        header.append(main.getPassword() + tag);
        header.append(main.getOrderTime() + tag);
        header.append(main.getExprieTime() + tag);
        header.append(main.getMsgType() + tag);
        header.append(main.getBatchID() + "\r\n");

        log.info("Every8d Header: {}", DataUtils.toPrettyJsonUseJackson(header));

        /*
         *   互動 body
         *   名稱          屬性        長度    Null?   說明
         *   SN           char        15     N       名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
         *   DestName     char        36     Y       收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
         *   Mobile       char        20     N       收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
         *   Content      nvarchar    756    N       簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
         *   PID          char        11     Y       身份字號
         *   CampaignID   varchar     28     Y       行銷活動代碼(可為空值)
         *   SegmentID    varchar     10     Y       客群代號(可為空值)
         *   ProgramID    varchar     20     Y       階段代號(可為空值)
         *   Variable1    varchar     15     Y       擴充欄位1(可為空值)
         *   Variable2    varchar     15     Y       擴充欄位2(可為空值)
         */

        StringBuilder body = new StringBuilder();
        for (PnpDetailEvery8d detail : detailList) {
            body.append(detail.getSn() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getPid() + tag);
            body.append(detail.getCampaignId() + tag);
            body.append(detail.getSegmentId() + tag);
            body.append(detail.getProgramId() + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + "\r\n");
        }
        log.info("Every8d body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    /**
     * Unica
     *
     * @param main       main
     * @param detailList detail List
     * @return InputStream
     */
    private InputStream smsUnicaInputStream(PnpMainUnica main, List<PnpDetailUnica> detailList) {
        String tag = "&";
        /*
         * 互動 header
         * 名稱          屬性       長度      Null?     說明
         * Subject      NVARCHAR   200      Y        簡訊主旨
         * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
         * Password     CHAR                Y        使用者密碼(可不填)
         * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
         * ExpireTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
         * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
         * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(main.getSubject() + tag);
        header.append(main.getUserID() + "_L" + tag);
        header.append(main.getPassword() + tag);
        header.append(main.getOrderTime() + tag);
        header.append(main.getExprieTime() + tag);
        header.append(main.getMsgType() + tag);
        header.append(main.getBatchID() + "\r\n");

        log.info("Unica Header: {}", DataUtils.toPrettyJsonUseJackson(header));

        /*
         *   互動 body
         *   名稱        屬性        長度   Null   說明
         *   SN         char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
         *   DestName   char        36     Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
         *   Mobile     char        20     N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
         *   Content    nvarchar    756    N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
         *   PID        char        11     Y     身份字號
         *   CampaignID varchar     28     Y     行銷活動代碼(可為空值)
         *   SegmentID  varchar     10     Y     客群代號(可為空值)
         *   ProgramID  varchar     20     Y     階段代號(可為空值)
         *   Variable1  varchar     15     Y     擴充欄位1(可為空值)
         *   Variable2  varchar     15     Y     擴充欄位2(可為空值)
         */

        StringBuilder body = new StringBuilder();
        for (PnpDetailUnica detail : detailList) {
            body.append(detail.getSn() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getPid() + tag);
            body.append(detail.getCampaignId() + tag);
            body.append(detail.getSegmentId() + tag);
            body.append(detail.getProgramId() + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + "\r\n");
        }
        log.info("Unica Body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }

    /**
     * 明宣格式
     *
     * @param detailList detailList
     * @return InputStream
     * @apiNote 流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
     */
    private InputStream smsMingInputStream(List<PnpDetailMing> detailList) {
        StringBuilder body = new StringBuilder();
        String tag = ";;";
        for (PnpDetailMing detail : detailList) {
            body.append(detail.getSn() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getDetailScheduleTime() + tag);
            body.append(detail.getAccount1() + "_L" + tag);
            body.append(detail.getAccount2() + "_L" + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + tag);
            body.append(detail.getKeepSecond() + "\r\n");
        }
        log.info("Ming Body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((body.toString()).getBytes());
    }


    /**
     * Upload File To SMS
     *
     * @param source       source
     * @param targetStream targetStream
     * @param fileName     fileName
     */
    private void uploadFileToSms(PnpFtpSourceEnum source, InputStream targetStream, String fileName) {
        log.info("Start Upload File To SMS!!");

        if (targetStream == null) {
            log.error("targetStream is null");
            return;
        }
        PNPFtpSetting setting = pnpFtpService.getFtpSettings(source);
        if (StringUtils.isBlank(setting.getAccount())
                || StringUtils.isBlank(setting.getPassword())) {
            log.error("FTP account or password is blank!!");
            return;
        }
        log.info("Upload File Name" + fileName);

        try {
            pnpFtpService.uploadFileByType(targetStream, fileName, setting.getUploadPath(), setting);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
}
