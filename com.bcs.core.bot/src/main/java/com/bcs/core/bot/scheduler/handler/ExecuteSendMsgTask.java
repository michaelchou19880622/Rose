package com.bcs.core.bot.scheduler.handler;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.bot.scheduler.service.SchedulerService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.GroupGenerateService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ???
 */
@Slf4j
public class ExecuteSendMsgTask {

    public void executeSendMsg(Long msgId) throws Exception {
        log.info("executeSendMsg msgId ============ :" + msgId);

        GroupGenerateService groupGenerateService = ApplicationContextProvider.getApplicationContext().getBean(GroupGenerateService.class);
        SendGroupService sendGroupService = ApplicationContextProvider.getApplicationContext().getBean(SendGroupService.class);
        MsgMainService msgMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgMainService.class);
        MsgSendMainService msgSendMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class);
        MsgDetailService msgDetailService = ApplicationContextProvider.getApplicationContext().getBean(MsgDetailService.class);

        int linePointMain = sendGroupService.findLinePointMaincount(msgId);
        if (linePointMain != 0) {
            log.info("This is Line Point 專案訊息");
            return;
        }

        MsgMain msgMain = msgMainService.findOne(msgId);
        log.info("executeSendMsg : msgMain = " + msgMain);

        if (msgMain == null) {
            log.error("Schdeuler MsgId:" + msgId + " Missing");
            SchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class);
            boolean status = schedulerService.deleteMsgSendSchedule(msgId);
            log.error("Schdeuler deleteMsgSendSchedule:" + msgId + " - status - " + status);
            return;
        }

        String groupTitle = "---";
        try {
            SendGroup sendGroup = sendGroupService.findOne(msgMain.getGroupId());
            if (sendGroup == null) {
                throw new BcsNoticeException("查無該群組");
            }
            groupTitle = sendGroup.getGroupTitle();

            Long groupId = sendGroup.getGroupId();
            if (groupId > 0) {
                marketSetting(msgId, groupGenerateService, msgSendMainService, msgDetailService, groupTitle, groupId);
            } else {
                defaultSetting(msgId, sendGroupService, msgSendMainService, msgDetailService, groupTitle, groupId);
            }

            switch (msgMain.getSendType()) {
                case MsgMain.SENDING_MSG_TYPE_DELAY:
                case MsgMain.SENDING_MSG_TYPE_IMMEDIATE:
                    msgMain.setStatus(MsgMain.MESSAGE_STATUS_COMPLETE);
                    msgMainService.save(msgMain);
                    break;
                default:
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));

            if (MsgMain.SENDING_MSG_TYPE_DELAY.equals(msgMain.getSendType())) {
                msgMain.setStatus(MsgMain.MESSAGE_STATUS_FAIL);
                msgMain.setStatusNotice(e.getMessage());
                msgMain.setModifyTime(new Date());
                msgMainService.save(msgMain);

                msgSendMainService.copyFromMsgMain(msgId, -1L, groupTitle, MsgMain.MESSAGE_STATUS_FAIL);

                SchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class);
                boolean status = schedulerService.deleteMsgSendSchedule(msgId);
                log.error("Schdeuler deleteMsgSendSchedule:" + msgId + " - status - " + status);
            } else if (MsgMain.SENDING_MSG_TYPE_SCHEDULE.equals(msgMain.getSendType())) {
                msgSendMainService.copyFromMsgMain(msgId, -1L, groupTitle, MsgMain.MESSAGE_STATUS_FAIL, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 預設群組
     */
    private void defaultSetting(Long msgId, SendGroupService sendGroupService, MsgSendMainService msgSendMainService, MsgDetailService msgDetailService, String groupTitle, Long groupId) throws Exception {
        log.info("DefaultSetting Start!!");
        Long totalCount = sendGroupService.countDefaultGroupSize(groupId);
        log.info("countDefaultGroupSize:" + totalCount);

        MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, totalCount, groupTitle);

        List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

        log.info("findByMsgIdAndMsgParentType");

        this.sendToAdminGroup(msgSendMain, details);

        // Reset Message
        details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

        int pageSize = SendGroupService.pageSize;
        if (totalCount > 80 * 5000) {
            pageSize = 5000;
        }

        int page = 0;
        while (true) {
            List<String> list = sendGroupService.queryDefaultGroup(groupId, page, pageSize);
            log.info("midList:" + DataUtils.toPrettyJsonUseJackson(list));
            if (list == null || list.isEmpty()) {
                break;
            }
            log.debug("queryDefaultGroup:" + list.size());
            sendMsgToMidList(list, details, msgSendMain.getMsgSendId());

            page++;
            if (page % 80 == 0) {
                Thread.sleep(3 * 60 * 1000L);
            }
        }
    }

    /**
     * 行銷人員設定 群組
     */
    private void marketSetting(Long msgId, GroupGenerateService groupGenerateService, MsgSendMainService msgSendMainService, MsgDetailService msgDetailService, String groupTitle, Long groupId) throws Exception {
        log.info("MarketSetting Start!!");
        List<String> midList = groupGenerateService.findMIDBySendGroupDetailGroupId(groupId);
        log.info("midList:" + DataUtils.toPrettyJsonUseJackson(midList));
        if (midList == null || midList.isEmpty()) {
            throw new BcsNoticeException("群組設定錯誤:查不到發送目標");
        }
        log.debug("executeSendMsg midList ============ :" + midList.size());
        MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, (long) midList.size(), groupTitle);

        List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

        this.sendToAdminGroup(msgSendMain, details);

        // Reset Message
        details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

        int pageSize = SendGroupService.pageSize;

        List<String> sendMidList = new ArrayList<>();
        for (String mid : midList) {
            sendMidList.add(mid);

            if (sendMidList.size() % pageSize == 0) {
                sendMsgToMidList(sendMidList, details, msgSendMain.getMsgSendId());
                sendMidList = new ArrayList<>();
            }
        }

        if (!sendMidList.isEmpty()) {
            sendMsgToMidList(sendMidList, details, msgSendMain.getMsgSendId());
        }
    }

    /**
     * Send To Test Group for Check Send
     */
    private void sendToAdminGroup(MsgSendMain msgSendMain, List<MsgDetail> details) {
        log.info("SendToAdminGroup Start!!");
        try {
            AdminUserService adminUserService = ApplicationContextProvider.getApplicationContext().getBean(AdminUserService.class);

            List<AdminUser> list = adminUserService.findByMidNotNull();
            List<String> midListTest = new ArrayList<>();
            if (list != null && !list.isEmpty()) {
                for (AdminUser adminUser : list) {
                    if (StringUtils.isNotBlank(adminUser.getMid())) {
                        midListTest.add(adminUser.getMid());
                    }
                }
            }

            MsgDetail detail = new MsgDetail();
            detail.setText("***此為發送訊息後通知管理群***");
            detail.setMsgType(MsgGenerator.MSG_TYPE_TEXT);

            details.add(0, detail);

            sendMsgToMidList(midListTest, details, msgSendMain.getMsgSendId());
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public void sendMsgToMidList(List<String> midList, List<MsgDetail> details, Long updateMsgId) throws Exception {
        SendingMsgService sendingMegService = ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class);
        List<MsgGenerator> msgGenerators = MsgGeneratorFactory.validateMessages(details);
        log.info("Mid List Size:" + midList.size());
        sendingMegService.sendToLineAsync(msgGenerators, details, midList, API_TYPE.BOT, updateMsgId);
    }
}
