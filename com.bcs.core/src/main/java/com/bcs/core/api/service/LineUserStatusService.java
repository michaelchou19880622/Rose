package com.bcs.core.api.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class LineUserStatusService {

    /** Logger */
    private static Logger logger = Logger.getLogger(LineUserStatusService.class);

    public void callLineUserStatusAPI(String uid, String status, long time) {
        logger.debug("callLineUserStatusAPI");

        try {
            HttpClient httpClient = HttpClientUtil.generateClient();

            String uri = CoreConfigReader.getString(CONFIG_STR.TAISHIN_POST_URL_USER_STATUS);

            HttpPost request = new HttpPost(uri);
            request.addHeader("content-type", "application/json;charset=utf-8");

            logger.debug("URI : " + request.getURI());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("uid", uid);
            node.put("time", time);
            node.put("status", status);
            
            StringEntity params = new StringEntity(node.toString());
            
            request.setEntity(params);

            HttpResponse clientResponse = httpClient.execute(request);

            int httpStatus = clientResponse.getStatusLine().getStatusCode();
            logger.debug("clientResponse StatusCode : " + httpStatus);

            String result = "";
            if (clientResponse != null && clientResponse.getEntity() != null
                    && clientResponse.getEntity().getContent() != null) {

                result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
            }
            logger.debug("clientResponse result : " + result);
            
            if (httpStatus == 200) {
                JsonNode resultNode = mapper.readTree(result);
                String resultStr = resultNode.get("result").toString();
                System.out.println("resultStr=" + resultStr);
                if ("0".equals(resultStr)) {
                    logger.debug("更新失敗:" + resultNode.get("uid"));
                    SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.ACTION_RICHART_API, LOG_TARGET_ACTION_TYPE.ACTION_SendToRichartApiStatus, result, resultNode.get("uid").toString());
                }
            }

            request.releaseConnection();

        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            logger.error(error);
        }
    }
}
