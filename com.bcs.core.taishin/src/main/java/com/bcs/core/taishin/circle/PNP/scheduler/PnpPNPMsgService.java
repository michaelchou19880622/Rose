package com.bcs.core.taishin.circle.PNP.scheduler;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgActionRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeDetailRepository;

/**
 * 循環執行以 PHONE NUMBER PUSH(PNP)發送訊息
 * 
 * push成功:更新狀態成功
 * push失敗:轉寄SMS
 * 
 * @author Kenneth
 *
 */
@Service
public class PnpPNPMsgService {

	/** Logger */
	private static Logger logger = Logger.getLogger(PnpPNPMsgService.class);
	@Autowired
	private CircleEntityManagerControl entityManagerControl;
//	@Autowired
//	private PnpService pnpService;
	@Autowired
	private PnpAkkaService pnpAkkaService;
	@Autowired
	private PnpRepositoryCustom pnpRepositoryCustom;
	@Autowired
	private PnpMainEvery8dRepository pnpMainEvery8dRepository;
	@Autowired
	private BillingNoticeDetailRepository billingNoticeDetailRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository;
	
	@Autowired
	private LineUserService lineUserService;
	
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	public PnpPNPMsgService() {
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {

		String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
		int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
		if (time == -1 || TimeUnit.valueOf(unit) == null) {
			logger.error(" PNPSendMsgService TimeUnit error :" + time  + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
				logger.debug(" PnpSendMsgService startCircle....");
				
				//#.pnp.bigswitch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
				int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
				if (1==bigSwitch || 0==bigSwitch) { //大流程關閉時不做
					return;
				}
				
				pnpMitakeMain();
				pnpEvery8dMain();
				pnpUnicaMain();
				pnpMingMain();
				
				
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}
	
	public void pnpMitakeMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("PHONE_NUMBER_PUSH MitakeMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMitake pnpMainMitake = pnpMitakeMain(procApName);
			if (null == pnpMainMitake) {
				logger.debug("sendingMitakeMain not data");
			}else {
				pnpAkkaService.tell(pnpMainMitake);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendingMitakeMain error:" + e.getMessage());
		}
	}
	
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainMitake pnpMitakeMain(String procApName){
		PnpMainMitake pnpMainMitake = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMitakes;
//		}
		
		//Retry detail 找一筆
		PnpDetailMitake pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateMitake(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("pnpMitakeMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPMitake(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("pnpMitakeMain details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("pnpMitakeMain : there is a main has no details!!!");
			return null;
		}
		
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, details);
		
		//組裝資料
		logger.info("pnpMitakeMain handle Details:" + details.size());
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		if(CollectionUtils.isNotEmpty(details)) {
			pnpMainMitake = pnpRepositoryCustom.findMainByMainIdMitake(mainId);
			pnpMainMitake.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
			//pnpMainMitake.setTempId  <<樣版訊息要在這裡設定
			pnpMainMitake.setPnpDetails(details);
		}
		return pnpMainMitake;
	}
	
	public void pnpEvery8dMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}

		} catch (Exception e) {
			logger.error("PHONE_NUMBER_PUSH Every8dMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainEvery8d pnpMainEvery8d = pnpEvery8dMain(procApName);
			if (null == pnpMainEvery8d) {
				logger.debug("sendingEvery8dMain not data");
			}else {
				pnpAkkaService.tell(pnpMainEvery8d);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendingEvery8dMain error:" + e.getMessage());
		}
	}
	
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainEvery8d pnpEvery8dMain(String procApName){
		PnpMainEvery8d pnpMainEvery8d = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainEvery8ds;
//		}
		
		//Retry detail 找一筆
		PnpDetailEvery8d pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateEvery8d(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("pnpEvery8dMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPEvery8d(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("pnpEvery8dMain details size :"+ details.size());

		if(CollectionUtils.isEmpty(details)) {
			logger.error("pnpEvery8dMain : there is a main has no details!!!");
			return null;
		}
		
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, details);
		
		//組裝資料
		logger.info("pnpEvery8dMain handle Details:" + details.size());
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		if(CollectionUtils.isNotEmpty(details)) {
			pnpMainEvery8d = pnpRepositoryCustom.findMainByMainIdEvery8d(mainId);
			pnpMainEvery8d.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
			//pnpMainEvery8d.setTempId  <<樣版訊息要在這裡設定
			pnpMainEvery8d.setPnpDetails(details);
		}
		return pnpMainEvery8d;
	}
	
	public void pnpUnicaMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("PHONE_NUMBER_PUSH UnicaMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainUnica pnpMainUnica = pnpUnicaMain(procApName);
			if (null == pnpMainUnica) {
				logger.debug("sendingUnicaMain not data");
			}else {
				pnpAkkaService.tell(pnpMainUnica);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendingUnicaMain error:" + e.getMessage());
		}
	}
	
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainUnica pnpUnicaMain(String procApName){
		PnpMainUnica pnpMainUnica = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainUnicas;
//		}
		
		//Retry detail 找一筆
		PnpDetailUnica pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateUnica(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("pnpUnicaMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPUnica(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("pnpUnicaMain details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("pnpUnicaMain : there is a main has no details!!!");
			return null;
		}
		
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, details);
		
		//組裝資料
		logger.info("pnpUnicaMain handle Details:" + details.size());
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		if(CollectionUtils.isNotEmpty(details)) {
			pnpMainUnica = pnpRepositoryCustom.findMainByMainIdUnica(mainId);
			pnpMainUnica.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
			//pnpMainUnica.setTempId  <<樣版訊息要在這裡設定
			pnpMainUnica.setPnpDetails(details);
		}
		return pnpMainUnica;
	}
	
	public void pnpMingMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("PHONE_NUMBER_PUSH MingMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMing pnpMainMing = pnpMingMain(procApName);
			if (null == pnpMainMing) {
				logger.debug("sendingMingMain not data");
			}else {
				pnpAkkaService.tell(pnpMainMing);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendingMingMain error:" + e.getMessage());
		}
	}
	
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainMing pnpMingMain(String procApName){
		PnpMainMing pnpMainMing = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMings;
//		}
		
		//Retry detail 找一筆
		PnpDetailMing pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateMing(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("pnpMingMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPMing(
				AbstractPnpMainEntity.STAGE_PNP,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("pnpMingMain details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("pnpMingMain : there is a main has no details!!!");
			return null;
		}
		
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, details);
		
		//組裝資料
		logger.info("pnpMingMain handle Details:" + details.size());
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		if(CollectionUtils.isNotEmpty(details)) {
			pnpMainMing = pnpRepositoryCustom.findMainByMainIdMing(mainId);
			pnpMainMing.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
			//pnpMainMing.setTempId  <<樣版訊息要在這裡設定
			pnpMainMing.setPnpDetails(details);
		}
		return pnpMainMing;
	}

	/**
	 * update mains and details status to sending
	 * @param procApName
	 * @param pnpMains
	 * @param allDetails
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private void updateStatus(String procApName , List<? super PnpDetail> allDetails) {
		Date  now = Calendar.getInstance().getTime();
		if (allDetails != null) {
			List<Object> details = new ArrayList<>();
			for(Object detail : allDetails) {
				((PnpDetail) detail).setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
				details.add(detail);
			}
			if (!details.isEmpty()) {
				entityManagerControl.merge(details);
			}
		}
	}


	/**
	 * Stop Schedule : Wait for Executing Jobs to Finish
	 * 
	 * @throws SchedulerException
	 */
	@PreDestroy
	public void destroy() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			logger.info(" BillingNoticeSendMsgService cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" BillingNoticeSendMsgService shutdown....");
			scheduler.shutdown();
		}

	}

}
