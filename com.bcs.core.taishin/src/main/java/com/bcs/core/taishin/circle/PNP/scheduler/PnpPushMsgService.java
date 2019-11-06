package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 循環執行以Line push發送訊息
 * <p>
 * push成功:更新狀態成功
 * push失敗:跟據使用者設定的發送通路參數更新狀態為轉寄PNP或轉寄SMS或更新狀態失敗
 *
 * @author Kenneth
 */
@Service
public class PnpPushMsgService {

    private static Logger logger = Logger.getLogger(PnpPushMsgService.class);

    private PnpAkkaService pnpAkkaService;
    private PnpRepositoryCustom pnpRepositoryCustom;
    private LineUserService lineUserService;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-BC-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture = null;

    @Autowired
    public PnpPushMsgService(PnpAkkaService pnpAkkaService, PnpRepositoryCustom pnpRepositoryCustom, LineUserService lineUserService) {
        this.pnpAkkaService = pnpAkkaService;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
        this.lineUserService = lineUserService;
    }

    /**
     * Start Schedule
     *
     * @throws SchedulerException   the scheduler exception
     * @throws InterruptedException the interrupted exception
     */
    public void startCircle() throws SchedulerException, InterruptedException {

        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            logger.error("PNPSendMsgService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("PNP-BC-Scheduled-" + Thread.currentThread().getId());
                // 排程工作
                logger.info("PnpSendMsgService startCircle....");

                //#.pnp.big switch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
                int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
                if (0 == bigSwitch || 1 == bigSwitch) { //大流程關閉時不做
                    logger.warn("PNP_BIG_SWITCH : " + bigSwitch + "PnpPushMsgService stop sending...");
                    return;
                }
                sendingMain();
            }
        }, 0, time, TimeUnit.valueOf(unit));

    }

    /**
     * 根據PNPFTPType 依序Push
     */
    public void sendingMain() {
        String procApName = pnpAkkaService.getProcessApName();
        for (PNPFTPType type : PNPFTPType.values()) {
            logger.info(String.format("BC Push ProcApName %s, Type: %s", procApName, type));
            try {
                Set<Long> allMainIds = new HashSet<>();
                List<? super PnpDetail> details = pnpRepositoryCustom.updateStatusByStageBc(type, procApName, allMainIds);
                if (details.isEmpty()) {
                    logger.info("details not data type:" + type.toString());
                    continue;
                }
                logger.info("details has data type:" + type.toString());
                List<? super PnpDetail> details2 = findDetailUid(details);
                Long[] mainIds = allMainIds.toArray(new Long[0]);
                PnpMain pnpMain = pnpRepositoryCustom.findMainByMainId(type, mainIds[0]);
                logger.info("Sending handle Main:" + pnpMain.getOrigFileName() + " type:" + type);
                pnpMain.setPnpDetails(details2);
                logger.info("Tell Akka Send BC!!");
                pnpAkkaService.tell(pnpMain);

            } catch (Exception e) {
                logger.error(e);
                logger.error(" pnpMain type :" + type + " sendingMain error:" + e.getMessage());
            }
        }
    }

    /**
     * @param details details
     * @return XXXPnpDetail
     */
    private List<? super PnpDetail> findDetailUid(List<? super PnpDetail> details) {
        List<String> phoneNumberList = addAllFormatPhoneNumberToList(details);

        /* Return Original Object */
        if (CollectionUtils.isNotEmpty(phoneNumberList)) {
            return details;
        }

        /* 透過電話號碼清單查尋UID */
        List<Object[]> uidPhoneList = lineUserService.findMidsByMobileIn(phoneNumberList);
        Map<String, String> uidPhoneNumberMap = generatePhoneNumberUidMap(uidPhoneList);

        for (int i = 0; i < details.size(); i++) {
            PnpDetail detail = (PnpDetail) details.get(i);
            String uid = setUidByPhoneNumberMap(uidPhoneNumberMap, detail);
            detail.setUid(uid);
            details.set(i, detail);
        }
        return details;
    }

    /**
     * Set Uid By Phone Number Map
     *
     * @param uidPhoneNumberMap uidPhoneNumberMap
     * @param detail            detail
     * @return UID
     */
    private String setUidByPhoneNumberMap(Map<String, String> uidPhoneNumberMap, PnpDetail detail) {
        String phone = detail.getPhone();
        String phoneE164 = formatPhoneNumberToE164(phone);
        return uidPhoneNumberMap.containsKey(phoneE164) ? uidPhoneNumberMap.get(phoneE164) : uidPhoneNumberMap.get(phone);
    }

    /**
     * 製作電話號碼UID對應表
     *
     * @param uidAndPhoneList Uid And Phone Number List
     * @return Map Key: Phone Number, Value: Uid
     */
    private Map<String, String> generatePhoneNumberUidMap(List<Object[]> uidAndPhoneList) {
        Map<String, String> uidPhoneMap = new HashMap<>(uidAndPhoneList.size());
        for (Object[] midPhone : uidAndPhoneList) {
            uidPhoneMap.put((String) midPhone[0], (String) midPhone[1]);
        }
        return uidPhoneMap;
    }

    /**
     * 加入所有格式電話號碼
     *
     * @param details details
     * @return All Format Phone Number List
     */
    private List<String> addAllFormatPhoneNumberToList(List<? super PnpDetail> details) {
        List<String> phoneNumberList = new ArrayList<>();
        for (Object detail : details) {
            String phone = ((PnpDetail) detail).getPhone();
            phoneNumberList.add(phone);
            phone = formatPhoneNumberToE164(phone);
            phoneNumberList.add(phone);
        }
        return phoneNumberList;
    }

    /**
     * 格式化電話號碼為E.164國際電信組織制定的通信編碼格式
     * [國碼]+[區碼]+[電話號碼]
     *
     * @param phoneNumber phone number
     * @return newPhoneNumber
     */
    private String formatPhoneNumberToE164(String phoneNumber) {
        String newPhoneNumber = "";
        if ("0".equals(phoneNumber.substring(0, 1))) {
            newPhoneNumber = "+886" + phoneNumber.substring(1);
        } else if ("+".equals(phoneNumber.substring(0, 1))) {
            newPhoneNumber = "0" + phoneNumber.substring(4);
        }
        return newPhoneNumber;
    }


    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
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
