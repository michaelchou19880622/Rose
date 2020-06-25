package com.bcs.core.db.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.repository.LineUserRepository;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;

/**
 * The type Line user service.
 *
 * @author ???
 */
@Service
public class LineUserService {

    private static Logger logger = Logger.getLogger(LineUserService.class);

    @Autowired
    private LineUserRepository lineUserRepository;
    
    @Autowired
    private UserTraceLogService userTraceLogService;

    /**
     * Find mid by mid in list.
     *
     * @param mids the mids
     * @return the list
     */
    public List<String> findMidByMidIn(List<String> mids) {
        return lineUserRepository.findMidByMidIn(mids);
    }

    /**
     * Find mid by mid in and active list.
     *
     * @param mids the mids
     * @return the list
     */
    public List<String> findMidByMidInAndActive(List<String> mids) {
        return lineUserRepository.findMidByMidInAndActive(mids);
    }

    /**
     * Find LineUID List by Phone Number List
     *
     * @param phoneNumberList the phoneNumberList
     * @return Object[] 0:Phone Number 1:Line UID
     */
    public List<Object[]> findMidsByMobileIn(List<String> phoneNumberList) {
        return lineUserRepository.findMidsByMobileIn(phoneNumberList);
    }

    /**
     * Find LineUID List by Phone Number List
     *
     * @param phoneNumberList the phoneNumberList
     * @return Object[] 0:Phone Number 1:Line UID
     */
    public List<LineUser> findByMobileIn(List<String> phoneNumberList) {
        return lineUserRepository.findByMobileIn(phoneNumberList);
    }

    /**
     * Find by mid line user.
     *
     * @param mid the mid
     * @return the line user
     */
    public LineUser findByMid(String mid) {
        return lineUserRepository.findByMid(mid);
    }

    /**
     * Find by mid and create unbind line user.
     *
     * @param mid the mid
     * @return the line user
     */
    public LineUser findByMidAndCreateUnbind(String mid) {
        LineUser lineUser = findByMid(mid);
        if (lineUser == null) {
            Date time = new Date();
            lineUser = new LineUser();
            lineUser.setMid(mid);
            lineUser.setStatus(LineUser.STATUS_UNBIND);
            lineUser.setIsBinded(LineUser.STATUS_UNBIND);
            lineUser.setModifyTime(time);
            lineUser.setCreateTime(time);
            lineUser.setSoureType("user");
            save(lineUser);
        }
        return lineUser;
    }


    /**
     * Find by mid and create unbind line user.
     *
     * @param mid the mid
     * @return the line user
     */
    public LineUser findByMidAndCreateSysAdd(String mid) {
        LineUser lineUser = findByMid(mid);
        if (lineUser == null) {
            Date time = new Date();
            lineUser = new LineUser();
            lineUser.setMid(mid);
            lineUser.setStatus(LineUser.STATUS_SYS_ADD);
            lineUser.setIsBinded(LineUser.STATUS_UNBIND);
            lineUser.setModifyTime(time);
            lineUser.setCreateTime(time);
            lineUser.setSoureType("user");
            save(lineUser);
        }
        return lineUser;
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    public List<LineUser> findAll() {
        return lineUserRepository.findAll();
    }

    /**
     * Find mid all active page.
     *
     * @param page     the page
     * @param pageSize the page size
     * @return the page
     */
    public Page<String> findMIDAllActive(int page, int pageSize) {
        Pageable pageable = new PageRequest(page, pageSize);
        return lineUserRepository.findMIDAllActive(pageable);
    }

    /**
     * Check mid all active boolean.
     *
     * @param mid the mid
     * @return the boolean
     */
    public boolean checkMIDAllActive(String mid) {
        String result = lineUserRepository.checkMIDAllActive(mid);
//        logger.info("checkMIDAllActive:" + result);
        return StringUtils.isNotBlank(result);
    }

    /**
     * Check mid all active and SYSADD boolean.
     *
     * @param mid the mid
     * @return the boolean
     */
    public boolean checkMIDAllActiveAndSysAdd(String mid) {
        String result = lineUserRepository.checkMIDAllActiveAndSysAdd(mid);
        logger.info("checkMIDAllActiveAndSysAdd:" + result);
        return StringUtils.isNotBlank(result);
    }

    
    /**
     * Count all long.
     *
     * @return the long
     */
    public Long countAll() {
        return lineUserRepository.count();
    }

    /**
     * Count by status long.
     *
     * @param status the status
     * @return the long
     */
    public Long countByStatus(String status) {
        return lineUserRepository.countByStatus(status);
    }

    /**
     * Count by status long.
     *
     * @param status the status
     * @param start  the start
     * @param end    the end
     * @return the long
     */
    public Long countByStatus(String status, String start, String end) {
        return lineUserRepository.countByStatus(status, start, end);
    }

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    public List<LineUser> findByStatus(String status) {
        return lineUserRepository.findByStatus(status);
    }

    /**
     * Find mid by status page.
     *
     * @param status   the status
     * @param page     the page
     * @param pageSize the page size
     * @return the page
     */
    public Page<String> findMIDByStatus(String status, int page, int pageSize) {
        Pageable pageable = new PageRequest(page, pageSize);
        return lineUserRepository.findMIDByStatus(status, pageable);
    }

    /**
     * Check mid by status boolean.
     *
     * @param status the status
     * @param mid    the mid
     * @return the boolean
     */
    public Boolean checkMIDByStatus(String status, String mid) {

        String result = lineUserRepository.checkMIDByStatus(status, mid);
        logger.debug("checkMIDByStatus:" + result);
        return StringUtils.isBlank(result);
    }

    /**
     * Check mid in status boolean.
     *
     * @param status the status
     * @param mid    the mid
     * @return the boolean
     */
    public Boolean checkMIDByStatus(String status, String status2, String mid) {

        String result = lineUserRepository.checkMIDByStatus(status, status2, mid);
        logger.info("checkMIDByStatus:" + result);
        return StringUtils.isNotBlank(result);
    }

    /**
     * Get mid by mobile.
     *
     * @param mobile the mobile
     * @return the boolean
     */
    public String getMidByMobile(String mobile) {
        String mid = lineUserRepository.getMidByMobile(mobile);
        logger.info("Mobile: " + mobile + ", Mid: " + mid);
        return mid;
    }

    /**
     * Save log.
     *
     * @param lineUser    the line user
     * @param mid         the mid
     * @param action      the action
     * @param referenceId the reference id
     */
    public void saveLog(LineUser lineUser, String mid, LOG_TARGET_ACTION_TYPE action, String referenceId) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        UserTraceLog log = new UserTraceLog();
        log.setTarget(LOG_TARGET_ACTION_TYPE.TARGET_LineUser);
        log.setAction(action);
        log.setModifyUser(mid);
        log.setModifyTime(now);
        log.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
        log.setModifyDay(sdf.format(now));

        log.setContent(lineUser);
        log.setReferenceId(referenceId);
        userTraceLogService.bulkPersist(log);
    }

    /**
     * Save.
     *
     * @param lineUser the line user
     */
    public void save(LineUser lineUser) {
        lineUserRepository.save(lineUser);
    }

    /**
     * Bulk persist.
     *
     * @param lineUsers the line users
     */
    public void bulkPersist(List<LineUser> lineUsers) {
        lineUserRepository.bulkPersist(lineUsers);
    }

    /**
     * Find by mobile and birthday list.
     *
     * @param mobile   the mobile
     * @param birthday the birthday
     * @return the list
     */
    public List<LineUser> findByMobileAndBirthday(String mobile, String birthday) {
        return lineUserRepository.findByMobileAndBirthday(mobile, birthday);
    }

    /**
     * Find by create time list.
     *
     * @param start the start
     * @param end   the end
     * @return the list
     */
    public List<LineUser> findByCreateTime(String start, String end) {
        return lineUserRepository.findByCreateTime(start, end);
    }
}
