package com.bcs.web.init.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.bot.scheduler.service.LiveChatTaskService;
import com.bcs.core.bot.scheduler.service.SchedulerService;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.record.service.CatchRecordBinded;
import com.bcs.core.record.service.CatchRecordOpAddReceive;
import com.bcs.core.record.service.CatchRecordOpBlockedReceive;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.scheduler.LoadFtbPnpDataTask;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.service.BillingNoticeFtpService;
import com.bcs.core.taishin.circle.service.BillingNoticeSendMsgService;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;

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
	private LoadFtbPnpDataTask loadFtbPnpDataTask;
	@Autowired
	private PnpPushMsgService pnpPushMsgService;
	@Autowired
	private PnpPNPMsgService pnpPNPMsgService;
	@Autowired
	private PnpSMSMsgService pnpSMSMsgService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(InitController.class);

	public InitController(){
		logger.info("Constructor InitController");
	}
	
	@PostConstruct
	public void init(){

		try {
			logger.info("init registerServer");
			DataSyncUtil.registerServer();
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
			logger.info("init loadScheduleFromDB");
			schedulerService.loadScheduleFromDB();
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
			logger.info("init loadInteractiveMap");
			interactiveService.loadInteractiveMap();
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isBillingNoticeFtpDownload()) {//AP
				logger.info("init Billing Notice Data Prase ");
				billingNoticeFtpService.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isBillingNoticeSendMsg()) {//WEB
				logger.info("init Billing Notice send ");
				billingNoticeSendMsgService.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		//PNP FTP flow
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isPNPFtpDownload()) {//WEB
				logger.info("init PNP FTP flow ");
				loadFtbPnpDataTask.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		//PNP pnpPushMsg flow
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isPNPSendMsg()) {//WEB
			logger.info("init pnpPushMsg flow ");
			pnpPushMsgService.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		//PNP PHONE NUMBER PUSH flow
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isPNPSendMsg()) {//WEB
				logger.info("init PNP PHONE NUMBER PUSH flow ");
				pnpPNPMsgService.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		//PNP transfer file to SMS flow
		try {
			//判斷WEB OR AP
			if(CoreConfigReader.isPNPFtpDownload()) {//WEB
				logger.info("init PNP transfer file to SMS flow ");
				pnpSMSMsgService.startCircle();
			}
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		

		try {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					catchRecordBinded.loadInitData();
					catchRecordOpAddReceive.loadInitData();
					catchRecordOpBlockedReceive.loadInitData();
					catchRecordReceive.loadInitData();
					catchHandleMsgReceiveTimeout.loadInitData();
				}
			});
			
			thread.start();
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		/* 定期檢查 User 的 status，避免卡在真人客服頻道 */
		try {
			liveChatTaskService.checkUserStatus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * cleanUp
	 */
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] InitController cleaning up...");
		
		System.gc();
		logger.info("[DESTROY] InitController destroyed.");
	}
}
