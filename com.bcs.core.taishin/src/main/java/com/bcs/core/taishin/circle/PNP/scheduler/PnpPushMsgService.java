package com.bcs.core.taishin.circle.PNP.scheduler;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgActionRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;

/**
 * 循環執行以Line push發送訊息
 * 
 * push成功:更新狀態成功
 * push失敗:跟據使用者設定的發送通路參數更新狀態為轉寄PNP或轉寄SMS或更新狀態失敗
 * 
 * @author Kenneth
 *
 */
@Service
public class PnpPushMsgService {

	/** Logger */
	private static Logger logger = Logger.getLogger(PnpPushMsgService.class);
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
	private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository;
	
	@Autowired
	private LineUserService lineUserService;
	
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	public PnpPushMsgService() {
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
					logger.warn("PNP_BIGSWITCH : "+bigSwitch +"PnpPushMsgService stop sendding...");
					return;
				}
				
				sendingMitakeMain();
				sendingEvery8dMain();
				sendingUnicaMain();
				sendingMing();
				
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}
	
	public void sendingMitakeMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("sendingMitakeMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMitake pnpMainMitake = sendingMitakeMain(procApName);
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
	 * 更新pnpMain & pnpDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainMitake sendingMitakeMain(String procApName){
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMitakes;
//		}
		// search Main status = WAIT找一筆
		PnpMainMitake waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateMitake(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		if(null == waitMain) {
			logger.info("sendingMitakeMain find NO mains status is Wait!!!");
			return null;
		}
		
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailByStatusForUpdateMitake(statusList, waitMain.getPnpMainId());
		
		logger.info("PnpDetailMitake details size :"+ details.size());

		if(CollectionUtils.isEmpty(details)) {
			logger.error("sendingMitakeMain : there is a main has no details!!!");
			return null;
		}
		
		details = findDetailUid(details);
		
		List<PnpMainMitake> waitMainList = new ArrayList<>();
		waitMainList.add(waitMain);
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, waitMainList, details);
		
		//組裝資料
		logger.info("sendingMitakeMain handle Main:" + waitMain.getOrigFileName());
		waitMain.setPnpDetails(details);
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		
		return waitMain;
	}
	
	public void sendingEvery8dMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}

		} catch (Exception e) {
			logger.error("sendingEvery8dMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainEvery8d pnpMainEvery8d = sendingEvery8dMain(procApName);
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
	 * 更新pnpMain & pnpDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainEvery8d sendingEvery8dMain(String procApName){
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainEvery8ds;
//		}
		// search Main status = WAIT找一筆
		PnpMainEvery8d waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateEvery8d(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		if(null == waitMain) {
			logger.info("sendingEvery8dMain find NO mains status is Wait!!!");
			return null;
		}
		
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailByStatusForUpdateEvery8d(statusList, waitMain.getPnpMainId());
		
		logger.info("PnpDetailEvery8d details size :"+ details.size());

		if(CollectionUtils.isEmpty(details)) {
			logger.error("sendingEvery8dMain : there is a main has no details!!!");
			return null;
		}
		
		details = findDetailUid(details);
		
		List<PnpMainEvery8d> waitMainList = new ArrayList<>();
		waitMainList.add(waitMain);
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, waitMainList, details);
		
		//組裝資料
		logger.info("sendingEvery8dMain handle Main:" + waitMain.getOrigFileName());
		waitMain.setPnpDetails(details);
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		
		return waitMain;
	}
	
	public void sendingUnicaMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("sendingUnicaMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainUnica pnpMainUnica = sendingUnicaMain(procApName);
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
	 * 更新pnpMain & pnpDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public PnpMainUnica sendingUnicaMain(String procApName){
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainUnicas;
//		}
		// search Main status = WAIT找一筆
		PnpMainUnica waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateUnica(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		if(null == waitMain) {
			logger.info("sendingUnicaMain find NO mains status is Wait!!!");
			return null;
		}
		
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailByStatusForUpdateUnica(statusList, waitMain.getPnpMainId());
		
		logger.info("PnpDetailUnica details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("sendingUnicaMain : there is a main has no details!!!");
			return null;
		}
		
		details = findDetailUid(details);
		
		List<PnpMainUnica> waitMainList = new ArrayList<>();
		waitMainList.add(waitMain);
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, waitMainList, details);
		
		//組裝資料
		logger.info("sendingUnicaMain handle Main:" + waitMain.getOrigFileName());
		waitMain.setPnpDetails(details);
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		
		return waitMain;
	}
	
	public void sendingMing(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("sendingMingMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMing pnpMainMing = sendingMing(procApName);
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
	public PnpMainMing sendingMing(String procApName){
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMings;
//		}
		// search Main status = WAIT找一筆
		PnpMainMing waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateMing(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		if(null == waitMain) {
			logger.info("sendingMingMain find NO mains status is wait!!!");
			return null;
		}
		
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailByStatusForUpdateMing(statusList, waitMain.getPnpMainId());
		
		logger.info("PnpDetailMing details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("sendingMingMain : there is a main has no details!!!");
			return null;
		}
		
		details = findDetailUid(details);
		
		List<PnpMainMing> waitMainList = new ArrayList<>();
		waitMainList.add(waitMain);
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatus(procApName, waitMainList, details);
		
		//組裝資料
		logger.info("sendingMingMain handle Main:" + waitMain.getOrigFileName());
		waitMain.setPnpDetails(details);
//		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
//		if (template != null) {
//			bnMain.setTemplate(template);
//			List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
//			bnMain.setTemplateActions(actions);
//			billingNoticeMains.add(bnMain);
//		}else {
//			logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
//		}
		
		return waitMain;
	}

	private List<? super PnpDetail> findDetailUid(List<? super PnpDetail> details) {
		
		
		if(CollectionUtils.isEmpty(details)) { 
			logger.error("PnpPushMsgService.findDetailUid details is empty!!!");
			return null;
		}
		
		//用電話查UID 此段可以考慮搬到parse data flow 現階段考慮parse效能故在發送前做
		List<String> mobilesList = new ArrayList<>();
		
		for(Object detail:details){ //e.164格式 及 09XX格式都查
			String phone = ((PnpDetail) detail).getPhone();
			mobilesList.add(phone);
	        if("0".equals(phone.substring(0, 1))){//phone有些可能已轉成e.164格式，0開頭的再做轉換
	        	phone = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
	        	mobilesList.add(phone);
	        }else if("+".equals(phone.substring(0, 1))){
	        	phone = "0"+phone.substring(4);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
	        	mobilesList.add(phone);
	        }
		}
		
		//TODO 確認電話號碼格式
		if(CollectionUtils.isNotEmpty(mobilesList)){
			List<Object[]> midPhoneList = lineUserService.findMidsByMobileIn(mobilesList);
			
			Map<String,String> midPhoneMap = new HashMap<>();
			for(Object[] midPhone : midPhoneList){
				midPhoneMap.put((String) midPhone[0], (String) midPhone[1]);
			}
			
			for(int i = 0 ; i < details.size() ; i++){
				PnpDetail detail = (PnpDetail) details.get(i);
				
				String phone = detail.getPhone();
				String phoneFormatB ="";
				if("0".equals(phone.substring(0, 1))){//phone有些可能已轉成e.164格式，0開頭的再做轉換
					phoneFormatB = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
		        }else if("+".equals(phone.substring(0, 1))){
		        	phoneFormatB = "0"+phone.substring(4);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
		        }
				
				if(midPhoneMap.containsKey(phoneFormatB)) {
					detail.setUid(midPhoneMap.get(phoneFormatB));
				}else {
					detail.setUid(midPhoneMap.get(phone));
				}
				
				details.set(i, detail);
			}
			
			
//			for(PnpDetail detail:details){ 
//				if(midPhoneMap.containsKey(detail.getPhone())){
//					detail.setUid(midPhoneMap.get(detail.getPhone()));
//				}
//			}
		}
		
		return details;
	}
	
	/**
	 * update mains and details status to sending
	 * @param procApName
	 * @param pnpMains
	 * @param allDetails
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private void updateStatus(String procApName,List<? extends PnpMain> pnpMains , List<? super PnpDetail> allDetails) {
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
		if (pnpMains != null) {
			List<Object> mains = new ArrayList<>();
			for(PnpMain main : pnpMains) {
				main.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
				main.setProcApName(procApName);
				main.setModifyTime(now);
				mains.add(main);
			}
			if (!mains.isEmpty()) {
				entityManagerControl.merge(mains);
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
			logger.info(" PnpPushMsgService cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" PnpPushMsgService shutdown....");
			scheduler.shutdown();
		}

	}

}
