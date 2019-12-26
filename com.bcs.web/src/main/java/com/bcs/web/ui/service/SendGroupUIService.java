package com.bcs.web.ui.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.repository.SendGroupDetailRepository;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.UserEventSetService;
import com.bcs.core.enums.EVENT_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;

/**
 * @author ???
 */
@Slf4j
@Service
public class SendGroupUIService {

    @Autowired
    private SendGroupService sendGroupService;
    @Autowired
    private SendGroupDetailRepository sendGroupDetailRepository;
    @Autowired
    private ImportDataFromExcel importMidFromExcel;
    @Autowired
    private ImportDataFromText importMidFromText;
    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private UserEventSetService userEventSetService;

    private static final int TRANSACTION_TIMEOUT_RETRY_MAX_TIMES = 3;
    private static final String EXCEL1 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL2 = "application/vnd.ms-excel";
    private static final String PLAIN_TEXT = "text/plain";

    private List<String> existMids = new ArrayList<>();
    private String referenceId;
    private String fileName;
    private Date modifyTime;
    private String modifyUser;
    private int curSaveIndex = 0;
    private int transactionTimeoutRetry = 0;

    /**
     * 新增或修改發送群組
     *
     * @param sendGroup
     * @param adminUserAccount
     * @return
     * @throws BcsNoticeException
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public SendGroup saveFromUI(SendGroup sendGroup, String adminUserAccount) throws BcsNoticeException {
        log.info("saveFromUI:" + sendGroup);

        Long groupId = sendGroup.getGroupId();
        if (groupId != null && groupId < 0) {
            throw new BcsNoticeException("預設群組無法修改");
        }

        String action = (groupId == null ? "Create" : "Edit");

        // Set Modify Admin User
        sendGroup.setModifyUser(adminUserAccount);
        sendGroup.setModifyTime(new Date());

        List<SendGroupDetail> list = sendGroup.getSendGroupDetail();
        sendGroup.setSendGroupDetail(new ArrayList<>());

        // Save Send Group
        sendGroupService.save(sendGroup);

        if (list != null) {
            for (SendGroupDetail detail : list) {
                detail.setSendGroup(sendGroup);
                sendGroup.getSendGroupDetail().add(detail);
            }
        }
        sendGroupService.save(sendGroup);
        sendGroup = sendGroupService.findOne(sendGroup.getGroupId());
        createSystemLog(action, sendGroup, sendGroup.getModifyUser(), sendGroup.getModifyTime(), sendGroup.getGroupId().toString());
        return sendGroup;
    }

    /**
     * 刪除發送群組
     *
     * @param groupId
     * @param adminUserAccount
     * @throws BcsNoticeException
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void deleteFromUI(Long groupId, String adminUserAccount) throws BcsNoticeException {
        log.info("deleteFromUI:" + groupId);
        if (groupId < 0) {
            throw new BcsNoticeException("預設群組無法刪除");
        }
        String groupTitle = sendGroupService.findGroupTitleByGroupId(groupId);
        sendGroupService.delete(groupId);
        createSystemLog("Delete", groupTitle, adminUserAccount, new Date(), groupId.toString());
    }

    /**
     * 新增系統日誌
     *
     * @param action
     * @param content
     * @param modifyUser
     * @param modifyTime
     */
    private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("SendGroup", action, modifyUser, content, referenceId);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 300000)
    public Map<String, Object> uploadMidSendGroup(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception {
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
        this.fileName = filePart.getOriginalFilename();
        String contentType = filePart.getContentType();
        log.info("File Name: {}, Size: {}, ContentType: {}", new Object[]{fileName, filePart.getSize(), contentType});


        Set<String> midSet = null;
        if (EXCEL1.equals(contentType) || EXCEL2.equals(contentType)) {
            midSet = importMidFromExcel.importData(filePart.getInputStream());
        } else if (PLAIN_TEXT.equals(contentType)) {
            midSet = importMidFromText.importData(filePart.getInputStream());
        }

        log.info("Mids: " + DataUtils.toPrettyJsonUseJackson(midSet));

        if (midSet == null) {
            throw new BcsNoticeException("上傳格式錯誤");
        }

        if (midSet.isEmpty()) {
            throw new BcsNoticeException("上傳沒有UID");
        }

        List<String> list = new ArrayList<>(midSet);
        log.info("list.size():" + list.size());

        try {
            List<String> checkList = new ArrayList<>();

            int i = 1;
            for (String mid : list) {
                checkList.add(mid);

                /* 每一千筆處理一次 */
                if (i % 1000 == 0) {
                    existMids.addAll(lineUserService.findMidByMidInAndActive(checkList));
                    checkList.clear();
                }
                i++;
            }
            /* 處理未滿一千的 */
            if (!checkList.isEmpty()) {
                existMids.addAll(lineUserService.findMidByMidInAndActive(checkList));
            }
        } catch (Exception e) {
            log.info("Exception", e);
        }

        if (existMids == null || existMids.isEmpty()) {
            throw new BcsNoticeException("上傳沒有UID");
        }

        referenceId = UUID.randomUUID().toString().toLowerCase();

        try {
            curSaveIndex = 0;
            for (String mid : existMids) {
                UserEventSet userEventSet = new UserEventSet();
                userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
                userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());
                userEventSet.setReferenceId(referenceId);
                userEventSet.setMid(mid);
                userEventSet.setContent(fileName);
                userEventSet.setSetTime(modifyTime);
                userEventSet.setModifyUser(modifyUser);
                log.info("userEventSet1:" + userEventSet);
                userEventSetService.save(userEventSet);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("transaction timeout expired")) {
                transactionTimeoutRetry += 1;
                log.info("Save [UserEventSet] retry : " + transactionTimeoutRetry);
                String cause = transactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES ? "TimeOut" : "RetrySaveUserEventSet";
                throw new Exception(cause);
            }
        }

        Map<String, Object> result = new HashMap<>(2);
        result.put("referenceId", referenceId);
        result.put("count", existMids.size());
        log.info("result:" + result);

        existMids.clear();
        return result;
    }

    public Map<String, Object> retrySaveUserEventSet() {
        try {
            return retrySaveUserEventSet(existMids, referenceId, fileName, modifyTime, modifyUser, curSaveIndex);
        } catch (Exception e) {
            log.error("Exception", e);
        }

        return null;
    }

    /**
     * Retry to save UserEventSet
     */
    @Transactional(rollbackFor = Exception.class, timeout = -1)
    public Map<String, Object> retrySaveUserEventSet(List<String> existMids, String referenceId, String fileName, Date modifyTime, String modifyUser, int curSaveIndex) throws Exception {
        this.existMids = existMids;
        this.referenceId = referenceId;
        this.fileName = fileName;
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
        this.curSaveIndex = curSaveIndex;

        try {
            for (int i = this.curSaveIndex; i < existMids.size(); i++) {
                String mid = existMids.get(i);
                UserEventSet userEventSet = new UserEventSet();
                userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
                userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());
                userEventSet.setReferenceId(referenceId);
                userEventSet.setMid(mid);
                userEventSet.setContent(fileName);
                userEventSet.setSetTime(modifyTime);
                userEventSet.setModifyUser(modifyUser);
                log.info("userEventSet1:" + userEventSet);
                userEventSetService.save(userEventSet);
            }

            Map<String, Object> result = new HashMap<>(2);
            result.put("referenceId", referenceId);
            result.put("count", existMids.size());
            log.info("result:" + result);
            return result;

        } catch (Exception e) {
            if (e.getMessage().contains("transaction timeout expired")) {
                transactionTimeoutRetry += 1;
                log.info("Save [UserEventSet] retry : " + transactionTimeoutRetry);
                String cause = transactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES ? "TimeOut" : "RetrySaveUserEventSet";
                throw new Exception(cause);
            }
            throw new BcsNoticeException("資料量過大導致超時，重試異常");
        }
    }

    @Transactional(rollbackFor = Exception.class, timeout = 300000)
    public Map<String, Object> uploadMidSendGroup(InputStream inputStream, String modifyUser, Date modifyTime, String fileName) throws Exception {
        Set<String> mids = null;
        mids = importMidFromExcel.importData(inputStream);
        if (mids == null) {
            throw new BcsNoticeException("上傳格式錯誤");
        }
        if (mids.isEmpty()) {
            throw new BcsNoticeException("上傳沒有UID");
        }

        List<String> list = new ArrayList<>(mids);
        log.info("list:" + list);
//        List<String> existMids = new ArrayList<>();
//			// Check MID Exist by Part
//			List<String> check = new ArrayList<String>();
//			for(int i = 1; i <= list.size(); i++){
//				log.info(" UID " + i + " : " +list.get(i-1));
//				check.add(list.get(i-1));
//
//				if(i % 1000 == 0){
//					List<String> midResult = lineUserService.findMidByMidInAndActive(check);
//					if(midResult != null && midResult.size() > 0){
//						existMids.addAll(midResult);
//					}
//					check.clear();
//				}
//			}
//			if(check.size() > 0){
//				List<String> midResult = lineUserService.findMidByMidInAndActive(check);
//				if(midResult != null && midResult.size() > 0){
//					existMids.addAll(midResult);
//				}
//			}
        if (list.isEmpty()) {
            throw new BcsNoticeException("上傳沒有UID");
        }
        log.debug("list:" + list);

        referenceId = UUID.randomUUID().toString().toLowerCase();

        try {
            curSaveIndex = 0;
            for (String mid : list) {
                UserEventSet userEventSet = new UserEventSet();
                userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
                userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());
                userEventSet.setReferenceId(referenceId);
                userEventSet.setMid(mid);
                userEventSet.setContent(fileName);
                userEventSet.setSetTime(modifyTime);
                userEventSet.setModifyUser(modifyUser);

                log.info("userEventSet1:" + userEventSet);

                userEventSetService.save(userEventSet);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("transaction timeout expired")) {
                transactionTimeoutRetry += 1;
                log.info("Save [UserEventSet] retry : " + transactionTimeoutRetry);
                String cause = transactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES ? "TimeOut" : "RetrySaveUserEventSet";
                throw new Exception(cause);
            }
        }

        log.info("count : " + list.size());

        Map<String, Object> result = new HashMap<>(2);
        result.put("referenceId", referenceId);
        result.put("count", list.size());

        return result;
    }

}
