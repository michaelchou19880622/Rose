package com.bcs.core.validate.service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User validate service
 * @author Jessie Wang
 * @author Alan
 */
@Slf4j
@Service
public class UserValidateService {
    @Autowired
    private LineUserService lineUserService;

    /**
     * User is bound
     *
     * @param mid mid
     * @return is bound
     */
    public boolean isBound(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        try {
            LineUser lineUser = lineUserService.findByMid(mid);
            if (lineUser == null) {
                return false;
            }
            log.info("User: " + lineUser);
            return LineUser.STATUS_BINDED.equals(lineUser.getStatus());
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return false;
        }
    }
}
