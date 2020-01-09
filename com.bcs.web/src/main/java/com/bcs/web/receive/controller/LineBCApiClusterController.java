package com.bcs.web.receive.controller;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.bot.akka.service.AkkaBotService;
import com.bcs.core.bot.send.akka.model.AsyncSendingClusterModel;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.helper.SignatureValidationHelper;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/line")
public class LineBCApiClusterController {

    @Autowired
    private AkkaBotService akkaBotService;
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LineBCApiClusterController.class);

    @RequestMapping(method = RequestMethod.POST, value = "/bc/api/cluster/send/{ChannelId}",
            consumes = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public void lineBCApiClusterSend(@RequestBody AsyncSendingClusterModel modelInput, @PathVariable String ChannelId, HttpServletRequest request, HttpServletResponse response) {
        logger.info("-------lineBCApiClusterSend-------");
        Date start = new Date();
        logger.debug("modelInput:" + modelInput);

        String sendMsg = ObjectUtil.objectToJsonStr(modelInput);

        try {
            String channelSignature = request.getHeader(LINE_HEADER.HEADER_ChannelSignature.toString());

            boolean validate = SignatureValidationHelper.signatureValidation(sendMsg, ChannelId, channelSignature);
            if (!validate) {
                logger.info("Signature Valid Fail!!");
                response.setStatus(470);
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 470, sendMsg, "470");
                return;
            }

            List<MsgGenerator> msgGenerators = MsgGeneratorFactory.validateMessages(modelInput.getMsgDetailList());
            AsyncSendingModel model = new AsyncSendingModel(modelInput.getChannelId(), msgGenerators, modelInput.getMidList(), API_TYPE.BC, modelInput.getUpdateMsgId());

            akkaBotService.sendingMsgs(model);
            logger.info("-------lineBCApiClusterSend Success-------");
            response.setStatus(200);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 200, sendMsg, "200");
            return;
        } catch (Throwable e) {
            logger.error(ErrorRecord.recordError(e));
        }
        logger.info("-------lineBCApiClusterSend Fail-------");
        response.setStatus(470);
        SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Receive, start, 470, sendMsg, "470");
        return;
    }
}
