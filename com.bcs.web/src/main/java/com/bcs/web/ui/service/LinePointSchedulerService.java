package com.bcs.web.ui.service;

import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.db.service.LinePointScheduledDetailService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LinePointSchedulerService {

    private LinePointPushAkkaService linePointPushAkkaService;
    private LinePointMainService linePointMainService;
    private LinePointScheduledDetailService linePointScheduledDetailService;
    private SendMsgUIService sendMsgUIService;
    private LinePointUIService linePointUIService;

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("Line-Point-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture = null;

    @Autowired
    public LinePointSchedulerService(LinePointPushAkkaService linePointPushAkkaService,
                                     LinePointMainService linePointMainService,
                                     LinePointScheduledDetailService linePointScheduledDetailService,
                                     SendMsgUIService sendMsgUIService,
                                     LinePointUIService linePointUIService) {
        this.linePointPushAkkaService = linePointPushAkkaService;
        this.linePointMainService = linePointMainService;
        this.linePointScheduledDetailService = linePointScheduledDetailService;
        this.sendMsgUIService = sendMsgUIService;
        this.linePointUIService = linePointUIService;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::pushScheduledLinePoint, 0, 120, TimeUnit.SECONDS);
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info(" LinePointSchedulerService cancel....");
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info(" LinePointSchedulerService shutdown....");
            scheduler.shutdown();
        }
    }

    public void pushScheduledLinePoint() {
        // get allowableLinePointMains
        List<LinePointMain> allowableLinePointMains = linePointMainService.findAllowableIdles();
        log.info("allowableLinePointMains:" + allowableLinePointMains);

        for (LinePointMain linePointMain : allowableLinePointMains) {
            try {
                // skip already project already send
                if (linePointMain.getSendStartTime() != null) {
                    continue;
                }

                // send append message
                Long msgId = linePointMain.getAppendMessageId();
                log.info("msgId:" + msgId);
                sendMsgUIService.createExecuteSendMsgRunnable(msgId);

                // save send start time
                linePointMain.setSendStartTime(new Date());
                linePointMain.setStatus(LinePointMain.STATUS_COMPLETE);
                linePointMain.setModifyTime(new Date());
                linePointUIService.saveLinePointMain(linePointMain);

                // get details
                List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMain.getId());
                log.info("linePointDetails:" + linePointDetails);

                JSONArray detailIds = new JSONArray();
                int i = 1;
                for (LinePointDetail linePointDetail : linePointDetails) {
                    log.info("Total LinePointDetail Detail " + i + ": " + DataUtils.toPrettyJsonUseJackson(linePointDetail));
                    if (!"FAIL".equals(linePointDetail.getStatus())) {
                        detailIds.put(linePointDetail.getDetailId());
                    }
                    i++;
                }
                log.info("To Akka Detail List Size: " + detailIds.toList().size());

                // combine LinePointPushModel
                LinePointPushModel linePointPushModel = new LinePointPushModel();
                linePointPushModel.setEventId(linePointMain.getId());
                linePointPushModel.setDetailIds(detailIds);
                linePointPushModel.setSource(LinePointMain.SEND_TYPE_BCS);
                linePointPushModel.setSendTimeType(LinePointMain.SEND_TIMING_TYPE_IMMEDIATE);
                linePointPushModel.setTriggerTime(new Date());

                linePointPushAkkaService.tell(linePointPushModel);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
            }
        }
    }
}
