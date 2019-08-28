package com.bcs.web.ui.service;

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
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.db.service.LinePointScheduledDetailService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.service.SendMsgUIService;

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
	@Autowired
	private SendMsgUIService sendMsgUIService;
	@Autowired
	private LinePointUIService linePointUIService;
	
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
				logger.info("LinePointSchedulerService startCircle....");
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
			try {
				// skip already project already send
				if(linePointMain.getSendStartTime() != null) {
					continue;
				}
				
				// send append message
				Long msgId = linePointMain.getAppendMessageId();
				logger.info("msgId:" + msgId);
				sendMsgUIService.createExecuteSendMsgRunnable(msgId);
				
				// save send start time
				linePointMain.setSendStartTime(new Date());
				linePointMain.setStatus(LinePointMain.STATUS_COMPLETE);
				linePointMain.setModifyTime(new Date());
				linePointUIService.saveLinePointMainFromUI(linePointMain);
				
				// get details
				List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMain.getId());
				logger.info("linePointDetails:"+linePointDetails);
				
				JSONArray detailIds = new JSONArray();
				for(LinePointDetail linePointDetail: linePointDetails) {
					detailIds.put(linePointDetail.getDetailId());
				}
				
				// combine LinePointPushModel
				LinePointPushModel linePointPushModel = new LinePointPushModel();
				linePointPushModel.setEventId(linePointMain.getId());
				linePointPushModel.setDetailIds(detailIds);
				linePointPushModel.setSource(LinePointMain.SEND_TYPE_BCS);
				linePointPushModel.setSendTimeType(LinePointMain.SEND_TIMING_TYPE_IMMEDIATE);
				linePointPushModel.setTriggerTime(new Date());
				
				linePointPushAkkaService.tell(linePointPushModel);
			}catch(Exception e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
}
