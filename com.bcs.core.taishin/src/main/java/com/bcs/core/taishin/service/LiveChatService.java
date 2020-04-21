package com.bcs.core.taishin.service;

import com.bcs.core.api.service.LineSwitchApiService;
import com.bcs.core.bot.db.entity.LiveChatReplyRecord;
import com.bcs.core.bot.db.service.LiveChatReplyRecordService;
import com.bcs.core.bot.receive.service.MessageProcessService;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.LiveChatReplyModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class LiveChatService {
    private static Logger logger = Logger.getLogger(LiveChatService.class);

    @Autowired
    private UserLiveChatService userLiveChatService;
    @Autowired
    private MessageProcessService messageProcessService;
    @Autowired
    private LiveChatReplyRecordService liveChatReplyRecordService;
    @Autowired
    private LineSwitchApiService lineSwitchApiService;

    public boolean updateUserLiveChatStatus(String UID, String status) {
        UserLiveChat userLiveChat = null;

        if (status.equals(UserLiveChat.IN_PROGRESS)) {// 找出狀態為 "WAITING" 的 User
            userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);
        } else if (status.equals(UserLiveChat.FINISH))    // 找出狀態為 "IN_PROGRESS" 的 User
            userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.IN_PROGRESS);
        else
            userLiveChat = userLiveChatService.findByUID(UID);

        if (userLiveChat == null)
            return false;

        /* 更改 User 的 status */
        userLiveChat.setStatus(status);

        userLiveChatService.save(userLiveChat);

        return true;
    }

    public void sendMsgToUser(LiveChatReplyModel receivedMessage, UserLiveChat userLiveChat) throws Exception {
        this.sendMsgToUser(receivedMessage, userLiveChat, CONFIG_STR.MANUAL_REPLY.toString());
    }

    public void sendMsgToUser(LiveChatReplyModel receivedMessage, UserLiveChat userLiveChat, String channelName) throws Exception {
        String UID = receivedMessage.getTo();
        String message = null;

        message = receivedMessage.getText();

        messageProcessService.pushTextMsgAsyncWithServiceCode(UID, message, channelName);

        this.saveLiveChatReplyRecord(receivedMessage, userLiveChat.getId(), false);
    }

    @Transactional
    public void switchToManualReplyChannel(UserLiveChat userLiveChat, String message) throws Exception {
        logger.info("[Switch] \"" + userLiveChat.getUID() + "\" switch to ManualReply Channel.");

        Date now = new Date();
        String UID = userLiveChat.getUID();

        userLiveChat.setStatus(UserLiveChat.IN_PROGRESS);
        userLiveChat.setModifyTime(now);

        userLiveChatService.save(userLiveChat);

        String manualReplyChannelName = CoreConfigReader.getString(CONFIG_STR.MANUAL_REPLY_CHANNEL_NAME.toString(), true);

        lineSwitchApiService.executeSwitch(CoreConfigReader.getString(manualReplyChannelName, "DestinationId", true), UID, "");    // 將使用者 switch 至真人客服的 channel
//		messageProcessService.pushTextMsgAsync(UID,message,CONFIG_STR.ManualReply.toString()); 20190125 轉真人接通文案移除
    }

    @Transactional
    public void switchToAutoReplyChannel(UserLiveChat userLiveChat, String message) throws Exception {
        logger.info("[Switch] \"" + userLiveChat.getUID() + "\" switch to AutoReply Channel.");

        String status = userLiveChat.getStatus();

        if (status.equals(UserLiveChat.IN_PROGRESS)) {
            String autoReplyChannelName = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY_CHANNEL_NAME.toString(), true);
            String UID = userLiveChat.getUID();
            Date now = new Date();

            userLiveChat.setStatus(UserLiveChat.FINISH);
            userLiveChat.setModifyTime(now);

            userLiveChatService.save(userLiveChat);

            lineSwitchApiService.executeSwitch(CoreConfigReader.getString(autoReplyChannelName, "DestinationId", true), UID, "");

            if (message != null)
                messageProcessService.pushTextMsgAsyncWithServiceCode(UID, message, CONFIG_STR.AUTO_REPLY.toString());
        }
    }

    @Transactional
    private void saveLiveChatReplyRecord(LiveChatReplyModel receivedMessage, Long userLiveChatId, Boolean isReplyComment) throws Exception {
        LiveChatReplyRecord record = new LiveChatReplyRecord();

        record.setUserLiveChatId(userLiveChatId);
        record.setUID(receivedMessage.getTo());
        record.setMsgType(receivedMessage.getMsgType());
        record.setMsgText(receivedMessage.getText());
        // record.setIsReplyComment(isReplyComment);
        record.setCreateTime(new Date());

        liveChatReplyRecordService.save(record);
    }
}