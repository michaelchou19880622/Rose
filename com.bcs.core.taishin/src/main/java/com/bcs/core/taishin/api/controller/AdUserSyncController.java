package com.bcs.core.taishin.api.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.taishin.api.model.AdUserSyncModel;
import com.bcs.core.taishin.service.TaishinAdService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/api")
public class AdUserSyncController {
    @Autowired
    private TaishinAdService taishinAdService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(AdUserSyncController.class);

    @WebServiceLog
    @RequestMapping(method = RequestMethod.POST, value = "/adUserSync/{ChannelId}",
            consumes = MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
    public ResponseEntity<?> adUserSync(@RequestBody String syncModel, @PathVariable String ChannelId, HttpServletRequest request, HttpServletResponse response) {
        logger.info("-------adUserSync-------");
        Date start = new Date();
        logger.info("syncModel:" + syncModel);

        String error = "";

        try {

            AdUserSyncModel model = ObjectUtil.jsonStrToObject(syncModel, AdUserSyncModel.class);
            logger.info("-------adUserSync model-------:" + model);

            taishinAdService.syncAdUser(model);

            logger.info("-------adUserSync Success-------");
            response.setStatus(200);
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_AdUserSync, start, 200, syncModel, "200");
            return new ResponseEntity<>(createResult(200, "Success"), HttpStatus.OK);
        } catch (Exception e) {
            error = e.getMessage();
            logger.info(ErrorRecord.recordError(e));
        }
        logger.info("-------adUserSync Fail-------");
        response.setStatus(500);
        SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_AdUserSync, start, 500, syncModel, "500");
        return new ResponseEntity<>(createResult(500, error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createResult(Integer status, String msg) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("status", status);
        result.put("msg", msg);

        return result;
    }
}
