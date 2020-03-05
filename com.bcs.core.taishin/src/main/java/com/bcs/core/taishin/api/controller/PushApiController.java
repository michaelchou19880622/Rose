package com.bcs.core.taishin.api.controller;

import com.bcs.core.api.service.model.PushApiModel;
import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.bot.akka.service.PNPService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.validator.PushApiRequestValidator;
import com.bcs.core.utils.CryptUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class PushApiController {
    private static Logger logger = Logger.getLogger(PushApiController.class);

    @Autowired
    private PNPService PNPService;

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/message/push", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> pushMessage(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBodyString) {
        try {
            logger.info("-------------------- pushMessage --------------------");
            logger.info("[pushMessage] Request body: " + requestBodyString);

            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
            } else {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

                if (authorization.split("key=").length != 2) {
                    return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
                }

                String token = authorization.split("key=")[1];
                logger.info("[pushMessage] token: " + token);
                
                String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
                logger.info("[pushMessage] secret: " + secret);
                
                String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
                logger.info("[pushMessage] iv: " + iv);
                
                String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);
                logger.info("[pushMessage] originalToken: " + originalToken);
                
                String descryptedToken = CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv);
                logger.info("[pushMessage] descryptedToken: " + descryptedToken);

                if (!descryptedToken.equals(originalToken)) {
                    return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Invalid token.\"}", HttpStatus.UNAUTHORIZED);
                }
            }

            PushApiModel pushApiModel = new PushApiModel();

            PushApiRequestValidator.validate(requestBodyString, pushApiModel);

            PNPService.tell(pushApiModel);

            return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
            else if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);

            return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}