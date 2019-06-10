package com.bcs.core.taishin.circle.PNP.scheduler;

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

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;

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
	private PnpAkkaService pnpAkkaService;
	@Autowired
	private PnpRepositoryCustom pnpRepositoryCustom;
	
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
				sendingPnpMain();
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}
	
	/**
	 * 根據PNPFTPType 依序發送PNP
	 */
	public void sendingPnpMain(){
		String procApName = pnpAkkaService.getProcApName();
		for (PNPFTPType type : PNPFTPType.values()) {
			PnpMain pnpMain = null;
			try {
				//update待發送資料 status(Sending) &excuter name(hostname)
				List<? super PnpDetail> details = pnpRepositoryCustom.updateStatus(type, procApName, AbstractPnpMainEntity.STAGE_PNP);
				logger.info("pnpMain details type :"+ type  +" details size:" + details.size());
				if(CollectionUtils.isEmpty(details)) {
					logger.debug("pnpMain type :"+ type  +" there is a main has no details!!!");
				}else {
					PnpDetail oneDetail = (PnpDetail)details.get(0);
					//組裝資料
					pnpMain = pnpRepositoryCustom.findMainByMainId(type, oneDetail.getPnpMainId());
					if (null == pnpMain) {
						logger.info("pnpMain type :"+ type  +"sendingMain not data");
					}else {
						pnpMain.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
						pnpMain.setPnpDetails(details);
						pnpAkkaService.tell(pnpMain);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("pnpMain type :"+ type  +" sendingMain error:" + e.getMessage());
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
