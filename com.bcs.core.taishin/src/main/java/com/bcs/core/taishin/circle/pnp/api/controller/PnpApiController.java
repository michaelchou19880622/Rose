package com.bcs.core.taishin.circle.pnp.api.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.api.model.PnpApiModel;
import com.bcs.core.utils.RestfulUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.web.client.HttpClientErrorException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j(topic = "PnpRecorder")
@Controller
@RequestMapping("/api")
public class PnpApiController {

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/pnp/push",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody PnpApiModel pnpApiModel) {
        try {
            log.info("-------------------- pnp push api --------------------");
            log.info("pnpApiModel:" + pnpApiModel);
            String phoneHash = phoneToSHA256(pnpApiModel.getPhone());

            // initialize request header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

            String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
            headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);

            String rawDeliveryTag = "PNP_DELIVERY;;Ming;;1024;;1025;;" + phoneHash;
            String deliveryTag = String.format("%1$-" + 64 + "s", rawDeliveryTag); // 64 <= tag length <= 100，不夠則補空格
            headers.set("X-Line-Delivery-Tag", deliveryTag);

            // combine line text message
            JSONArray messageArray = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("type", "text");
            message.put("text", pnpApiModel.getMessage());
            messageArray.put(message);

            // initialize request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("to", phoneHash);
            requestBody.put("messages", messageArray);


            // set httpEntity by header and body
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED.toString());
            HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            JSONObject responseObject = null;

            try {
                responseObject = restfulUtil.execute();
                log.info("responseObject:" + responseObject.toString());
            } catch (HttpClientErrorException e) {
                log.info("[PnpApi] Status code: " + e.getStatusCode());
                log.info("[PnpApi]  Response body: " + e.getResponseBodyAsString());
                return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
            }
            return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.info("[PnpApi] Error Information:" + e.toString());
            if (e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
            else if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String phoneToSHA256(String phone) throws Exception {
        // Transform to E.164 Form
        if ("0".equals(phone.substring(0, 1))) {
            phone = "+886" + phone.substring(1);
        }

        // SHA256 Encode
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //phone = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
        byte[] hash = digest.digest(phone.getBytes(StandardCharsets.UTF_8));
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
