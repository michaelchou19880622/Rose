package com.bcs.core.api.service;

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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LineUserStatusService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LineUserStatusService.class);

    public void callLineUserStatusAPI(String uid, String status, long time) {
        logger.info("callLineUserStatusAPI");

        try (CloseableHttpClient httpClient = HttpClientUtil.getSingleInstance()) {

            String uri = CoreConfigReader.getString(CONFIG_STR.TAISHIN_POST_URL_USER_STATUS);

            HttpPost request = new HttpPost(uri);
            request.addHeader("content-type", "application/json;charset=utf-8");

            logger.info("URI : " + request.getURI());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("uid", uid);
            node.put("time", time);
            node.put("status", status);

            StringEntity params = new StringEntity(node.toString());

            request.setEntity(params);

            HttpResponse clientResponse = httpClient.execute(request);

            int httpStatus = clientResponse.getStatusLine().getStatusCode();
            logger.info("clientResponse StatusCode : " + httpStatus);

            String result = "";
            if (clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null) {
                result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
            }
            logger.info("clientResponse result : " + result);

            if (httpStatus == HttpStatus.OK.value()) {
                JsonNode resultNode = mapper.readTree(result);
                String resultStr = resultNode.get("result").toString();
                logger.info("resultStr=" + resultStr);
                if ("0".equals(resultStr)) {
                    logger.info("更新失敗:" + resultNode.get("uid"));
                    SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.ACTION_TAISHIN_API, LOG_TARGET_ACTION_TYPE.ACTION_SendToTaishinApiStatus, result, resultNode.get("uid").toString());
                }
            }

            request.releaseConnection();

        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            logger.error(error);
        }
    }
}
