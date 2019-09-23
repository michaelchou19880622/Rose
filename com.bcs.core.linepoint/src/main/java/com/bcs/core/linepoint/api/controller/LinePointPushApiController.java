package com.bcs.core.linepoint.api.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.api.model.LinePointTaskModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.scheduler.service.LinePointSimpleSchedulerService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.RestfulUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/api")
public class LinePointPushApiController {
    private static Logger logger = Logger.getLogger(LinePointPushApiController.class);

    @Autowired
    private LinePointPushAkkaService linePointPushAkkaService;
    @Autowired
    private LinePointDetailService linePointDetailService;
    @Autowired
    private LinePointMainService linePointMainService;
    @Autowired
    private LinePointSimpleSchedulerService linePointSimpleSchedulerService;


    @WebServiceLog
//	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/pushLinePoint", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
            /*@RequestBody List<String> uids,*/ @RequestParam Long eventId) throws IOException {
        try {
            LinePointMain linePointMain = linePointMainService.findOne(eventId);

            JSONArray uids = new JSONArray();
            JSONArray amounts = new JSONArray();
            for (int i = 1; i <= 10; i++) {
                uids.put("U58ffae876d497a488111d38a70b5aea0");
                amounts.put(i);
            }

            LinePointPushModel linePointPushModel = new LinePointPushModel();
            linePointPushModel.setEventId(eventId);
            linePointPushModel.setSource(LinePointPushModel.SOURCE_TYPE_BCS);
            linePointPushModel.setSendTimeType(LinePointPushModel.SEND_TIMING_TYPE_IMMEDIATE);
            linePointPushModel.setTriggerTime(new Date());
            linePointPushAkkaService.tell(linePointPushModel);
            return new ResponseEntity<>("", HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @WebServiceLog
//	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/task", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> linePointTask(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointTaskModel linePointTaskModel) {
        try {
            LinePointDetail linePointDetail = linePointTaskModel.getLinePointDetail();
            logger.info("-------------------- api linePoint task --------------------");

            logger.info("[LinePoint API] Request Body:" + linePointDetail);
            linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_API);
            linePointDetail.setTriggerTime(new Date());

            // ----------- validation --------------
            try {
                if (linePointDetail.getDepartment() == null) {
                    throw new IllegalArgumentException("Request Department is NULL");
                }
                if (linePointDetail.getServiceName() == null) {
                    throw new IllegalArgumentException("Request ServiceName is NULL");
                }
                if (linePointDetail.getPccCode() == null) {
                    throw new IllegalArgumentException("Request PccCode is NULL");
                }
                if (linePointDetail.getCampName() == null) {
                    throw new IllegalArgumentException("Request CampName is NULL");
                }
                if (linePointDetail.getUid() == null) {
                    throw new IllegalArgumentException("Request Uid is NULL");
                }
                if (linePointDetail.getOrderKey() == null) {
                    throw new IllegalArgumentException("Request OrderKey is NULL");
                }
                if (linePointDetail.getAmount() == null) {
                    throw new IllegalArgumentException("Request Amount is NULL");
                }

                if (linePointDetail.getDepartment().length() > 50) {
                    linePointDetail.setDepartment("NG");
                    throw new IllegalArgumentException("Request Department out of boundary");
                }
                if (linePointDetail.getServiceName().length() > 50) {
                    linePointDetail.setServiceName("NG");
                    throw new IllegalArgumentException("Request ServiceName out of boundary");
                }
                if (linePointDetail.getPccCode().length() > 50) {
                    linePointDetail.setPccCode("NG");
                    throw new IllegalArgumentException("Request PccCode out of boundary");
                }
                if (linePointDetail.getCampName().length() > 50) {
                    linePointDetail.setCampName("NG");
                    throw new IllegalArgumentException("Request CampName out of boundary");
                }
                if (linePointDetail.getUid().length() > 33) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Uid out of boundary");
                }
                if (linePointDetail.getCustid().length() > 10) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Custid out of boundary");
                }
                if (linePointDetail.getOrderKey().length() > 50) {
                    linePointDetail.setOrderKey("NG");
                    throw new IllegalArgumentException("Request OrderKey out of boundary");
                }
                if (linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > 2147483647L) {
                    linePointDetail.setAmount(0L);
                    throw new IllegalArgumentException("Request Amount out of boundary");
                }
            } catch (Exception e) {
                linePointDetail.setMessage(e.toString());
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);
                throw e;
            }

            String dateString = linePointTaskModel.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateString);
            // linePointTaskModel.getDate()
            linePointSimpleSchedulerService.createLinePointTask(linePointDetail, date);
            return new ResponseEntity<>("{}", HttpStatus.OK);
        } catch (Exception e) {
            logger.info("e:" + e.toString());
            if (e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
            else if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
//	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/issue", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointDetail linePointDetail) {
        try {
            logger.info("-------------------- api linePoint issue --------------------");

            // ----------- authorization --------------
            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
            } else {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization.split("key=").length != 2) {
                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
                }

                String token = authorization.split("key=")[1];
                String originalToken = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_ORIGINAL_TOKEN, true);

                if (!token.equals(originalToken)) {
                    return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
                }
            }

            // ----- set main information -----
            String title = linePointDetail.getCampName();
            LinePointMain linePointMain = linePointMainService.findByTitle(title);
            if (linePointMain == null) {
                linePointMain = new LinePointMain();
                linePointMain.setSendType(LinePointMain.SEND_TYPE_API);
                linePointMain.setModifyTime(new Date());
                linePointMain.setTitle(title);
                linePointMain.setTotalCount(0L);
                linePointMain.setTotalAmount(0L);
                linePointMain.setSuccessfulAmount(0L);
                linePointMain.setSuccessfulCount(0L);
                linePointMain.setFailedCount(0L);

                linePointMain.setSerialId("API_" + title);
                linePointMain.setModifyUser("API");
                linePointMain.setSerialId("API無此參數");
                linePointMain.setDepartmentFullName(linePointDetail.getDepartment());
                linePointMainService.save(linePointMain);
            }
            logger.info("linePointMain:" + linePointMain);

            // ----- set detail information -----
            linePointDetail.setLinePointMainId(linePointMain.getId());
            linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_API);
            linePointDetail.setTriggerTime(new Date());
            logger.info("linePointDetail:" + linePointDetail);

            // ----------- validation --------------
            try {
                if (linePointDetail.getDepartment() == null) {
                    throw new IllegalArgumentException("Request Department is NULL");
                }
                if (linePointDetail.getServiceName() == null) {
                    throw new IllegalArgumentException("Request ServiceName is NULL");
                }
                if (linePointDetail.getPccCode() == null) {
                    throw new IllegalArgumentException("Request PccCode is NULL");
                }
                if (linePointDetail.getCampName() == null) {
                    throw new IllegalArgumentException("Request CampName is NULL");
                }
                if (linePointDetail.getUid() == null) {
                    throw new IllegalArgumentException("Request Uid is NULL");
                }
                if (linePointDetail.getOrderKey() == null) {
                    throw new IllegalArgumentException("Request OrderKey is NULL");
                }
                if (linePointDetail.getAmount() == null) {
                    throw new IllegalArgumentException("Request Amount is NULL");
                }

                if (linePointDetail.getDepartment().length() > 50) {
                    linePointDetail.setDepartment("NG");
                    throw new IllegalArgumentException("Request Department out of boundary");
                }
                if (linePointDetail.getServiceName().length() > 50) {
                    linePointDetail.setServiceName("NG");
                    throw new IllegalArgumentException("Request ServiceName out of boundary");
                }
                if (linePointDetail.getPccCode().length() > 50) {
                    linePointDetail.setPccCode("NG");
                    throw new IllegalArgumentException("Request PccCode out of boundary");
                }
                if (linePointDetail.getCampName().length() > 50) {
                    linePointDetail.setCampName("NG");
                    throw new IllegalArgumentException("Request CampName out of boundary");
                }
                if (linePointDetail.getUid().length() > 33) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Uid out of boundary");
                }
                if (linePointDetail.getCustid().length() > 10) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Custid out of boundary");
                }
                if (linePointDetail.getOrderKey().length() > 50) {
                    linePointDetail.setOrderKey("NG");
                    throw new IllegalArgumentException("Request OrderKey out of boundary");
                }
                if (linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > Integer.MAX_VALUE) {
                    linePointDetail.setAmount(0L);
                    throw new IllegalArgumentException("Request Amount out of boundary");
                }
            } catch (Exception e) {
                linePointDetail.setMessage(e.toString());
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);
                throw e;
            }


            // ----------- execute --------------
            // initialize request header
            HttpHeaders headers = new HttpHeaders();
            String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);

            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            // initialize request body
            JSONObject requestBody = new JSONObject();
            // https://api.line.me/pointConnect/v1/issue
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true);
            // 10052
            String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true);
            requestBody.put("clientId", clientId);
            requestBody.put("amount", linePointDetail.getAmount());
            requestBody.put("memberId", linePointDetail.getUid());
            requestBody.put("orderKey", linePointDetail.getOrderKey());

            // applicationTime
            Long applicationTime = System.currentTimeMillis();
            requestBody.put("applicationTime", applicationTime);
            linePointDetail.setApplicationTime(applicationTime);

            // HttpEntity by header and body
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            JSONObject responseObject = null;
            try {
                responseObject = restfulUtil.execute();

                String Id = responseObject.getString("transactionId");
                Long Time = responseObject.getLong("transactionTime");
                String Type = responseObject.getString("transactionType");
                Integer Amount = responseObject.getInt("transactionAmount");
                Integer Balance = responseObject.getInt("balance");

                linePointDetail.setTranscationId(Id);
                linePointDetail.setTranscationTime(Time);
                linePointDetail.setTranscationType(Type);
                linePointDetail.setTransactionAmount(Amount);
                linePointDetail.setBalance(Balance);
                linePointDetail.setMessage("SUCCESS");
                linePointDetail.setStatus(LinePointDetail.STATUS_SUCCESS);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);

                linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() + Amount);
                linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() + 1L);
                linePointMainService.save(linePointMain);
            } catch (HttpClientErrorException e) {
                logger.info("[LinePointApi] Status code: " + e.getStatusCode());
                logger.info("[LinePointApi] Response body: " + e.getResponseBodyAsString());

                linePointDetail.setMessage(e.getResponseBodyAsString());
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);

                linePointMain.setFailedCount(linePointMain.getFailedCount() + 1L);
                linePointMainService.save(linePointMain);
                // e.getStatusCode()
                return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.OK);

            }
            return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
        } catch (IllegalArgumentException ie) {
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + ie.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.info("e:" + e.toString());
            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/linePoint/cancel", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> cancelLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointDetail linePointDetail) {
        try {
            logger.info("-------------------- api linePoint cancel --------------------");

            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
            } else {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization.split("key=").length != 2) {
                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
                }

                String token = authorization.split("key=")[1];
                String originalToken = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_ORIGINAL_TOKEN, true);

                if (!token.equals(originalToken)) {
                    return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
                }
            }

            logger.info("[LinePoint API] Request Body:" + linePointDetail);
            linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_CANCEL_API);
            linePointDetail.setTriggerTime(new Date());

            // ----------- validation --------------
            try {
                if (linePointDetail.getDepartment() == null) {
                    throw new IllegalArgumentException("Request Department is NULL");
                }
                if (linePointDetail.getServiceName() == null) {
                    throw new IllegalArgumentException("Request ServiceName is NULL");
                }
                if (linePointDetail.getPccCode() == null) {
                    throw new IllegalArgumentException("Request PccCode is NULL");
                }
                if (linePointDetail.getCampName() == null) {
                    throw new IllegalArgumentException("Request CampName is NULL");
                }
                if (linePointDetail.getUid() == null) {
                    throw new IllegalArgumentException("Request Uid is NULL");
                }
                if (linePointDetail.getOrderKey() == null) {
                    throw new IllegalArgumentException("Request OrderKey is NULL");
                }
                if (linePointDetail.getCancelTranscationId() == null) {
                    throw new IllegalArgumentException("Request CancelTranscationId is NULL");
                }

                if (linePointDetail.getDepartment().length() > 50) {
                    linePointDetail.setDepartment("NG");
                    throw new IllegalArgumentException("Request Department out of boundary");
                }
                if (linePointDetail.getServiceName().length() > 50) {
                    linePointDetail.setServiceName("NG");
                    throw new IllegalArgumentException("Request ServiceName out of boundary");
                }
                if (linePointDetail.getPccCode().length() > 50) {
                    linePointDetail.setPccCode("NG");
                    throw new IllegalArgumentException("Request PccCode out of boundary");
                }
                if (linePointDetail.getCampName().length() > 50) {
                    linePointDetail.setCampName("NG");
                    throw new IllegalArgumentException("Request CampName out of boundary");
                }
                if (linePointDetail.getUid().length() > 33) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Uid out of boundary");
                }
                if (linePointDetail.getCustid().length() > 10) {
                    linePointDetail.setUid("NG");
                    throw new IllegalArgumentException("Request Custid out of boundary");
                }
                if (linePointDetail.getOrderKey().length() > 50) {
                    linePointDetail.setOrderKey("NG");
                    throw new IllegalArgumentException("Request OrderKey out of boundary");
                }
                if (linePointDetail.getCancelTranscationId().length() > 15) {
                    linePointDetail.setCancelTranscationId("NG");
                    throw new IllegalArgumentException("Request CancelTranscationId out of boundary");
                }
                if (linePointDetail.getNote() != null) {
                    if (linePointDetail.getNote().length() > 50) {
                        linePointDetail.setNote("NG");
                        throw new IllegalArgumentException("Request Note out of boundary");
                    }
                }
                if (linePointDetail.getAmount() != null) {
                    if (linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > Integer.MAX_VALUE) {
                        linePointDetail.setAmount(0L);
                        throw new IllegalArgumentException("Request Amount out of boundary");
                    }
                }

            } catch (Exception e) {
                linePointDetail.setMessage(e.toString());
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);
                throw e;
            }

            // ---------------------------------------
            // initialize request header
            HttpHeaders headers = new HttpHeaders();
            String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            // initialize request body
            JSONObject requestBody = new JSONObject();
            // https://api.line.me/pointConnect/v1/issue
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_CANCEL_URL.toString(), true);
            url = url.replace("{transactionId}", linePointDetail.getCancelTranscationId());
            // 10052
            String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true);
            requestBody.put("clientId", clientId);
            requestBody.put("memberId", linePointDetail.getUid());
            requestBody.put("orderKey", linePointDetail.getOrderKey());
            if (linePointDetail.getAmount() != null) {
                requestBody.put("amount", linePointDetail.getAmount());
            }
            if (linePointDetail.getNote() != null) {
                requestBody.put("note", linePointDetail.getNote());
            }

            // HttpEntity by header and body
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            JSONObject responseObject = null;
            try {
                responseObject = restfulUtil.execute();

                String Id = responseObject.getString("transactionId");
                Long Time = responseObject.getLong("transactionTime");
                String Type = responseObject.getString("transactionType");
                Integer cancelledAmount = responseObject.getInt("cancelledAmount");
                Integer remainingAmount = responseObject.getInt("remainingAmount");
                Integer Balance = responseObject.getInt("balance");

                linePointDetail.setTranscationId(Id);
                linePointDetail.setTranscationTime(Time);
                linePointDetail.setTranscationType(Type);
                linePointDetail.setCancelledAmount(cancelledAmount);
                linePointDetail.setRemainingAmount(remainingAmount);
                // amount = remainingAmount
                linePointDetail.setAmount(new Long(remainingAmount));
                linePointDetail.setBalance(Balance);
                linePointDetail.setMessage("SUCCESS");
                linePointDetail.setStatus(LinePointDetail.STATUS_SUCCESS);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);

            } catch (HttpClientErrorException e) {
                logger.info("[LinePointApi] Status code: " + e.getStatusCode());
                logger.info("[LinePointApi]  Response body: " + e.getResponseBodyAsString());

                linePointDetail.setMessage(e.getResponseBodyAsString());
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setSendTime(new Date());
                linePointDetailService.save(linePointDetail);
                //e.getStatusCode()
                return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.OK);
            }
            return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);


        } catch (BadPaddingException be) {
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException ie) {
            logger.info("e:" + ie.toString());
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + ie.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
        } catch (IllegalBlockSizeException ie) {
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.info("e:" + e.toString());
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
