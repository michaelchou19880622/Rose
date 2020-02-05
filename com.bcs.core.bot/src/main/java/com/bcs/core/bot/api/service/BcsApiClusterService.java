package com.bcs.core.bot.api.service;

import com.bcs.core.api.service.model.PostLineResponse;
import com.bcs.core.bot.send.akka.model.AsyncSendingClusterModel;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.helper.SignatureValidationHelper;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.utils.RestfulUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Date;

/**
 * @author ???
 */
@Slf4j
public class BcsApiClusterService {

    public static PostLineResponse clusterApiSend(AsyncSendingClusterModel model) throws Exception {
        String sendMsg = ObjectUtil.objectToJsonStr(model);
        String encode = SignatureValidationHelper.encode(sendMsg, CONFIG_STR.DEFAULT.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(LINE_HEADER.HEADER_ChannelSignature.toString(), encode);

        String url = CoreConfigReader.getString(CONFIG_STR.BCS_API_CLUSTER_SEND.toString());
        if (StringUtils.isBlank(url)) {
            throw new Exception("Url Error");
        }
        return clusterApiSend(model, url, headers);
    }

    public static PostLineResponse clusterApiSend(AsyncSendingClusterModel model, String url, HttpHeaders headers) throws Exception {
        log.info("ClusterApiSend Start!!");
        Date start = new Date();
        int status = 0;

        String postMsg = ObjectUtil.objectToJsonStr(model);

        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(postMsg, headers);

            boolean useProxy = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString());
            log.info("Use Proxy: {}", useProxy);
            log.info("Post Msg : {}", postMsg);

            RestfulUtil restfulUtil = new RestfulUtil(
                    HttpMethod.POST,
                    url,
                    httpEntity,
                    useProxy
            );

            JSONObject responseObject = restfulUtil.execute();
            if (NumberUtils.isParsable(restfulUtil.getStatusCode())) {
                status = NumberUtils.toInt(restfulUtil.getStatusCode());
            }
            log.debug("Status Code: {}", status);
            String result = responseObject != null ? responseObject.toString() : "";
            log.debug("After Execute Response Object: {}", result);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send, start, status, postMsg, status + "");
            return new PostLineResponse(status, result);
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            log.error("Exception", error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send_Error, start, status, postMsg, status + "");
            throw e;
        }
    }
}
