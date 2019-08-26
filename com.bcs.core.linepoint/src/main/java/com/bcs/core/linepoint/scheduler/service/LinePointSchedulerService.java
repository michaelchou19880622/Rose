package com.bcs.core.linepoint.scheduler.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.entity.ShareUserRecord;
import com.bcs.core.db.service.ShareCampaignClickTracingService;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.db.service.LinePointScheduledDetailService;
import com.bcs.core.resource.CoreConfigReader;

@Service
public class LinePointSchedulerService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointSchedulerService.class);
	@Autowired
	LinePointPushAkkaService linePointPushAkkaService;
	@Autowired
	LinePointMainService linePointMainService;
	@Autowired
	LinePointScheduledDetailService linePointScheduledDetailService;
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	public LinePointSchedulerService() {
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {
		// run every day
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				logger.info("LinePointAMSchedulerService startCircle....");
				pushScheduledLinePoint();
			}
		}, 0, 120, TimeUnit.SECONDS);
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
			logger.info(" LinePointSchedulerService cancel....");
		}
		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" LinePointSchedulerService shutdown....");
			scheduler.shutdown();
		}
	}
	
	public void pushScheduledLinePoint() {
		// get allowableLinePointMains			
		List<LinePointMain> allowableLinePointMains = linePointMainService.findAllowableIdles();
		logger.info("allowableLinePointMains:"+allowableLinePointMains);

		for(LinePointMain linePointMain : allowableLinePointMains) {
			
	    	logger.info("LinePointMain.STATUS_SCHEDULED Saving");
	    	// linePointMain.status = scheduled
	    	linePointMain.setStatus(LinePointMain.STATUS_SCHEDULED);
	    	linePointMainService.save(linePointMain);
	    	
	    	// linePointScheduledDetail.status = waiting
	    	LinePointScheduledDetail linePointScheduledDetail = new LinePointScheduledDetail();
	    	//linePointScheduledDetail.setUid(undoneUser.getUid());
	    	linePointScheduledDetail.setLinePointMainId(linePointMain.getId());
	    	linePointScheduledDetail.setStatus(LinePointScheduledDetail.STATUS_WAITING);
	    	linePointScheduledDetail.setModifyTime(new Date());
	    	linePointScheduledDetailService.save(linePointScheduledDetail);
		}
		
		// find linePointMain.status = scheduled
		List<LinePointMain> mains = linePointMainService.findByStatus(LinePointMain.STATUS_SCHEDULED);
		for(LinePointMain main : mains) {
			logger.info("Scheduled LinePointMainId:"+main.getId());
			
			// linePointMain.status = idle
			main.setStatus(LinePointMain.STATUS_IDLE);
			linePointMainService.save(main);
			
			// find linePointScheduledDetail.mainId = mainId
			//List<LinePointScheduledDetail> details = linePointScheduledDetailService.findByLinePointMainId(main.getId());
			
			JSONArray uid = new JSONArray();
//			for(LinePointScheduledDetail detail : details) {
//				//uid.put(detail.getUid());
//				
//				detail.setStatus(LinePointScheduledDetail.STATUS_SENDED);
//				detail.setModifyTime(new Date());
//				linePointScheduledDetailService.save(detail);
//			}
//			logger.info("uid (begin to send):"+uid);
			
			// push to AkkaService
			LinePointPushModel linePointPushModel = new LinePointPushModel();
			//linePointPushModel.setAmount(main.getAmount());
			//linePointPushModel.setUid(uid);
			linePointPushModel.setEventId(main.getId());
			linePointPushModel.setSource(LinePointPushModel.SOURCE_TYPE_BCS);
			//linePointPushModel.setSendTimeType(LinePointPushModel.SEND_TYPE_IMMEDIATE);
			linePointPushModel.setTriggerTime(new Date());
			linePointPushAkkaService.tell(linePointPushModel);
		}
	}
}
