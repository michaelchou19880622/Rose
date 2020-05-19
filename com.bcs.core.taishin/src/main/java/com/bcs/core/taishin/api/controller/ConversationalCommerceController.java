package com.bcs.core.taishin.api.controller;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
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
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.api.service.LineProfileService;
import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.bot.db.entity.BotReplyRecord;
import com.bcs.core.bot.db.service.BotReplyRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.smartrobot.service.SwitchIconService;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.RestfulUtil;



import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api")
public class ConversationalCommerceController {

    @Autowired
    private SwitchIconService switchIconService;
    
    @Autowired
    private LineProfileService lineProfileService;
    
    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/pushMessage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> pushMessage(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBodyString) {
        log.info("[pushMessage] Requestbody: " + requestBodyString);

        try {
            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"missing headers\"}", HttpStatus.BAD_REQUEST);
            }

            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization.split("Basic ").length != 2) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid authorization format\"}", HttpStatus.UNAUTHORIZED);
            }

            String token = authorization.split("Basic ")[1];
            String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
            String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
            String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);

            if (!CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv).equals(originalToken)) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            }

            JSONObject requestBody = new JSONObject(requestBodyString);
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
            String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);

            /* 設定 request headers */
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);

            switchIconService.appendSender(CONFIG_STR.AUTO_REPLY.toString(), requestBody);

            /* 將 headers 跟 body 塞進 HttpEntity 中 */
            HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

            restfulUtil.execute();

            return new ResponseEntity<>("{\"error\": \"false\", \"message\": \"success\"}", HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            } else if (e instanceof HttpClientErrorException) {
                String responseMessage = ((HttpClientErrorException) e).getResponseBodyAsString();
                HttpStatus responseStatusCode = ((HttpClientErrorException) e).getStatusCode();

                if (responseMessage.contains("{\"message\"")) {
                    JSONObject responseMessageObject = new JSONObject(responseMessage);
                    String message = responseMessageObject.getString("message");

                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + message + "\"}", HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", responseStatusCode);
                }
            } else if (e instanceof JSONException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"The request body has 1 error(s)\"}", HttpStatus.BAD_REQUEST);
            } else {
                String errorMsg = e.getMessage();

                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + errorMsg + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/replyMessageWithReplyToken", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> replyMessageWithReplyToken(HttpServletRequest request, HttpServletResponse response, 
								    		@RequestBody String requestBodyString) {
        try {
        	log.info("[replyMessageWithReplyToken] Requestbody: " + DataUtils.toPrettyJsonUseJackson(requestBodyString));
        	
            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"missing headers\"}", HttpStatus.BAD_REQUEST);
            }

            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization.split("Basic ").length != 2) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid authorization format\"}", HttpStatus.UNAUTHORIZED);
            }

            String token = authorization.split("Basic ")[1];
            log.info("token = {}", token);
            
            String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
            log.info("secret = {}", secret);
            
            String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
            log.info("iv = {}", iv);
            
            String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);
            log.info("originalToken = {}", originalToken);
            
            String decrytedString = CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv);
            log.info("decrytedString = {}", decrytedString);

            if (!decrytedString.equals(originalToken)) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            }

            String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
            
            /* 設定 request headers */
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
    		log.info("headers = {}", headers);
            
            JSONObject requestBody = new JSONObject(requestBodyString);
            log.info("requestBody = {}", requestBody.toString());

            switchIconService.appendSender(CONFIG_STR.AUTO_REPLY.toString(), requestBody);
            log.info("requestBody after append Sender = {}", requestBody.toString());
            
            /* 解析 replyToken */
            String replyToken = requestBody.getString("replyToken");
            log.info("replyToken = {}", replyToken);

            /* 檢查 BOT_REPLY_RECORD 是否有此 replyToken 對應的紀錄? */
            BotReplyRecordService botReplyRecordService = ApplicationContextProvider.getApplicationContext().getBean(BotReplyRecordService.class);
            BotReplyRecord botReplyRecord = botReplyRecordService.findByReplyToken(replyToken);
            log.info("botReplyRecord = {}", botReplyRecord);
            
            if (StringUtils.isBlank(replyToken)) {
            	return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid reply token\"}", HttpStatus.UNAUTHORIZED);
            } else if (botReplyRecord == null) {
            	return new ResponseEntity<>(String.format("{\"error\": \"true\", \"message\": \"can not find the bot reply record with this reply token : %s\"}", replyToken), HttpStatus.UNAUTHORIZED);
			}
            
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_REPLY_URL.toString());

            /* 將 headers 跟 body 塞進 HttpEntity 中 */
            HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

            JSONObject jsonObjectResult = restfulUtil.execute();
            log.info("jsonObjectResult = {}", jsonObjectResult);

            return new ResponseEntity<>("{\"error\": \"false\", \"message\": \"success\"}", HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            } else if (e instanceof HttpClientErrorException) {
                String responseMessage = ((HttpClientErrorException) e).getResponseBodyAsString();
                HttpStatus responseStatusCode = ((HttpClientErrorException) e).getStatusCode();

                if (responseMessage.contains("{\"message\"")) {
                    JSONObject responseMessageObject = new JSONObject(responseMessage);
                    String message = responseMessageObject.getString("message");

                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + message + "\"}", HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", responseStatusCode);
                }
            } else if (e instanceof JSONException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"The request body has 1 error(s)\"}", HttpStatus.BAD_REQUEST);
            } else {
                String errorMsg = e.getMessage();

                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + errorMsg + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/getNickname", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getNickname(HttpServletRequest request, HttpServletResponse response, @RequestParam String UID) {
        try {
            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"missing headers\"}", HttpStatus.BAD_REQUEST);
            }

            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization.split("Basic ").length != 2) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid authorization format\"}", HttpStatus.UNAUTHORIZED);
            }

            String token = authorization.split("Basic ")[1];
            String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
            String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
            String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);

            if (!CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv).equals(originalToken)) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            }

            return new ResponseEntity<>("{\"error\": \"false\", \"UID\": \"" + UID + "\", \"nickname\": \"" + lineProfileService.getUserNickName(UID) + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            else if (e instanceof HttpClientErrorException) {
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid UID.\"}", HttpStatus.BAD_REQUEST);
            } else
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}