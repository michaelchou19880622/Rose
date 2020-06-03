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
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Random;


/**
 * The type Line bot api controller.
 *
 * @author ???
 * @author Alan
 */
@Slf4j
@Controller
@RequestMapping("/line")
public class LineBotApiController {

    private AkkaBotService akkaBotService;

    @Autowired
    public LineBotApiController(AkkaBotService akkaBotService) {
        this.akkaBotService = akkaBotService;
    }

    /**
     * Line bot api receiving.
     *
     * @param receivingMsg the receiving msg
     * @param channelId    the channel id
     * @param channelName  the channel name
     * @param request      the request
     * @param response     the response
     */
    @WebServiceLog
    @PostMapping(value = "/bot/api/receiving/{channelId}/{channelName}", consumes = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public void lineBotApiReceiving(@RequestBody String receivingMsg, @PathVariable String channelId, @PathVariable String channelName, HttpServletRequest request, HttpServletResponse response) {
        Date start = new Date();
        try {
        	String tid = "" + new Random().nextInt(1000000000);
            log.info("Received a LineBotApi Message, keywordTID={} msg={}", tid, DataUtils.toPrettyJsonUseJackson(receivingMsg));
            String channelSignature = request.getHeader(LINE_HEADER.HEADER_BOT_ChannelSignature.toString());
            if (signatureValidIsFail(receivingMsg, channelName, response, start, channelSignature)) {
            	responseProcess(receivingMsg, response, start, "Signature Valid Fail!!", 470, "470");
                return;
            }
            responseProcess(receivingMsg, response, start, "LineBotApi Receiving finish!!", 200, "200");
            akkaBotService.receivingMsgs(new ReceivedModelOriginal(receivingMsg, channelId, channelName, channelSignature, API_TYPE.BOT, tid));
            return;
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        responseProcess(receivingMsg, response, start, "LineBotApi Receiving fail!!", 470, "470");
    }

    private void responseProcess(@RequestBody String receivingMsg, HttpServletResponse response, Date start, String s, int i, String s2) {
        log.info(s);
        response.setStatus(i);
        SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBotApi, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, i, receivingMsg, s2);
    }

    private boolean signatureValidIsFail(@RequestBody String receivingMsg, @PathVariable String channelName, HttpServletResponse response, Date start, String channelSignature) {
        if (CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_CHECK_SIGNATURE)) {
            boolean validIsFail = !SignatureValidationHelper.signatureValidation(receivingMsg, channelName, channelSignature);
            if (validIsFail) {
                return true;
            }
        }
        return false;
    }
}
