package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
@Slf4j
@Service
public class PnpPushMsgService {

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
     */
    public void startCircle() {

        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.error("PNPSendMsgService TimeUnit error :" + time + unit);
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            Thread.currentThread().setName("PNP-BC-Scheduled-" + Thread.currentThread().getId());

            log.info("PnpSendMsgService startCircle....");
            //#.pnp.big switch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
            int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
            if (0 == bigSwitch || 1 == bigSwitch) {
                log.warn("PNP_BIG_SWITCH : " + bigSwitch + "PnpPushMsgService stop sending...");
                return;
            }
            sendingMain();
        }, 0, time, TimeUnit.valueOf(unit));

    }

    /**
     * 根據PNPFTPType 依序Push
     */
    private void sendingMain() {
        String procApName = pnpAkkaService.getProcessApName();
        for (PnpFtpSourceEnum type : PnpFtpSourceEnum.values()) {
            log.info(String.format("BC Push ProcApName %s, Type: %s", procApName, type));
            try {
                Set<Long> allMainIds = new HashSet<>();
                List<? super PnpDetail> details = pnpRepositoryCustom.updateStatusByStageBc(type, procApName, allMainIds);
                if (details.isEmpty()) {
                    log.info("details not data type:" + type);
                    continue;
                }
                log.info("details has data type:" + type);

                List<? super PnpDetail> details2 = findDetailUid(details);
                Long[] mainIds = allMainIds.toArray(new Long[0]);

                PnpMain pnpMain = pnpRepositoryCustom.findMainByMainId(type, mainIds[0]);
                log.info("Sending handle Main:" + pnpMain.getOrigFileName() + " type:" + type);
                pnpMain.setPnpDetails(details2);
                log.info("Tell Akka Send BC!!");
                pnpAkkaService.tell(pnpMain);

            } catch (Exception e) {
                log.error("Exception", e);
                log.error(" pnpMain type :" + type + " sendingMain error:" + e.getMessage());
            }
        }
    }

    /**
     * @param details details
     * @return XXXPnpDetail
     */
    private List<? super PnpDetail> findDetailUid(List<? super PnpDetail> details) {
        List<String> phoneNumberList = addAllFormatPhoneNumberToList(details);
        log.info("phoneNumberList : {}", DataUtils.toPrettyJsonUseJackson(phoneNumberList));

        /* Return Original Object */
        if (CollectionUtils.isEmpty(phoneNumberList)) {
            return details;
        }

        /* 透過電話號碼清單查尋UID */
        List<LineUser> lineUserList = lineUserService.findByMobileIn(phoneNumberList);
        log.info("LineUserList:{}", DataUtils.toPrettyJsonUseJackson(lineUserList));
        Map<String, String> uidPhoneNumberMap = generatePhoneNumberUidMapWithoutBlock(lineUserList);
        Map<String, String> phoneNumberStatusMap = generatePhoneNumberStatusMap(lineUserList);
        log.info("uidPhoneNumberMap   :{}", DataUtils.toPrettyJsonUseJackson(uidPhoneNumberMap));
        log.info("phoneNumberStatusMap:{}", DataUtils.toPrettyJsonUseJackson(phoneNumberStatusMap));
        for (int i = 0; i < details.size(); i++) {
            PnpDetail detail = (PnpDetail) details.get(i);
            log.info("Phone Number : {}", detail.getPhone());
            detail.setUid(getUidByPhoneNumberMap(uidPhoneNumberMap, detail.getPhone()));
            detail.setBindStatus(getBindStatusByPhoneNumber(phoneNumberStatusMap, detail.getPhone()));
            details.set(i, detail);
        }
        return details;
    }

    private Map<String, String> generatePhoneNumberStatusMap(List<LineUser> lineUserList) {
        Map<String, String> map = new HashMap<>(lineUserList.size());
        for (LineUser lineUser : lineUserList) {
            map.put(lineUser.getMobile(), lineUser.getStatus());
        }
        return map;
    }

    /**
     * Set Uid By Phone Number Map
     *
     * @param phoneNumberStatusMap phoneNumberStatusMap
     * @param phoneNumber          phoneNumber
     * @return UID
     */
    private String getBindStatusByPhoneNumber(Map<String, String> phoneNumberStatusMap, String phoneNumber) {
        String phoneE164 = formatPhoneNumberToE164(phoneNumber);
        return phoneNumberStatusMap.containsKey(phoneE164) ? phoneNumberStatusMap.get(phoneE164) : phoneNumberStatusMap.get(phoneNumber);
    }

    /**
     * Set Uid By Phone Number Map
     *
     * @param uidPhoneNumberMap uidPhoneNumberMap
     * @param phoneNumber       phoneNumber
     * @return UID
     */
    private String getUidByPhoneNumberMap(Map<String, String> uidPhoneNumberMap, String phoneNumber) {
        String phoneE164 = formatPhoneNumberToE164(phoneNumber);
        return uidPhoneNumberMap.containsKey(phoneE164) ? uidPhoneNumberMap.get(phoneE164) : uidPhoneNumberMap.get(phoneNumber);
    }

    /**
     * 製作電話號碼UID對應表
     *
     * @param lineUserList line User List
     * @return Map Key: Phone Number, Value: Uid
     */
    private Map<String, String> generatePhoneNumberUidMapWithoutBlock(List<LineUser> lineUserList) {
        Map<String, String> uidPhoneMap = new HashMap<>(lineUserList.size());
        for (LineUser lineUser : lineUserList) {
            if (LineUser.STATUS_BLOCK.equals(lineUser.getStatus())) {
                continue;
            }
            uidPhoneMap.put(lineUser.getMobile(), lineUser.getMid());
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
            log.info("Cancel....");
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("Shutdown....");
            scheduler.shutdown();
        }
    }
}
