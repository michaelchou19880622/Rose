package com.bcs.core.api.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.jcodec.common.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LineWebLoginApiService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LineWebLoginApiService.class);

    public ObjectNode callRetrievingAPI(String client_id, String client_secret, String code, String redirect_uri) throws Exception {
        Date start = new Date();

        log.info("start = ", start);
        log.info("client_id = ", client_id);
        log.info("client_secret = ", client_secret);
        log.info("code = ", code);
        log.info("redirect_uri = ", redirect_uri);
        
        return this.callRetrievingAPI(start, client_id, client_secret, code, redirect_uri, 0);
    }

    public ObjectNode callRetrievingAPI(Date start, String client_id, String client_secret, String code, String redirect_uri, int retryCount) throws Exception {
        logger.info("callRetrievingAPI");

        int status = 0;
        try (CloseableHttpClient httpClient = HttpClientUtil.getSingleInstance()) {

            List<String> list = new ArrayList<>();
            list.add("grant_type=authorization_code");
            list.add("client_id=" + client_id);
            list.add("client_secret=" + client_secret);
            list.add("code=" + code);
            list.add("redirect_uri=" + URLEncoder.encode(redirect_uri, "UTF-8"));
            log.info("list = ", list);
            
            String postMsg = StringUtils.join(list.toArray(), "&");
            log.info("postMsg = ", postMsg);

            StringEntity entity = new StringEntity(postMsg, "UTF-8");
            entity.setContentType("application/x-www-form-urlencoded");

            // init Request
            HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_ACCESSTOKEN_V2_1));
            logger.info("URI : " + requestPost.getURI());
            requestPost.setEntity(entity);

            // print requestPost
            logger.info("postMsg : " + postMsg);

            // execute Call
            HttpResponse clientResponse = httpClient.execute(requestPost);

            status = clientResponse.getStatusLine().getStatusCode();
            logger.debug("clientResponse StatusCode : " + status);

            String result = "";
            if (clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null) {

                result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
            }
            logger.info("clientResponse result : " + result);

            requestPost.releaseConnection();

            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ValidateLoginApi, start, status, postMsg, status + "");
            return (ObjectNode) (new ObjectMapper()).readTree(result);
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            logger.error(error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ValidateLoginApi, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ValidateLoginApi_Error, start, status, error, status + "");
            if (retryCount < 5) {
                return this.callRetrievingAPI(start, client_id, client_secret, code, redirect_uri, retryCount + 1);
            } else {
                throw e;
            }
        }
    }
}
