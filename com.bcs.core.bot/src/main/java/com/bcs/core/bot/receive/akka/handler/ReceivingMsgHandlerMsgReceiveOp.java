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
    public void onReceive(Object message) {
        log.info("-------Get Operation Save-------");
        try {
            if (message instanceof Map) {
                mapProcess(message);
            }
            ReceivingMsgHandlerMaster.taskCount.addAndGet(-1L);
            ReceivingMsgHandlerMaster.updateDate = Calendar.getInstance().getTime();
            log.info("-------Get Operation Save End-------");
        } catch (Exception e) {
        	log.info("An exception detected dering processing a message");
            log.error("Exception", e);
        }
    }

    /**
     * 標準綁定及封鎖處理程序(經由LineChannel)
     * @param message message map
     */
    @SuppressWarnings("unchecked")
    private void mapProcess(Object message) throws Exception {
        Map<String, Object> map = (Map<String, Object>)message;
        MsgBotReceive receive = (MsgBotReceive) map.get("Content");
        receive.setReferenceId(Objects.toString(map.get("iMsgId")));

        final String mid = receive.getSourceId();
        log.info("MID: {}", mid);

        /* Increment Count */
        if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())) {
            ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpAddReceive.class).incrementCount();
        } else if (MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())) {
            ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpBlockedReceive.class).incrementCount();
        }

        /* Update User or new user */
        log.info("Event type is {}", receive.getEventType());
        final Date now = new Date();
        if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())) {
            newFriend(receive, mid, now);
        } else if (MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())) {
            blockFriend(receive, mid, now);
        }

        /* Save Record */
        try {
            ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(receive);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 封鎖好友
     * @param receive receive
     * @param mid mid
     * @param now now
     */
    private void blockFriend(MsgBotReceive receive, String mid, Date now) throws Exception {
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
        
        //call Taishin api通知user已封鎖
        lineUserStatusService.callLineUserStatusAPI(mid, LineUser.STATUS_BLOCK, now.getTime());
    }

    /**
     * 新增好友
     * @param receive receive
     * @param mid mid
     * @param now now
     */
    private void newFriend(MsgBotReceive receive, String mid, Date now) throws Exception {
        log.info("---------- newFriend ----------");
        LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
        LineUser lineUser = lineUserService.findByMid(receive.getSourceId());
        log.info("lineUser = {}", lineUser);
        
        if (lineUser != null) {
        	
            Date date = lineUser.getCreateTime();
            lineUser.setCreateTime(date == null ? now : date);
            
            String status = lineUser.getStatus();
            log.info("status = {}", status);
            
            String isBinded = lineUser.getIsBinded();
            log.info("isBinded = {}", isBinded);
            
            if (status.equals(LineUser.STATUS_SYS_ADD)) {
            	// 如果 STATUS 狀態為 SYSADD，則更新為 UNBIND
            	status = LineUser.STATUS_UNBIND;
            } else {
            	// 如果 STATUS 狀態不為 SYSADD，則檢查 ISBINDED 狀態是否為 UNBIND，若是，則更新為 UNBIND；否則更新為當前的 ISBINDED 狀態。
            	status = (StringUtils.isBlank(isBinded) ? LineUser.STATUS_UNBIND : isBinded);
			}

            log.info("final status = {}", status);
            
            lineUser.setStatus(status);
            lineUser.setModifyTime(now);
            log.info("[ LineUser UPDATE ] lineUser = {}", lineUser);
            
            lineUserService.save(lineUser);
        } else {
        	lineUser = lineUserService.findByMidAndCreateUnbind(mid);
            log.info("[ LineUser CREATE ] lineUser = {}", lineUser);
        }
        
        lineUserService.saveLog(lineUser, lineUser.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Unbind, lineUser.getMid());
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
        final String mid = content.getSourceId();
        log.info("MID:" + mid);
        try {
        	
            // 新增好友
            if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(content.getEventType())) {

                // 取得 關鍵字回應 設定
                Long iMsgId = interactiveService.getWelcomeResponse();
                log.info("Get Welcome iMsgId:" + iMsgId);

                if (iMsgId != null) {
                    String replyToken = content.getReplyToken();
                    // 傳送 關鍵字回應
                    List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);
                    ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, null);
                    return iMsgId;
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return -1L;
    }
}
