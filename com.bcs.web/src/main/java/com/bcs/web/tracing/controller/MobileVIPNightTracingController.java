package com.bcs.web.tracing.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bcs.core.aspect.annotation.WebServiceLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.record.akke.model.MsgApiSendRecordModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LineLoginUtil;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Controller
@RequestMapping("/campaign")
public class MobileVIPNightTracingController extends BCSBaseController {

    @Autowired
    private SendingMsgService sendingMsgService;
    @Autowired
    private ShareCampaignService shareCampaignService;
    @Autowired
    private LineUserService lineUserService;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(MobileVIPNightTracingController.class);

    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/VIPNight/auth")
    public void vipNightAuth(HttpServletRequest request, HttpServletResponse response, Model model)
            throws Exception {
        logger.info("vipNightAuth");

        try {
            String code = request.getParameter("code");
            logger.info("vipNightAuth code:" + code);

            String state = request.getParameter("state");
            logger.info("vipNightAuth state:" + state);

            String error = request.getParameter("error");
            logger.info("vipNightAuth error:" + error);

            String errorCode = request.getParameter("errorCode");
            logger.info("vipNightAuth errorCode:" + errorCode);

            String errorMessage = request.getParameter("errorMessage");
            logger.info("vipNightAuth errorMessage:" + errorMessage);

            if (StringUtils.isBlank(state) || !"20181020".equals(state)) {
                throw new Exception("TracingId Error:" + state);
            }
            
            String campaignId = CoreConfigReader.getString(CONFIG_STR.VIP_NIGHT_MGM_ID, true);

            if(StringUtils.isNotBlank(campaignId)) {
                
                ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
                
                // 活動是否存在
                if(shareCampaign != null && ShareCampaign.STATUS_ACTIVE.equals(shareCampaign.getStatus())) {

                    // 活動是否過期
                    Date now = new Date();
                    if(now.after(shareCampaign.getStartTime()) && now.before(shareCampaign.getEndTime())) {
                        
                        // 取得UID、好友狀態
                        Map<String, String> resultMap = LineLoginUtil.callRetrievingAPI(code, UriHelper.getVIPNightAuth(), state);
                        String uid = resultMap.get("UID");
                        Boolean friendFlag = Boolean.valueOf(resultMap.get("friendFlag"));
                        
                        if(StringUtils.isNotBlank(uid)) {
                            lineUserService.findByMidAndCreateUnbind(uid);
                        }
                        
                        if(friendFlag){ // 好友
                            sendVIPMsg(campaignId, uid);
                        }

                        response.sendRedirect(CoreConfigReader.getString(CONFIG_STR.ADD_LINE_FRIEND_LINK, true));
                        return; 
                    }
                } 
            }

            String linkUrl = UriHelper.bcsMPage;
            response.sendRedirect(linkUrl);
            return;
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            String linkUrl = UriHelper.bcsMPage;
            response.sendRedirect(linkUrl);
            return;
        }
    }
    
    private void sendVIPMsg(final String campaignId, final String uid) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Message> messages = new ArrayList<>();
                
                String msg = CoreConfigReader.getString(CONFIG_STR.VIP_NIGHT_MGM_MSG, true);
                if(StringUtils.isNotBlank(msg)) {
                    TextMessage vipMgmMsg = new TextMessage(msg.replace("\\n", "\r\n") + "\n" + UriHelper.getMgmTracingUrl() + campaignId);
                    messages.add(vipMgmMsg);
                }
                
                MsgApiSendRecordModel msgApiSendRecordModel = new MsgApiSendRecordModel();
                
                MsgApiSendRecord msgApiSendRecord = new MsgApiSendRecord();
                
                Date now = new Date();
                
                msgApiSendRecord.setSendApiId(UUID.randomUUID().toString());
                msgApiSendRecord.setMid(uid);
                msgApiSendRecord.setMsgSource(campaignId);
                msgApiSendRecord.setReceiveTime(now);
                msgApiSendRecord.setMsgName("VIP_NIGHT_MGM");
                msgApiSendRecord.setSendBody(messages);
                msgApiSendRecord.setSendTime(now);
                msgApiSendRecord.setSendType(MsgMain.SENDING_MSG_TYPE_IMMEDIATE);
                
                msgApiSendRecordModel.setMessage(messages);
                msgApiSendRecordModel.setMsgApiSendRecord(msgApiSendRecord);
                
                try {
                    sendingMsgService.sendApiMessage(msgApiSendRecordModel, CONFIG_STR.Default.toString(), API_TYPE.BOT.toString(), 0); 
                }catch(Exception e) {}
            }
        });
        
        t.start();
    }
}
