package com.bcs.core.taishin.circle.pnp.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.helper.SignatureValidationHelper;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.akka.model.AsyncSendingClusterModel;
import com.bcs.core.taishin.circle.pnp.plugin.PostLineResponse;
import com.bcs.core.taishin.circle.pnp.plugin.RestfulUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Date;

@Slf4j(topic = "PnpRecorder")
public class CircleBcsApiClusterService {


    public static PostLineResponse clusterApiSend(AsyncSendingClusterModel model) throws Exception {

        String sendMsg = ObjectUtil.objectToJsonStr(model);

        String encode = SignatureValidationHelper.encode(sendMsg, CONFIG_STR.DEFAULT.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(LINE_HEADER.HEADER_ChannelSignature.toString(), encode);

        String url = CoreConfigReader.getString(CONFIG_STR.BCS_API_CLUSTER_SEND.toString());
        if (StringUtils.isNotBlank(url)) {
            return clusterApiSend(model, url, headers);
        } else {
            throw new Exception("Url Error");
        }
    }

    public static PostLineResponse clusterApiSend(AsyncSendingClusterModel model, String url, HttpHeaders headers) throws Exception {

        Date start = new Date();
        int status = 0;

        String postMsg = ObjectUtil.objectToJsonStr(model);

        try {
            HttpEntity<String> httpEntity = new HttpEntity<String>(postMsg, headers);

            // print requestPost
            log.debug("postMsg : " + postMsg);

            // execute Call
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            restfulUtil.setUseProxy(CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));

            log.info(">>> Use proxy: " + CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));

            JSONObject responseObject = restfulUtil.execute();
            log.info("{}", responseObject);
            log.debug("clientResponse StatusCode : " + status);
            status = Integer.parseInt(restfulUtil.getStatusCode());
            String result = responseObject != null ? responseObject.toString() : "";
            log.debug("clientResponse result : " + result);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send, start, status, postMsg, status + "");
            return new PostLineResponse(status, result);
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            log.error(error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineBCApiCluster, LOG_TARGET_ACTION_TYPE.ACTION_Send_Error, start, status, postMsg, status + "");
            throw e;
        }
    }
}
