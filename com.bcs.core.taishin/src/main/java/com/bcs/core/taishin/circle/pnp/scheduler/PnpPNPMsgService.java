package com.bcs.core.taishin.circle.pnp.scheduler;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpRepositoryCustom;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 循環執行以 PHONE NUMBER PUSH(PNP)發送訊息
 * <p>
 * push成功:更新狀態成功
 * push失敗:轉寄SMS
 *
 * @author Kenneth
 * @author Alan
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpPNPMsgService {

    private PnpAkkaService pnpAkkaService;
    private PnpRepositoryCustom pnpRepositoryCustom;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-PNP-Scheduled-%d")
                    .daemon(true).build()
    );

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
        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SEND_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.error("TimeUnit error :" + time + unit);
            return;
        }
        scheduler.scheduleWithFixedDelay(this::pnpSendProcess, 0, time, TimeUnit.valueOf(unit));
    }

    private void pnpSendProcess() {
        log.info("StartCircle....");
        /* pnp.big switch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行) */
        int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIG_SWITCH, true, false);
        if (0 == bigSwitch || 1 == bigSwitch) {
            return;
        }
        sendingPnpMain();
    }

    /**
     * 發現BC各種失敗狀態並發送PNP
     */
    public void sendingPnpMain() {
        String procApName = DataUtils.getProcApName();
        PnpStatusEnum[] statusEnumArray = {
                PnpStatusEnum.BC_SENT_FAIL_PNP_PROCESS,
                PnpStatusEnum.BC_UID_NOT_FOUND_PNP_PROCESS,
                PnpStatusEnum.BC_USER_IN_BLACK_LIST_PNP_PROCESS,
                PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE_PNP
        };
        Arrays.stream(PnpFtpSourceEnum.values()).forEach(
                type -> Arrays.stream(statusEnumArray).forEach(
                        bcStatus -> sendMain(type, bcStatus)
                )
        );

    }

    private void sendMain(PnpFtpSourceEnum type, PnpStatusEnum bcStatus) {
        log.info(String.format("PNP Push Type: %s, BC status: %s",type.english.toUpperCase(), bcStatus.value));
        final int onceProcessMainNumber = 100;
        try {
            for (int i = 0; i < onceProcessMainNumber; i++) {
                /* 1. Find Bc fail detail */
                List<PnpDetail> allBcToPnpDetail = pnpRepositoryCustom.findAllBcToPnpDetail(type, PnpStageEnum.PNP, bcStatus);
                if (allBcToPnpDetail.isEmpty()) {
                    log.info("Detail list is empty!!");
                    return;
                }

                Long mainId = ((PnpDetail) allBcToPnpDetail.get(0)).getPnpMainId();
                log.info("Main id is {}", mainId);
                /* 2. Find detail main */
                List<PnpMain> allMainList = pnpRepositoryCustom.findMainById(type, mainId);
                if (allMainList.isEmpty()) {
                    log.info("Main list is empty!!");
                    return;
                }
                PnpMain main = (PnpMain) allMainList.get(0);
                /* 3. Merge main and detail */
                main.setPnpDetails(allBcToPnpDetail);
                /* 4. Foreach tell akka */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Tell Akka Send PNP!!");
                pnpAkkaService.tell(main);
            }
        } catch (Exception e) {
            log.error("", e);
            log.error("pnpMain type :" + type.english + " sendingMain error:" + e.getMessage());
        }
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("Shutdown....");
            scheduler.shutdown();
        }
    }
}
