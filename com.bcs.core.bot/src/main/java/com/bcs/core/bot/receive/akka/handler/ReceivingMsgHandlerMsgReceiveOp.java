package com.bcs.core.bot.receive.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.api.service.LineUserStatusService;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.record.service.CatchRecordOpAddReceive;
import com.bcs.core.record.service.CatchRecordOpBlockedReceive;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Update Line User Status
 *
 * @author ???, Alan
 */
@Slf4j
public class ReceivingMsgHandlerMsgReceiveOp extends UntypedActor {

    @Override
    @SuppressWarnings("unchecked")
    public void onReceive(Object message) {
        log.debug("-------Get Operation Save-------");

        if (message instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) message;
            MsgBotReceive receive = (MsgBotReceive) map.get("Content");
            receive.setReferenceId(Objects.toString(map.get("iMsgId")));

            final String MID = receive.getSourceId();
            log.info("MID: {}", MID);

            if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())) {
                ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpAddReceive.class).incrementCount();
            } else if (MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())) {
                ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpBlockedReceive.class).incrementCount();
            }


            final Date NOW = new Date();
            if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())) {
                newFriend(receive, MID, NOW);
            } else if (MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())) {
                blockFriend(receive, MID, NOW);
            }

            /* Save Record */
            try {
                ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(receive);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
            }
        }
        ReceivingMsgHandlerMaster.taskCount.addAndGet(-1L);
        ReceivingMsgHandlerMaster.updateDate = Calendar.getInstance().getTime();
        log.debug("-------Get Operation Save End-------");
    }

    /**
     * 封鎖好友
     * @param receive receive
     * @param mid mid
     * @param now now
     */
    private void blockFriend(MsgBotReceive receive, String mid, Date now) {
        LineUserStatusService lineUserStatusService = ApplicationContextProvider.getApplicationContext().getBean(LineUserStatusService.class);
        LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
        LineUser lineUser = lineUserService.findByMid(receive.getSourceId());
        if (lineUser != null) {
            Date date = lineUser.getCreateTime();
            lineUser.setCreateTime(date == null ? now : date);
            lineUser.setStatus(LineUser.STATUS_BLOCK);
            lineUser.setModifyTime(now);
            lineUserService.save(lineUser);

            lineUserService.saveLog(lineUser, lineUser.getMid(),
                    LineUser.STATUS_BINDED.equals(lineUser.getStatus())
                            ? LOG_TARGET_ACTION_TYPE.ACTION_Binded2Block
                            : LOG_TARGET_ACTION_TYPE.ACTION_Block,
                    lineUser.getMid());
        } else {
            LineUser user = new LineUser();
            user.setMid(mid);
            user.setModifyTime(now);
            user.setCreateTime(now);
            user.setStatus(LineUser.STATUS_BLOCK);
            user.setSoureType(receive.getSourceType());
            lineUserService.save(user);
            lineUserService.saveLog(user, user.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Block, user.getMid());
        }
        //call Richart api通知user已封鎖
        lineUserStatusService.callLineUserStatusAPI(mid, LineUser.STATUS_BLOCK, now.getTime());
    }

    /**
     * 新增好友
     * @param receive receive
     * @param mid mid
     * @param now now
     */
    private void newFriend(MsgBotReceive receive, String mid, Date now) {
        LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
        LineUser lineUser = lineUserService.findByMid(receive.getSourceId());
        if (lineUser != null) {
            Date date = lineUser.getCreateTime();
            lineUser.setCreateTime(date == null ? now : date);
            String isBind = lineUser.getIsBinded();
            lineUser.setStatus(StringUtils.isBlank(isBind) ? LineUser.STATUS_UNBIND : isBind);
            lineUser.setModifyTime(now);
            lineUserService.save(lineUser);
            lineUserService.saveLog(lineUser, lineUser.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Unbind, lineUser.getMid());
        } else {
            LineUser user = lineUserService.findByMidAndCreateUnbind(mid);
            lineUserService.saveLog(user, user.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Unbind, user.getMid());
        }
    }

    /**
     * Handle Msg ReceiveOp
     *
     * @param content content
     * @param channelId channelId
     * @param apiType apiType
     * @return iMsgId
     */
    public static Long handleMsgReceiveOp(MsgBotReceive content, String channelId, String apiType) {

        InteractiveService interactiveService = ApplicationContextProvider.getApplicationContext().getBean(InteractiveService.class);

        log.debug("channelId:" + channelId);
        log.debug("apiType:" + apiType);
        final String MID = content.getSourceId();
        log.debug("MID:" + MID);
        try {
            // 新增好友
            if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(content.getEventType())) {

                // 取得 關鍵字回應 設定
                Long iMsgId = interactiveService.getWelcomeResponse();
                log.debug("Get Welcome iMsgId:" + iMsgId);

                if (iMsgId != null) {
                    String replyToken = content.getReplyToken();
                    // 傳送 關鍵字回應
                    List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);
                    ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, channelId, MID, apiType, null);

                    // 記錄自動回應 iMsgId
                    return iMsgId;
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return -1L;
    }
}
