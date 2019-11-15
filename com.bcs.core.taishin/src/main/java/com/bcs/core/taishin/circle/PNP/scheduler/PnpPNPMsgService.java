package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 循環執行以 PHONE NUMBER PUSH(PNP)發送訊息
 * <p>
 * push成功:更新狀態成功
 * push失敗:轉寄SMS
 *
 * @author Kenneth
 */
@Service
public class PnpPNPMsgService {

    private static Logger logger = Logger.getLogger(PnpPNPMsgService.class);

    private PnpAkkaService pnpAkkaService;
    private PnpRepositoryCustom pnpRepositoryCustom;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-PNP-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public PnpPNPMsgService(PnpAkkaService pnpAkkaService, PnpRepositoryCustom pnpRepositoryCustom) {
        this.pnpAkkaService = pnpAkkaService;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
    }

    /**
     * Start Schedule
     *
     * @see com.bcs.web.init.controller.InitController#init()
     */
    public void startCircle() {

        for (PNPFTPType type : PNPFTPType.values()) {
            logger.info("PNPFTPType : " + type);
        }

        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            logger.error(" PNPSendMsgService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            // 排程工作
            logger.info(" PnpSendMsgService startCircle....");

            /* pnp.big switch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行) */
            int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
            /* 大流程關閉時不做 */
            if (1 == bigSwitch || 0 == bigSwitch) {
                return;
            }
            sendingPnpMain();
        }, 0, time, TimeUnit.valueOf(unit));

    }

    /**
     * 根據PNPFTPType 依序發送PNP
     */
    public void sendingPnpMain() {
        String procApName = pnpAkkaService.getProcessApName();
        for (PNPFTPType type : PNPFTPType.values()) {
            logger.info(String.format("PNP Push ProcApName %s, Type: %s", procApName, type.toString().toUpperCase()));
            PnpMain pnpMain;
            try {
                /* Update 待發送資料 Status(Sending) & Executor name(hostname)*/
                List<? super PnpDetail> details = pnpRepositoryCustom.updateStatus(type, procApName, AbstractPnpMainEntity.STAGE_PNP);
                if (CollectionUtils.isEmpty(details)) {
                    logger.info("Detail is Empty:  " + type.toString().toUpperCase());
                    continue;
                }
                logger.info(String.format("PNP FTP Type: %s, Detail Size: %d", type, details.size()));
                logger.info("details has data type:" + type.toString());
                PnpDetail oneDetail = (PnpDetail) details.get(0);
                //組裝資料
                pnpMain = pnpRepositoryCustom.findMainByMainId(type, oneDetail.getPnpMainId());
                if (null == pnpMain) {
                    logger.info("pnpMain type :" + type + "sendingMain not data");
                    continue;
                }
                logger.info("Sending handle Main:" + pnpMain.getOrigFileName() + " type:" + type);
                pnpMain.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
                pnpMain.setPnpDetails(details);
                logger.info("Tell Akka Send PNP!!");
                pnpAkkaService.tell(pnpMain);

            } catch (Exception e) {
                logger.error(e);
                logger.error("pnpMain type :" + type + " sendingMain error:" + e.getMessage());
            }

        }
    }


    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
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
