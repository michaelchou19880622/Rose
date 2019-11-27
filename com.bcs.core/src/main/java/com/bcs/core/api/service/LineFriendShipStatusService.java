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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LineFriendShipStatusService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LineFriendShipStatusService.class);

    public ObjectNode getFriendShipStatusService(String access_token) throws Exception {
        return this.getFriendShipStatusService(new Date(), access_token, 0);
    }

    public ObjectNode getFriendShipStatusService(Date start, String access_token, int retryCount) throws Exception {
        logger.info("getFriendShipStatusService");
        int status = 0;

        try (CloseableHttpClient httpClient = HttpClientUtil.getSingleInstance()) {

            String uri = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_FRIENDSHIP_STATUS);

            HttpGet requestGet = new HttpGet(uri);
            logger.info("URI : " + requestGet.getURI());

            requestGet.addHeader("Authorization", "Bearer " + access_token);
            logger.debug("Authorization : Bearer " + access_token);

            // execute Call
            HttpResponse clientResponse = httpClient.execute(requestGet);

            status = clientResponse.getStatusLine().getStatusCode();
            logger.info("clientResponse StatusCode : " + status);

            String result = "";
            if (clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null) {

                result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
            }
            logger.debug("clientResponse result : " + result);

            requestGet.releaseConnection();

            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_FriendshipApi, start, status, result, status + "");
            return (ObjectNode) (new ObjectMapper()).readTree(result);
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            logger.error(error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_FriendshipApi, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_FriendshipApi_Error, start, status, error, status + "");
            if (retryCount < 5) {
                return this.getFriendShipStatusService(start, access_token, retryCount + 1);
            } else {
                throw e;
            }
        }
    }
}
