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
import com.bcs.core.taishin.circle.pnp.scheduler.LoadFtpPnpDataTask;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpPNPMsgService;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.service.BillingNoticeFtpService;
import com.bcs.core.taishin.circle.service.BillingNoticeSendMsgService;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.service.LinePointSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Initial Method When Application Startup
 *
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/init")
public class InitController {
    private SchedulerService schedulerService;
    private InteractiveService interactiveService;
    private CatchRecordBinded catchRecordBinded;
    private CatchRecordOpAddReceive catchRecordOpAddReceive;
    private CatchRecordOpBlockedReceive catchRecordOpBlockedReceive;
    private CatchHandleMsgReceiveTimeout catchHandleMsgReceiveTimeout;
    private CatchRecordReceive catchRecordReceive;
    private LiveChatTaskService liveChatTaskService;
    private BillingNoticeFtpService billingNoticeFtpService;
    private BillingNoticeSendMsgService billingNoticeSendMsgService;
    private LoadFtpPnpDataTask loadFtpPnpDataTask;
    private PnpPushMsgService pnpPushMsgService;
//    private PnpPNPMsgService pnpMsgService;
    private PnpSMSMsgService pnpSmsMsgService;
    private LinePointSchedulerService linePointschedulerService;
    private SystemLogService systemLogService;
//    private ServerInfoService serverInfoService;

    @Autowired
    public InitController(SchedulerService schedulerService,
                          InteractiveService interactiveService,
                          CatchRecordBinded catchRecordBinded,
                          CatchRecordOpAddReceive catchRecordOpAddReceive,
                          CatchRecordOpBlockedReceive catchRecordOpBlockedReceive,
                          CatchHandleMsgReceiveTimeout catchHandleMsgReceiveTimeout,
                          CatchRecordReceive catchRecordReceive,
                          LiveChatTaskService liveChatTaskService,
                          BillingNoticeFtpService billingNoticeFtpService,
                          BillingNoticeSendMsgService billingNoticeSendMsgService,
                          LoadFtpPnpDataTask loadFtpPnpDataTask,
                          PnpPushMsgService pnpPushMsgService,
//                          PnpPNPMsgService pnpMsgService,
                          PnpSMSMsgService pnpSmsMsgService,
                          LinePointSchedulerService linePointschedulerService,
                          SystemLogService systemLogService
//                          ServerInfoService serverInfoService
    ) {
        this.schedulerService = schedulerService;
        this.interactiveService = interactiveService;
        this.catchRecordBinded = catchRecordBinded;
        this.catchRecordOpAddReceive = catchRecordOpAddReceive;
        this.catchRecordOpBlockedReceive = catchRecordOpBlockedReceive;
        this.catchHandleMsgReceiveTimeout = catchHandleMsgReceiveTimeout;
        this.catchRecordReceive = catchRecordReceive;
        this.liveChatTaskService = liveChatTaskService;
        this.billingNoticeFtpService = billingNoticeFtpService;
        this.billingNoticeSendMsgService = billingNoticeSendMsgService;
        this.loadFtpPnpDataTask = loadFtpPnpDataTask;
        this.pnpPushMsgService = pnpPushMsgService;
//        this.pnpMsgService = pnpMsgService;
        this.pnpSmsMsgService = pnpSmsMsgService;
        this.linePointschedulerService = linePointschedulerService;
        this.systemLogService = systemLogService;
//        this.serverInfoService = serverInfoService;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        log.info("HostName: {}, IP: {}", DataUtils.getProcApName(), DataUtils.getProcApIp());
//        registerServerInfo();

        registerServer();
        loadScheduleFromDb();
        loadInteractiveMap();
        billingNoticeFtpServiceStartCircle();
        billingNoticeSendMsgServiceStartCircle();
        loadFtpPnpDataTaskStartCircle();
        pnpMsgServiceStartCircle();
        pnpSmsMsgServiceStartCircle();
        linePointSchedulerServiceStartCircle(); //這個service註解掉後，預約發送可以正常發送。暫時先註解掉 - by Michael 20190919
        threadStart();
        liveChatTaskServiceCheckUserStatus();
        cleanSystemLogTask();
    }

//    private void registerServerInfo() {
//        try {
//            initLog("REGISTER SERVER INFORMATION!!");
//            ServerInfo serverInfo = new ServerInfo();
//            serverInfo.setComputerName(DataUtils.getProcApName());
//            serverInfo.setIp(DataUtils.getProcApIp());
//            String serverType = CoreConfigReader.getString("server.type", false);
//            ServerInfo.ServerType type = getServerType(serverType);
//            serverInfo.setServerType(type);
//            serverInfo.setCreateTime(new Date());
//            serverInfo.setModifyTime(new Date());
//            serverInfoService.saveServerInfo(serverInfo);
//        }catch (Exception e) {
//            log.error(ErrorRecord.recordError(e));
//        }
//    }
//
//    private ServerInfo.ServerType getServerType(String serverType) {
//        if (StringUtils.isBlank(serverType)){
//            return ServerInfo.ServerType.AP;
//        }
//        try {
//            return ServerInfo.ServerType.valueOf(serverType);
//        } catch (IllegalArgumentException e) {
//            return ServerInfo.ServerType.AP;
//        }
//    }

    /**
     * 1
     */
    private void registerServer() {
        try {
            initLog("REGISTER SERVER!!");
            initLog("file.encoding:" + System.getProperty("file.encoding"));
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
            initLog("LOAD SCHEDULE FROM DB");
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
            initLog("LOAD INTERACTIVE MAP");
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
                initLog("BILLING NOTICE FTP DOWNLOAD!!");
                billingNoticeFtpService.startCircle();
            } else {
                initLog("BILLING NOTICE FTP DOWNLOAD IS CLOSE!!");
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
                initLog("BILLING NOTICE PUSH MESSAGE SCHEDULE!!");
                billingNoticeSendMsgService.startCircle();
            } else {
                initLog("BILLING NOTICE PUSH MESSAGE SCHEDULE IS CLOSE");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 6
     */
    private void loadFtpPnpDataTaskStartCircle() {
        try {
            /* BE-OA : .pnp.ftpDownload */
            if (CoreConfigReader.isPNPFtpDownload()) {
                initLog("PNP FTP DOWNLOAD SCHEDULE!!");
                loadFtpPnpDataTask.startCircle();
            } else {
                initLog("PNP FTP DOWNLOAD SCHEDULE IS CLOSE!!");
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
                initLog("PNP BC  PUSH MESSAGE SCHEDULE!!");
                pnpPushMsgService.startCircle();
//                initLog("PNP PNP PUSH MESSAGE SCHEDULE!!");
//                pnpMsgService.startCircle();
            } else {
                initLog("PNP PUSH MESSAGE IS CLOSE!!");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 8
     */
    private void pnpSmsMsgServiceStartCircle() {
        try {
            /* BE-OA : .pnp.ftpDownload */
            if (CoreConfigReader.isPNPFtpDownload()) {
                initLog("PNP SMS FTP PUSH SCHEDULE!!");
                pnpSmsMsgService.startCircle();
            } else {
                initLog("PNP SMS FTP PUSH SCHEDULE IS CLOSE!!");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 9. LinePoint Scheduler
     */
    private void linePointSchedulerServiceStartCircle() {
        try {
            initLog("init LinePoint Scheduler ");
            linePointschedulerService.startCircle();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 10
     */
    private void threadStart() {
        try {
            new Thread(() -> {
                catchRecordBinded.loadInitData();
                catchRecordOpAddReceive.loadInitData();
                catchRecordOpBlockedReceive.loadInitData();
                catchRecordReceive.loadInitData();
                catchHandleMsgReceiveTimeout.loadInitData();
            }).start();
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
            if (CoreConfigReader.getBoolean("system.log.clean.schedule.enable", true)) {
                int scheduleTime = 30;
                int deleteRangeDay = 30;
                systemLogService.deleteLogByRange(scheduleTime, TimeUnit.SECONDS, deleteRangeDay);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void initLog(String msg) {
        log.info("[INIT] {}", msg);
    }
}