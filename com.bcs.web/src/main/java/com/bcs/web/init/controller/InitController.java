package com.bcs.web.init.controller;

import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.bot.scheduler.service.LiveChatTaskService;
import com.bcs.core.bot.scheduler.service.SchedulerService;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Initial Method When Application Startup
 *
 * @author ???
 */
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

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(InitController.class);

		

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
        pnpSMSMsgServiceStartCircle();
        linePointschedulerServiceStartCircle();
        threadStart();
        liveChatTaskServiceCheckUserStatus();

    }

    /**
     * 1
     */
    private void registerServer() {
        try {
            logger.info("init registerServer");
            logger.info("init file.encoding:" + System.getProperty("file.encoding"));
            DataSyncUtil.registerServer();
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 2
     */
    private void loadScheduleFromDb() {
        try {
            logger.info("init loadScheduleFromDB");
            schedulerService.loadScheduleFromDB();
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 3
     */
    private void loadInteractiveMap() {
        try {
            logger.info("init loadInteractiveMap");
            interactiveService.loadInteractiveMap();
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 4
     */
    private void billingNoticeFtpServiceStartCircle() {
        try {
            /* AP */
            if (CoreConfigReader.isBillingNoticeFtpDownload()) {
                logger.info("init Billing Notice Data Parse ");
                billingNoticeFtpService.startCircle();
            } else {
                logger.info("isBillingNoticeFtpDownload: false");
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 5
     */
    private void billingNoticeSendMsgServiceStartCircle() {
        try {
            /* WEB */
            if (CoreConfigReader.isBillingNoticeSendMsg()) {
                logger.info("init Billing Notice send ");
                billingNoticeSendMsgService.startCircle();
            } else {
                logger.info("isBillingNoticeSendMsg: false");
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 6
     */
    private void loadFtpPnpDataTaskStartCircle() {
        /* PNP FTP flow */
        try {
            /* WEB */
            if (CoreConfigReader.isPNPFtpDownload()) {
                logger.info("init PNP FTP flow ");
                loadFtpPnpDataTask.startCircle();
            } else {
                logger.info("isPNPFtpDownload: false");
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 7
     */
    private void pnpMsgServiceStartCircle() {
        try {
            /* WEB */
            if (CoreConfigReader.isPNPSendMsg()) {
                logger.info("init pnpPushMsg flow ");
                /* PNP pnpPushMsg flow */
                pnpPushMsgService.startCircle();
                logger.info("init PNP PHONE NUMBER PUSH flow ");
                //PNP PHONE NUMBER PUSH flow
                pnpMsgService.startCircle();
            } else {
                logger.info("isPNPSendMsg: false");
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 8
     */
    private void pnpSMSMsgServiceStartCircle() {
        //PNP transfer file to SMS flow
        try {
            /* WEB */
            if (CoreConfigReader.isPNPFtpDownload()) {
                logger.info("init PNP transfer file to SMS flow ");
                pnpSmsMsgService.startCircle();
            } else {
                logger.info("isPNPFtpDownload: false");
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 9. LinePoint Scheduler
     */
    private void linePointschedulerServiceStartCircle() {
        try {
            logger.info("init LinePoint Scheduler ");
            linePointschedulerService.startCircle();
        } catch (Throwable e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 10
     */
    private void threadStart() {
        try {
            Thread thread = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            catchRecordBinded.loadInitData();
                            catchRecordOpAddReceive.loadInitData();
                            catchRecordOpBlockedReceive.loadInitData();
                            catchRecordReceive.loadInitData();
                            catchHandleMsgReceiveTimeout.loadInitData();
                        }
                    });
            thread.start();
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 11. 定期檢查 User Status，避免卡在真人客服頻道
     */
    private void liveChatTaskServiceCheckUserStatus() {
        try {
            liveChatTaskService.checkUserStatus();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * cleanUp
     */
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] InitController cleaning up...");
        logger.info("[DESTROY] InitController destroyed.");
    }
}
