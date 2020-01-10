package com.bcs.web.ui.controller;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.MsgSendRecordService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.SendMsgModel;
import com.bcs.web.ui.service.ExportExcelUIService;
import com.bcs.web.ui.service.SendMsgUIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type Bcs msg send controller.
 */
@Controller
@RequestMapping("/bcs")
public class BCSMsgSendController extends BCSBaseController {

    private SendGroupService sendGroupService;
    private AdminUserService adminUserService;
    private SendMsgUIService sendMsgUiService;
    private MsgMainService msgMainService;
    private MsgSendMainService msgSendMainService;
    private MsgDetailService msgDetailService;
    private ExportExcelUIService exportExcelUiService;
    private MsgSendRecordService msgSendRecordService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(BCSMsgSendController.class);

    /**
     * Instantiates a new Bcs msg send controller.
     *
     * @param sendGroupService     the send group service
     * @param adminUserService     the admin user service
     * @param sendMsgUiService     the send msg ui service
     * @param msgMainService       the msg main service
     * @param msgSendMainService   the msg send main service
     * @param msgDetailService     the msg detail service
     * @param exportExcelUiService the export excel ui service
     * @param msgSendRecordService the msg send record service
     */
    @Autowired
    public BCSMsgSendController(SendGroupService sendGroupService,
                                AdminUserService adminUserService,
                                SendMsgUIService sendMsgUiService,
                                MsgMainService msgMainService,
                                MsgSendMainService msgSendMainService,
                                MsgDetailService msgDetailService,
                                ExportExcelUIService exportExcelUiService,
                                MsgSendRecordService msgSendRecordService) {
        this.sendGroupService = sendGroupService;
        this.adminUserService = adminUserService;
        this.sendMsgUiService = sendMsgUiService;
        this.msgMainService = msgMainService;
        this.msgSendMainService = msgSendMainService;
        this.msgDetailService = msgDetailService;
        this.exportExcelUiService = exportExcelUiService;
        this.msgSendRecordService = msgSendRecordService;
    }

    /**
     * 建立訊息 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgCreatePage string
     */
    @GetMapping(value = "/edit/msgCreatePage")
    public String msgCreatePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgCreatePage");

        return BcsPageEnum.MsgCreatePage.toString();
    }

    /**
     * 訊息列表 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgListDraftPage string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/msgListPage")
    public String msgListPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgListPage");

        return BcsPageEnum.MsgListDraftPage.toString();
    }

    /**
     * Cdn msg create page string.
     *
     * @param request  the request
     * @param response the response
     * @return the string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/cdnMsgCreatePage")
    public String cdnMsgCreatePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("cdnMsgCreatePage");
        return BcsPageEnum.CdnMsgCreatePage.toString();
    }

    /**
     * 訊息列表 草稿 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgListDraftPage string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/msgListDraftPage")
    public String msgListDraftPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgListDraftPage");

        return BcsPageEnum.MsgListDraftPage.toString();
    }

    /**
     * 訊息列表 預約 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgListDelayPage string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/msgListDelayPage")
    public String msgListDelayPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgListDelayPage");
        return BcsPageEnum.MsgListDelayPage.toString();
    }

    /**
     * 訊息列表 已傳送 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgList Sent Page string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/msgListSendedPage")
    public String msgListSendedPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgListSendedPage");
        return BcsPageEnum.MsgListSendedPage.toString();
    }

    /**
     * 訊息列表 排程傳送 導頁
     *
     * @param request  the request
     * @param response the response
     * @return MsgListSchedulePage string
     */
    @WebServiceLog
    @GetMapping(value = "/edit/msgListSchedulePage")
    public String msgListSchedulePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("msgListSchedulePage");
        return BcsPageEnum.MsgListSchedulePage.toString();
    }

    /**
     * 取得 訊息資料 by msgId or msgSendId
     *
     * @param request    the request
     * @param response   the response
     * @param customUser the custom user
     * @param msgId      the msg id
     * @param msgSendId  the msg send id
     * @return Map<String, Object>  send msg
     */
    @WebServiceLog
    @GetMapping(value = "/edit/getSendMsg")
    @ResponseBody
    public ResponseEntity<?> getSendMsg(
            HttpServletRequest request, HttpServletResponse response,
            @CurrentUser CustomUser customUser, @RequestParam(required = false) String msgId,
            @RequestParam(required = false) String msgSendId) {
        logger.info("getSendMsg");

        try {
            if (StringUtils.isNotBlank(msgId)) {
                logger.info("msgId:" + msgId);
                Map<String, Object> result = new LinkedHashMap<>();

                Map<MsgMain, List<MsgDetail>> map = msgMainService.queryGetMsgMainDetailByMsgId(Long.parseLong(msgId));

                if (map != null && map.size() == 1) {
                    result.put("MsgMain", map);
                    sendMsgUiService.setGroups(result);
                    for (List<MsgDetail> details : map.values()) {
                        sendMsgUiService.setDetailContent(result, details);
                    }
                    return new ResponseEntity<>(result, HttpStatus.OK);
                }
            } else if (StringUtils.isNotBlank(msgSendId)) {
                logger.info("msgSendId:" + msgSendId);
                Map<String, Object> result = new LinkedHashMap<>();

                Map<MsgSendMain, List<MsgDetail>> map = msgSendMainService.queryGetMsgSendMainDetailByMsgId(Long.parseLong(msgSendId));

                if (map != null && map.size() == 1) {
                    result.put("MsgMain", map);
                    sendMsgUiService.setGroups(result);
                    for (List<MsgDetail> details : map.values()) {
                        sendMsgUiService.setDetailContent(result, details);
                    }
                    return new ResponseEntity<>(result, HttpStatus.OK);
                }
            }

            logger.error("Status Null");
            throw new BcsNoticeException("查詢參數錯誤");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * 取得 訊息資料列表
     *
     * @param request    the request
     * @param response   the response
     * @param customUser the custom user
     * @param status     the status
     * @param sendType   the send type
     * @return Map<String, Object>  send msg list
     */
    @WebServiceLog
    @GetMapping(value = "/edit/getSendMsgList")
    @ResponseBody
    public ResponseEntity<?> getSendMsgList(HttpServletRequest request, HttpServletResponse response,
                                            @CurrentUser CustomUser customUser, @RequestParam String status, @RequestParam(required = false) String sendType) {
        logger.info("getSendMsgList");

        try {
            if (StringUtils.isBlank(status)) {
                logger.error("Status Null");
                throw new BcsNoticeException("查詢參數錯誤");
            }
            logger.info("status:" + status);
            Map<String, Object> result = new LinkedHashMap<>();

            Map<MsgMain, List<MsgDetail>> map = null;

            if (StringUtils.isNotBlank(sendType)) {
                map = msgMainService.queryGetMsgMainDetailByStatusAndSendType(status, sendType);
            } else {
                map = msgMainService.queryGetMsgMainDetailByStatus(status);
            }

            if (map == null) {
                logger.error("Map Null");
                throw new BcsNoticeException("查詢參數錯誤");
            }
            result.put("MsgMain", map);

            sendMsgUiService.setGroups(result);

            try {
                Map<String, AdminUser> admins = adminUserService.findAllMap();
                Map<String, String> adminMap = new HashMap<>();
                for (MsgMain msg : map.keySet()) {
                    String userAccount = msg.getModifyUser();
                    if (admins.containsKey(userAccount)) {
                        adminMap.put(userAccount, admins.get(userAccount).getUserName());
                    }
                }
                result.put("AdminUser", adminMap);
            } catch (Exception e) {
                logger.error(ErrorRecord.recordError(e));
            }

            for (List<MsgDetail> details : map.values()) {
                sendMsgUiService.setDetailContent(result, details);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取得 訊息資料列表 (已傳送)
     *
     * @param request    the request
     * @param response   the response
     * @param customUser the custom user
     * @return Map<String, Object>  sent msg list
     */
    @WebServiceLog
    @GetMapping(value = "/edit/getSendedMsgList")
    @ResponseBody
    public ResponseEntity<?> getSentMsgList(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
        logger.info("getSentMsgList");

        try {
            Map<String, Object> result = new LinkedHashMap<>();

            Map<MsgSendMain, List<MsgDetail>> map = msgSendMainService.queryGetMsgSendMainDetailAll();

            if (map == null) {
                logger.error("Data Null");
                throw new BcsNoticeException("查詢參數錯誤");
            }
            result.put("MsgMain", map);

            try {
                Map<Long, String> groups = sendGroupService.findGroupTitleMap();
                result.put("SendGroup", groups);
            } catch (Exception e) {
                logger.error(ErrorRecord.recordError(e));
            }

            try {
                Map<String, AdminUser> admins = adminUserService.findAllMap();
                Map<String, String> adminMap = new HashMap<>();
                for (MsgSendMain msg : map.keySet()) {
                    String userAccount = msg.getModifyUser();
                    if (admins.containsKey(userAccount)) {
                        adminMap.put(userAccount, admins.get(userAccount).getUserName());
                    }
                }
                result.put("AdminUser", adminMap);
            } catch (Exception e) {
                logger.error(ErrorRecord.recordError(e));
            }

            for (List<MsgDetail> details : map.values()) {
                sendMsgUiService.setDetailContent(result, details);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sending cdn msg response entity.
     *
     * @param request      the request
     * @param response     the response
     * @param customUser   the custom user
     * @param sendMsgModel the send msg model
     * @return the response entity
     */
    @WebServiceLog
    @PostMapping(value = "/edit/sendingCdnMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> sendingCdnMsg(HttpServletRequest request, HttpServletResponse response,
                                           @CurrentUser CustomUser customUser, @RequestBody SendMsgModel sendMsgModel) {
        logger.info("sendingCdnMsg");
        logger.info("sendMsgModel:" + sendMsgModel);
        List<SendMsgDetailModel> details = sendMsgModel.getSendMsgDetails();

        List<SendMsgDetailModel> newD = new ArrayList<>();
        for (SendMsgDetailModel detail : details) {
            detail.setDetailType("TEXT");

            String oldContext = detail.getDetailContent();
            int i1 = oldContext.indexOf("Id") + 5;
            int i2 = oldContext.indexOf("\"}");
            String context = oldContext.substring(i1, i2);

            detail.setDetailContent("{\"Text\":\"" + UriHelper.getCdnResourceUri("IMAGE", context) + "\"}");
            newD.add(detail);
        }

        sendMsgModel.setSendMsgDetails(newD);
        logger.info("new sendMsgModel:" + sendMsgModel);

        try {
            if (StringUtils.isBlank(sendMsgModel.getActionType())) {
                throw new Exception("ActionType Null");
            }
            if (SendMsgModel.ACTION_TYPE.SendToMe.toString().equals(sendMsgModel.getActionType())) {
                return sendToMe(sendMsgModel, customUser);
            }
            if (SendMsgModel.ACTION_TYPE.SendToTestGroup.toString().equals(sendMsgModel.getActionType())) {
                return sendToTestGroup(sendMsgModel, customUser);
            }
            if (SendMsgModel.ACTION_TYPE.SaveDraft.toString().equals(sendMsgModel.getActionType())) {
                return saveToDraft(sendMsgModel, customUser);
            }
            if (SendMsgModel.ACTION_TYPE.SendMsg.toString().equals(sendMsgModel.getActionType())) {
                if (customUser.isAdmin()) {
                    return sendMsg(sendMsgModel, customUser);
                }
                throw new BcsNoticeException("權限錯誤");
            }
            throw new Exception("Validate ActionType Error");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 傳送, 儲存 訊息
     *
     * @param request      the request
     * @param response     the response
     * @param customUser   the custom user
     * @param sendMsgModel the send msg model
     * @return String Result
     */
    @WebServiceLog
    @PostMapping(value = "/edit/sendingMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> sendingMsg(HttpServletRequest request, HttpServletResponse response,
                                        @CurrentUser CustomUser customUser, @RequestBody SendMsgModel sendMsgModel) {
        logger.info("sendingMsg");

        try {
            if (sendMsgModel != null) {
                if (StringUtils.isBlank(sendMsgModel.getActionType())) {
                    throw new Exception("ActionType Null");
                }
                if (SendMsgModel.ACTION_TYPE.SendToMe.toString().equals(sendMsgModel.getActionType())) {
                    return sendToMe(sendMsgModel, customUser);
                }
                if (SendMsgModel.ACTION_TYPE.SendToTestGroup.toString().equals(sendMsgModel.getActionType())) {
                    return sendToTestGroup(sendMsgModel, customUser);
                }
                if (SendMsgModel.ACTION_TYPE.SaveDraft.toString().equals(sendMsgModel.getActionType())) {
                    return saveToDraft(sendMsgModel, customUser);
                }
                if (SendMsgModel.ACTION_TYPE.SendMsg.toString().equals(sendMsgModel.getActionType())) {
                    if (customUser.isAdmin()) {
                        return sendMsg(sendMsgModel, customUser);
                    }
                    throw new BcsNoticeException("權限錯誤");
                }
                throw new Exception("Validate ActionType Error");
            }
            throw new Exception("SendMsgModel Null");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 傳送, 儲存 訊息
     *
     * @param request      the request
     * @param response     the response
     * @param customUser   the custom user
     * @param sendMsgModel the send msg model
     * @return String Result
     * @throws IOException
     */
    @WebServiceLog
    @PostMapping(value = "/edit/redeisgnSendMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> redeisgnSendMsg(HttpServletRequest request, HttpServletResponse response,
                                             @CurrentUser CustomUser customUser, @RequestBody SendMsgModel sendMsgModel
    ) {
        logger.info("redeisgnSendMsg");

        try {
            if (sendMsgModel != null) {
                if (StringUtils.isBlank(sendMsgModel.getActionType())) {
                    throw new Exception("ActionType Null");
                }
                if (SendMsgModel.ACTION_TYPE.RedesignMsg.toString().equals(sendMsgModel.getActionType())) {
                    return redesignMsg(sendMsgModel, customUser);
                }
                throw new Exception("Validate ActionType Error");
            }
            logger.error("SendMsgModel Null");
            throw new BcsNoticeException("請選擇正確的訊息");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刪除訊息
     *
     * @param customUser the custom user
     * @param msgId      the msg id
     * @param msgSendId  the msg send id
     * @return String response entity
     */
    @WebServiceLog
    @DeleteMapping(value = "/admin/deleteSendMsg")
    @ResponseBody
    public ResponseEntity<?> deleteSendMsg(@CurrentUser CustomUser customUser, @RequestParam(required = false) String msgId, @RequestParam(required = false) String msgSendId) {
        logger.info("deleteSendMsg");
        boolean isAdmin = customUser.isAdmin();
        if (!isAdmin) {
            return new ResponseEntity<>("User No Delete Right", HttpStatus.OK);
        }
        try {
            if (StringUtils.isNotBlank(msgId)) {
                logger.info("msgId:" + msgId);
                sendMsgUiService.deleteMessageMain(Long.parseLong(msgId));
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            if (StringUtils.isNotBlank(msgSendId)) {
                logger.info("msgSendId:" + msgSendId);
                sendMsgUiService.deleteMessageSendMain(Long.parseLong(msgSendId));
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            logger.error("msgId msgSendId Null");
            throw new BcsNoticeException("請選擇正確的訊息");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 傳送給我
     */
    private ResponseEntity<?> sendToMe(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception {

        String account = customUser.getAccount();
        String mid = customUser.getMid();
        if (StringUtils.isBlank(mid)) {
            logger.error("You Not Setting LINE MID");
            throw new BcsNoticeException("請設定綁定LINE帳號");
        }

        List<SendMsgDetailModel> details = sendMsgModel.getSendMsgDetails();
        details.add(0, generateTestMsgNotice());

        sendMsgUiService.sendMsgToMid(mid, sendMsgModel.getSendMsgDetails(), account, sendMsgModel);
        String result = "Sending Message To Me Success";
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 傳送給測試群組
     */
    private ResponseEntity<?> sendToTestGroup(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception {

        String account = customUser.getAccount();

        List<AdminUser> list = adminUserService.findByMidNotNull();
        List<String> midList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            logger.error("No Test Group");
            throw new BcsNoticeException("測試人員都沒有綁定LINE帳號");
        }
        for (AdminUser adminUser : list) {
            if (StringUtils.isNotBlank(adminUser.getMid())) {
                midList.add(adminUser.getMid());
            }
        }
        // 設定 Test Message Notice Start
        List<SendMsgDetailModel> details = sendMsgModel.getSendMsgDetails();
        details.add(0, generateTestMsgNotice());

        sendMsgUiService.sendMsgToMids(midList, sendMsgModel.getSendMsgDetails(), account, sendMsgModel);
        String result = "Sending Message To Test Group Success";
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 儲存草稿
     */
    private ResponseEntity<?> saveToDraft(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception {
        String adminUserAccount = customUser.getAccount();
        Long msgId = sendMsgUiService.saveDraftMessageWithId(sendMsgModel, adminUserAccount);
        return new ResponseEntity<>(msgId, HttpStatus.OK);
    }

    private SendMsgDetailModel generateTestMsgNotice() {
        SendMsgDetailModel detail = new SendMsgDetailModel();
        detail.setDetailType(MsgGenerator.MSG_TYPE_TEXT);
        ObjectNode content = (new ObjectMapper()).createObjectNode();
        content.put("Text", "***此為測試訊息***");
        detail.setDetailContent(ObjectUtil.objectToJsonStr(content));
        return detail;
    }

    /**
     * 傳送訊息
     */
    private ResponseEntity<?> sendMsg(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception {
        String adminUserAccount = customUser.getAccount();
        sendMsgUiService.sendMessage(sendMsgModel, adminUserAccount);
        String result = "Send Message Success";
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 重設訊息
     *
     * @param sendMsgModel sendMsgModel
     * @param customUser   customUser
     * @return String
     * @throws Exception Exception
     */
    private ResponseEntity<?> redesignMsg(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception {
        String adminUserAccount = customUser.getAccount();
        sendMsgUiService.redesignMsg(sendMsgModel, adminUserAccount);
        String result = "Redesign Message Success";
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Export to excel for sent msg.
     *
     * @param request    the request
     * @param response   the response
     * @param customUser the custom user
     * @param msgSendId  the msg send id
     * @throws Exception the exception
     */
    @WebServiceLog
    @GetMapping(value = "/edit/exportToExcelForSendedMsg")
    @ResponseBody
    public void exportToExcelForSentMsg(HttpServletRequest request, HttpServletResponse response,
                                        @CurrentUser CustomUser customUser, @RequestParam(required = false) String msgSendId) throws Exception {
        logger.info("exportToExcelForSentMsg");

        if (StringUtils.isBlank(msgSendId)) {
            throw new Exception("資料產生錯誤");
        }
        logger.info("msgSendId:" + msgSendId);
        List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(Long.parseLong(msgSendId), MsgSendMain.THIS_PARENT_TYPE);

        StringBuilder title = new StringBuilder();
        for (MsgDetail detail : details) {
            if (StringUtils.isNotBlank(detail.getText())) {
                title.append(detail.getText());
            }
        }

        List<MsgSendRecord> records = msgSendRecordService.findByMsgSendId(Long.parseLong(msgSendId));

        if (records != null && !records.isEmpty()) {

            Set<String> midSet = new HashSet<>();
            for (MsgSendRecord record : records) {
                String sendRecord = record.getSendRecord();
                if (StringUtils.isNotBlank(sendRecord) && sendRecord.equals("\"200-\"")) {
                    midSet.add(record.getMid());
                }
            }
            List<String> titles = new ArrayList<>();
            titles.add("收訊人UID");
            List<List<String>> data = new ArrayList<>();
            data.add(new ArrayList<>(midSet));

            exportExcelUiService.exportMidResultToExcel(request, response, "SendedMsg", "發送訊息:" + title.toString(), null, titles, data);
        }
    }
}
