package com.bcs.core.taishin.circle.service;

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
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j(topic = "BNRecorder")
@Service
public class BillingNoticeSendMsgService {
	public final static AtomicLong scheduleTaskcount = new AtomicLong(0L);
    private BillingNoticeService billingNoticeService;
    private BillingNoticeAkkaService billingNoticeAkkaService;
    private BillingNoticeRepositoryCustom billingNoticeRepositoryCustom;
    private BillingNoticeMainRepository billingNoticeMainRepository;
    private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
    private BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("BN-Send-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture = null;

    @Autowired
    public BillingNoticeSendMsgService(BillingNoticeService billingNoticeService,
                                       BillingNoticeAkkaService billingNoticeAkkaService,
                                       BillingNoticeRepositoryCustom billingNoticeRepositoryCustom,
                                       BillingNoticeMainRepository billingNoticeMainRepository,
                                       BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository,
                                       BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository) {
        this.billingNoticeService = billingNoticeService;
        this.billingNoticeAkkaService = billingNoticeAkkaService;
        this.billingNoticeRepositoryCustom = billingNoticeRepositoryCustom;
        this.billingNoticeMainRepository = billingNoticeMainRepository;
        this.billingNoticeContentTemplateMsgRepository = billingNoticeContentTemplateMsgRepository;
        this.billingNoticeContentTemplateMsgActionRepository = billingNoticeContentTemplateMsgActionRepository;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        String unit = CoreConfigReader.getString(CONFIG_STR.BN_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.BN_SEND_SCHEDULE_TIME, true, false);

        log.info("Starting a scheduled task for BN sending service, unit={} time={}", unit, time);
        if (time == -1) {
            log.error(" BillingNoticeSendMsgService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::sendProcess, 0, time, TimeUnit.valueOf(unit));
        log.info("Started the scheduled task for BN sending service successfully!");
    }

    private void sendProcess() {
    	scheduleTaskcount.addAndGet(1L);
        log.info("Started a new task to send BN messages, scheduleTaskCount=" + scheduleTaskcount.get());
        boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIG_SWITCH, true, false);
        if (!bigSwitch) {
            return;
        }
        try {
            List<BillingNoticeMain> billingNoticeMainList = sendingBillingNoticeMain(DataUtils.getProcApName());
            if (billingNoticeMainList.isEmpty()) {
            	log.info("Finished the task due to no jobs at this time, scheduleTaskCount=" + scheduleTaskcount.get());
            }
            else {
                billingNoticeMainList.forEach(billingNoticeMain -> {
                    log.info("Telling Akka a job, noticeMainId={} status={}", billingNoticeMain.getNoticeMainId(), billingNoticeMain.getStatus());              
                });
                billingNoticeMainList.forEach(billingNoticeMain -> billingNoticeAkkaService.tell(billingNoticeMain));
                log.info("Submitted all of BN sending requests successfully, scheduleTaskCount=" + scheduleTaskcount.get() + " jobSize=" + billingNoticeMainList.size());
            }
        } catch (Exception e) {
        	log.info("An exception detected during processing the sending task for BN service!");
            log.error("Exception", e);
        }
    }

    /**
     * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
     * 更新BillingNoticeMain & BillingNoticeDetail status
     */
    @SuppressWarnings("unchecked")
    public List<BillingNoticeMain> sendingBillingNoticeMain(String procApName) {
    	List<BillingNoticeMain> billingNoticeMainList = new ArrayList<>();

    	try {
    		log.info("Inquiring BN jobs whose status is WAIT or RETRY, procApName={}", procApName);
            List<String> templateIdList = billingNoticeService.findProductSwitchOnTemplateId();
            if (templateIdList == null || templateIdList.isEmpty()) {
            	log.info("Skipped the jobs due to no enabled template found");
                return Collections.emptyList();
            }
            // 更新狀態
            Object[] returnArray = billingNoticeRepositoryCustom.updateStatus(procApName, templateIdList);
            Set<Long> allMainIdSet = (Set<Long>) returnArray[0];
            log.info("allMainIdSet={}", allMainIdSet);
            List<BillingNoticeDetail> allDetails = (List<BillingNoticeDetail>) returnArray[1];
            if (allMainIdSet.isEmpty()) {
                return Collections.emptyList();
            }
            //組裝資料
            for (Long mainId : allMainIdSet) {
                BillingNoticeMain bnMain = billingNoticeMainRepository.findOne(mainId);
                log.info("noticeMainId={}, originalFileName={}", bnMain.getNoticeMainId(), bnMain.getOrigFileName());
                List<BillingNoticeDetail> details = new ArrayList<>();
                for (BillingNoticeDetail detail : allDetails) {
                    if (detail.getNoticeMainId().longValue() == mainId.longValue()) {
                        details.add(detail);
                    }
                }
                bnMain.setDetails(details);
                BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(bnMain.getTempId());
                if (template == null) {
                    log.error("BillingNoticeContentTemplateMsg :" + bnMain.getTempId() + " is null");
                    continue;
                }
                bnMain.setTemplate(template);
                List<BillingNoticeContentTemplateMsgAction> actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(template.getTemplateId());
                bnMain.setTemplateActions(actions);
                billingNoticeMainList.add(bnMain);
            }
    	} catch (Exception e) {
            log.error("Exception", e);
        }
        return billingNoticeMainList;
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info(" BillingNoticeSendMsgService cancel....");
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            log.info(" BillingNoticeSendMsgService shutdown....");
            scheduler.shutdown();
        }
    }
}
