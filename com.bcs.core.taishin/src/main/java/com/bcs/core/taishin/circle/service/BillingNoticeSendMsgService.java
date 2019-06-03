package com.bcs.core.taishin.circle.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgActionRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeMainRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeRepositoryCustom;

@Service
public class BillingNoticeSendMsgService {

	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeSendMsgService.class);
	@Autowired
	private BillingNoticeService billingNoticeService;
	@Autowired
	private BillingNoticeAkkaService billingNoticeAkkaService;
	@Autowired
	private BillingNoticeRepositoryCustom billingNoticeRepositoryCustom;
	@Autowired
	private BillingNoticeMainRepository billingNoticeMainRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository;
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	public BillingNoticeSendMsgService() {
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {

		String unit = CoreConfigReader.getString(CONFIG_STR.BN_SCHEDULE_UNIT, true, false);
		int time = CoreConfigReader.getInteger(CONFIG_STR.BN_SCHEDULE_TIME, true, false);
		if (time == -1 || TimeUnit.valueOf(unit) == null) {
			logger.error(" BillingNoticeSendMsgService TimeUnit error :" + time  + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
				logger.debug(" BillingNoticeSendMsgService startCircle....");
				sendingBillingNoticeMain();
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}
	
	private void sendingBillingNoticeMain() {
		boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIGSWITCH, true, false);
		if (!bigSwitch) { //大流程關閉時不做
			return;
		}
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}

		} catch (Exception e) {
			logger.error("getHostName error:" + e.getMessage());
		}
		try {
			List<BillingNoticeMain> billingNoticeMains = sendingBillingNoticeMain(procApName);
			if (billingNoticeMains.isEmpty()) {
				logger.debug("sendingBillingNoticeMain not data");
			}
			for (BillingNoticeMain billingNoticeMain : billingNoticeMains) {
				billingNoticeAkkaService.tell(billingNoticeMain);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendingBillingNoticeMain error:" + e.getMessage());
		}
		
	}
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 */
	public List<BillingNoticeMain> sendingBillingNoticeMain( String procApName){
		List<BillingNoticeMain> billingNoticeMains = new ArrayList<>();
		List<String> templateIds = billingNoticeService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
		if (templateIds == null || templateIds.isEmpty()) {
			return billingNoticeMains;
		}
		Set<Long>  allMainIds = new  HashSet<Long>(); 
		List<BillingNoticeDetail> allDetails = new ArrayList<BillingNoticeDetail>();
		
		// 更新狀態
		billingNoticeRepositoryCustom.updateStatus(procApName, templateIds, allMainIds, allDetails);
		
		if (allMainIds.isEmpty()) {
			return  new ArrayList<>();
		}
		
		//組裝資料
		for (Long mainId : allMainIds) {
			BillingNoticeMain bnMain =  billingNoticeMainRepository.findOne(mainId);
			logger.info("sendingBillingNoticeMain handle Main:" + bnMain.getOrigFileName());
			List<BillingNoticeDetail> details = new ArrayList<>();
			for (BillingNoticeDetail detail : allDetails) {
				if (detail.getNoticeMainId().longValue() == mainId.longValue() ) {
					details.add(detail);
				}
			}
			bnMain.setDetails(details);
			BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
			if (template != null) {
				bnMain.setTemplate(template);
				List<BillingNoticeContentTemplateMsgAction>  actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
				bnMain.setTemplateActions(actions);
				billingNoticeMains.add(bnMain);
			}else {
				logger.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
			}
			
		}
		
		return billingNoticeMains;
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
