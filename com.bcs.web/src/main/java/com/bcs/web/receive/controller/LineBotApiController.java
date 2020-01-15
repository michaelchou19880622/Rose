package com.bcs.web.receive.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.bot.akka.service.AkkaBotService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.helper.SignatureValidationHelper;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


@Controller
@RequestMapping("/line")
public class LineBotApiController {

    @Autowired
    private AkkaBotService akkaBotService;
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LineBotApiController.class);

    /**
     * @param receivingMsg
     * @param ChannelId
     * @param ChannelName
     * @param request
     * @param response
     */
    @WebServiceLog
    @PostMapping(value = "/bot/api/receiving/{ChannelId}/{ChannelName}", consumes = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public void lineBotApiReceiving(@RequestBody String receivingMsg, @PathVariable String ChannelId, @PathVariable String ChannelName, HttpServletRequest request, HttpServletResponse response) {
        logger.info("-------lineBotApiReceiving-------");
        Date start = new Date();
        logger.info("Receiving Message : " + receivingMsg);

        try {
            String channelSignature = request.getHeader(LINE_HEADER.HEADER_BOT_ChannelSignature.toString());

            if (CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_CHECK_SIGNATURE.toString())) {
                boolean validate = SignatureValidationHelper.signatureValidation(receivingMsg, ChannelName, channelSignature);
                if (!validate) {
                    logger.info("Signature Valid Fail!!");
                    response.setStatus(470);
                    SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBotApi, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 470, receivingMsg, "470");
                    return;
                }
            }

            ReceivedModelOriginal msgs = new ReceivedModelOriginal(receivingMsg, ChannelId, ChannelName, channelSignature, API_TYPE.BOT);

            akkaBotService.receivingMsgs(msgs);
            logger.info("-------lineBotApiReceiving Success-------");
            response.setStatus(200);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBotApi, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 200, receivingMsg, "200");
            return;
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
        logger.info("-------lineBotApiReceiving Fail-------");
        response.setStatus(470);
        SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBotApi, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 470, receivingMsg, "470");
        return;
    }
}
