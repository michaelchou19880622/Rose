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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "BNRecorder")
@Service
public class BillingNoticeSendMsgService {

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
        int time = CoreConfigReader.getInteger(CONFIG_STR.BN_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.error(" BillingNoticeSendMsgService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::sendProcess, 0, time, TimeUnit.valueOf(unit));

    }

    private void sendProcess() {
        log.info("BillingNoticeSendMsgService startCircle.... ");
        boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIGSWITCH, true, false);
        log.info("帳務通知大開關: {}", bigSwitch);
        if (!bigSwitch) {
            return;
        }
        try {
            List<BillingNoticeMain> billingNoticeMainList = sendingBillingNoticeMain(getProcApName());
            if (billingNoticeMainList.isEmpty()) {
                log.info("Main List is Empty!!");
                return;
            }
            AtomicInteger i = new AtomicInteger();
            billingNoticeMainList.forEach(billingNoticeMain -> {
                i.getAndIncrement();
                log.info("To Akka BillingNoticeMain {}: {}", i, DataUtils.toPrettyJsonUseJackson(billingNoticeMain));
            });
            billingNoticeMainList.forEach(billingNoticeMain -> billingNoticeAkkaService.tell(billingNoticeMain));
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private String getProcApName() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (localAddress != null) {
                return localAddress.getHostName();
            }
        } catch (Exception e) {
            log.error("getHostName error:" + e.getMessage());
        }
        return null;
    }

    /**
     * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
     * 更新BillingNoticeMain & BillingNoticeDetail status
     */
    @SuppressWarnings("unchecked")
    public List<BillingNoticeMain> sendingBillingNoticeMain(String procApName) {
        List<String> templateIdList = billingNoticeService.findProductSwitchOnTemplateId();
        if (templateIdList == null || templateIdList.isEmpty()) {
            log.info("Template Id List Is Empty!!");
            return Collections.emptyList();
        }

        // 更新狀態
        Object[] returnArray = billingNoticeRepositoryCustom.updateStatus(procApName, templateIdList);
        Set<Long> allMainIdSet = (Set<Long>) returnArray[0];
        List<BillingNoticeDetail> allDetails = (List<BillingNoticeDetail>) returnArray[1];

        if (allMainIdSet.isEmpty()) {
            log.info("Main Id Set Is Empty!!");
            return Collections.emptyList();
        }

        List<BillingNoticeMain> billingNoticeMainList = new ArrayList<>();
        //組裝資料
        for (Long mainId : allMainIdSet) {
            BillingNoticeMain bnMain = billingNoticeMainRepository.findOne(mainId);
            log.info("Original File Name: {}", bnMain.getOrigFileName());
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
