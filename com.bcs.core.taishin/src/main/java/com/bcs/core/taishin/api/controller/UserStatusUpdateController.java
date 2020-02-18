package com.bcs.core.taishin.api.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.UpdateStatusModel;
import com.bcs.core.taishin.service.RichartValidateService;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LineIdUtil;
import com.bcs.core.utils.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/api")
public class UserStatusUpdateController {
    @Autowired
    private RichartValidateService richartValidateService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(UserStatusUpdateController.class);

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/userStatusUpdate/{ChannelId}",
            consumes = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public ResponseEntity<?> userStatusUpdate(@RequestBody String updateModel, @PathVariable String ChannelId, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("-------userStatusUpdate-------");
        Date start = new Date();
        logger.debug("updateModel:" + updateModel);

        String error = "";

        try {

            UpdateStatusModel model = ObjectUtil.jsonStrToObject(updateModel, UpdateStatusModel.class);
            logger.info("-------userStatusUpdate model-------:" + model);

            if (LineIdUtil.isLineUID(model.getUid())) {
                // Validate
            } else {
                throw new Exception("UidError");
            }

            if (LineUser.STATUS_BINDED.equals(model.getStatus())
                    || LineUser.STATUS_UNBIND.equals(model.getStatus())
                    || LineUser.STATUS_SYS_ADD.equals(model.getStatus())) {
                // Validate
            } else {
                throw new Exception("StatusError");
            }

            richartValidateService.bindedLineUser(model);

            logger.debug("-------userStatusUpdate Success-------");
            response.setStatus(200);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_BcsApi, LOG_TARGET_ACTION_TYPE.ACTION_BcsApi_UpdateStatus, start, 200, updateModel, "200");
            return new ResponseEntity<>(createResult(200, "Success"), HttpStatus.OK);
        } catch (Exception e) {
            error = e.getMessage();
            logger.error(ErrorRecord.recordError(e));
        }
        logger.debug("-------userStatusUpdate Fail-------");
        response.setStatus(500);
        SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_BcsApi, LOG_TARGET_ACTION_TYPE.ACTION_BcsApi_UpdateStatus, start, 500, updateModel, "500");
        return new ResponseEntity<>(createResult(500, error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/admin/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createUser(@RequestBody String requestBodyString, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
                return new ResponseEntity<>("{\"error\": \"true\", \"msg\": \"missing headers\"}", HttpStatus.BAD_REQUEST);
            }

            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization.split("Basic ").length != 2) {
                return new ResponseEntity<>("{\"error\": \"true\", \"msg\": \"invalid authorization format\"}", HttpStatus.UNAUTHORIZED);
            }

            String token = authorization.split("Basic ")[1];
            String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
            String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
            String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);

            if (!CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv).equals(originalToken)) {
                return new ResponseEntity<>("{\"error\": \"true\", \"msg\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            }

            JSONObject requestBody = new JSONObject(requestBodyString);

            String account = requestBody.getString("account");
            String username = requestBody.getString("username");
            String password = requestBody.getString("password");

            if (StringUtils.isEmpty(account) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
                return new ResponseEntity<String>("{\"error\": \"true\", \"msg\": \"Parameters cannot be empty.\"}", HttpStatus.OK);

            if (adminUserService.findOne(account) == null) {
                AdminUser adminUser = new AdminUser();

                adminUser.setAccount(account);
                adminUser.setUserName(username);
                adminUser.setRole("ROLE_ADMIN");
                adminUser.setModifyUser("SYSTEM");
                adminUser.setModifyTime(new Date());
                adminUser.setCanCopy(true);
                adminUser.setCanPrinting(true);
                adminUser.setCanSave(true);
                adminUser.setMid(null);
                adminUser.setPassword(passwordEncoder.encode(password));

                adminUserService.save(adminUser);

                return new ResponseEntity<String>("{\"error\": \"false\", \"msg\": null}", HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("{\"error\": \"true\", \"msg\": \"Account '" + account + "' is already exist.\"}", HttpStatus.OK);
            }
        } catch (Exception e) {
            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"msg\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
            else if (e instanceof JSONException)
                return new ResponseEntity<String>("{\"error\": \"true\", \"msg\": \"Invalid request body.\"}", HttpStatus.BAD_REQUEST);

            return new ResponseEntity<>("{\"error\": \"true\", \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> createResult(Integer status, String msg) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("status", status);
        result.put("msg", msg);

        return result;
    }
}
