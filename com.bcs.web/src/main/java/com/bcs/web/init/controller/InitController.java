package com.bcs.web.init.controller;

import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.bot.scheduler.service.LiveChatTaskService;
import com.bcs.core.bot.scheduler.service.SchedulerService;
import com.bcs.core.db.service.SystemLogService;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.record.service.CatchRecordBinded;
import com.bcs.core.record.service.CatchRecordOpAddReceive;
import com.bcs.core.record.service.CatchRecordOpBlockedReceive;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.scheduler.LoadFtpPnpDataTask;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.service.BillingNoticeFtpService;
import com.bcs.core.taishin.circle.service.BillingNoticeSendMsgService;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.service.LinePointSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

/**
 * Initial Method When Application Startup
 *
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/init")
public class InitController {

    @Autowired
    private SchedulerService schedulerService;
    @Autowired
    private InteractiveService interactiveService;
    @Autowired
    private CatchRecordBinded catchRecordBinded;
    @Autowired
    private CatchRecordOpAddReceive catchRecordOpAddReceive;
    @Autowired
    private CatchRecordOpBlockedReceive catchRecordOpBlockedReceive;
    @Autowired
    private CatchHandleMsgReceiveTimeout catchHandleMsgReceiveTimeout;
    @Autowired
    private CatchRecordReceive catchRecordReceive;
    @Autowired
    private LiveChatTaskService liveChatTaskService;
    @Autowired
    private BillingNoticeFtpService billingNoticeFtpService;
    @Autowired
    private BillingNoticeSendMsgService billingNoticeSendMsgService;
    @Autowired
    private LoadFtpPnpDataTask loadFtpPnpDataTask;
    @Autowired
    private PnpPushMsgService pnpPushMsgService;
    @Autowired
    private PnpPNPMsgService pnpMsgService;
    @Autowired
    private PnpSMSMsgService pnpSmsMsgService;
    @Autowired
    private LinePointSchedulerService linePointschedulerService;
    @Autowired
    private SystemLogService systemLogService;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        registerServer();
        loadScheduleFromDb();
        loadInteractiveMap();
        billingNoticeFtpServiceStartCircle();
        billingNoticeSendMsgServiceStartCircle();
        loadFtpPnpDataTaskStartCircle();
        pnpMsgServiceStartCircle();
        pnpSmsMsgServiceStartCircle();
        linePointschedulerServiceStartCircle(); //這個service註解掉後，預約發送可以正常發送。暫時先註解掉 - by Michael 20190919
        threadStart();
        liveChatTaskServiceCheckUserStatus();
        cleanSystemLogTask();
    }

    /**
     * 1
     */
    private void registerServer() {
        try {
            log.info("init registerServer");
            log.info("init file.encoding:" + System.getProperty("file.encoding"));
            DataSyncUtil.registerServer();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 2
     */
    private void loadScheduleFromDb() {
        try {
            log.info("init loadScheduleFromDB");
            schedulerService.loadScheduleFromDB();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 3
     */
    private void loadInteractiveMap() {
        try {
            log.info("init loadInteractiveMap");
            interactiveService.loadInteractiveMap();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 4
     */
    private void billingNoticeFtpServiceStartCircle() {
        try {
            /* AP */
            if (CoreConfigReader.isBillingNoticeFtpDownload()) {
                log.info("init Billing Notice Data Parse ");
                billingNoticeFtpService.startCircle();
            } else {
                log.info("isBillingNoticeFtpDownload: false");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 5
     */
    private void billingNoticeSendMsgServiceStartCircle() {
        try {
            /* WEB */
            if (CoreConfigReader.isBillingNoticeSendMsg()) {
                log.info("init Billing Notice send ");
                billingNoticeSendMsgService.startCircle();
            } else {
                log.info("isBillingNoticeSendMsg: false");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 6
     */
    private void loadFtpPnpDataTaskStartCircle() {
        /* PNP FTP flow */
        try {
            /* BE-OA : .pnp.ftpDownload */
            if (CoreConfigReader.isPNPFtpDownload()) {
                log.info("init PNP FTP flow ");
                loadFtpPnpDataTask.startCircle();
            } else {
                log.info("isPNPFtpDownload: false");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 7
     */
    private void pnpMsgServiceStartCircle() {
        try {
            /* BE-OAAPI : .pnp.sendMsg */
            if (CoreConfigReader.isPNPSendMsg()) {
                log.info("init pnpPushMsg flow ");
                /* PNP pnpPushMsg flow */
                pnpPushMsgService.startCircle();
                log.info("init PNP PHONE NUMBER PUSH flow ");
                //PNP PHONE NUMBER PUSH flow
                pnpMsgService.startCircle();
            } else {
                log.info("isPNPSendMsg: false");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 8
     */
    private void pnpSmsMsgServiceStartCircle() {
        //PNP transfer file to SMS flow
        try {
            /* BE-OA : .pnp.ftpDownload */
            if (CoreConfigReader.isPNPFtpDownload()) {
                log.info("init PNP transfer file to SMS flow ");
                pnpSmsMsgService.startCircle();
            } else {
                log.info("isPNPFtpDownload: false");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 9. LinePoint Scheduler
     */
    private void linePointschedulerServiceStartCircle() {
        try {
            log.info("init LinePoint Scheduler ");
            linePointschedulerService.startCircle();
        } catch (Throwable e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 10
     */
    private void threadStart() {
        try {
            Thread thread = new Thread(() -> {
                catchRecordBinded.loadInitData();
                catchRecordOpAddReceive.loadInitData();
                catchRecordOpBlockedReceive.loadInitData();
                catchRecordReceive.loadInitData();
                catchHandleMsgReceiveTimeout.loadInitData();
            });
            thread.start();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }


    /**
     * 11. 定期檢查 User Status，避免卡在真人客服頻道
     */
    private void liveChatTaskServiceCheckUserStatus() {
        try {
            liveChatTaskService.checkUserStatus();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 12. 清System Log
     */
    private void cleanSystemLogTask() {
        try {
            int scheduleDay = 1;
            int deleteRangeDay = 30;
            systemLogService.deleteLogByRange(scheduleDay, deleteRangeDay);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}