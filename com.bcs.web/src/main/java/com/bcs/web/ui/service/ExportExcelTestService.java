package com.bcs.web.ui.service;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.SystemLogService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.DEFAULT_SEND_GROUP;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.report.export.ExportToExcelForMid;
import com.bcs.core.report.service.ContentLinkReportService;
import com.bcs.core.utils.DBResultUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.model.LinkClickReportModel;

@Service
public class ExportExcelTestService {

    @Autowired
    private SendGroupService sendGroupService;
    @Autowired
    private UserTraceLogService userTraceLogService;
    @Autowired
    private MsgBotReceiveService msgBotReceiveService;
    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private ContentLinkService contentLinkService;
    @Autowired
    private ContentLinkReportService contentLinkReportService;
    @Autowired
    private MsgInteractiveMainService msgInteractiveMainService;
    @Autowired
    private MsgDetailService msgDetailService;
    @Autowired
    private SystemLogService systemLogService;
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private ExportToExcelForMid exportToExcelForMid;
    @Autowired
    private ContentGameService contentGameService;
    @Autowired
    private RecordReportService recordReportService;

    /** Logger */
    private static Logger logger = Logger.getLogger(ExportExcelTestService.class);

    public void exportExcel(String exportPath, String fileName, String sheetName, String startDate, String endDate,
            String linkUrl, String keyword) throws Exception {
        try {
            Workbook wb = new XSSFWorkbook(); // →xls // new XSSFWorkbook()→xlsx

            Sheet sheet = wb.createSheet(sheetName);
            this.exportExcel(wb, sheet, sheetName, startDate, endDate, linkUrl, keyword);
            FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
            wb.write(out);
            out.close();
            wb.close();

        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    public void exportExcel(Workbook wb, Sheet sheet, String sheetName, String startDate, String endDate,
            String linkUrl, String keyword) throws Exception {

        if ("Management".equals(sheetName)) { // 管理報表
            this.exportManagement(sheet, startDate, endDate);

        } else if ("SharedCampaign".equals(sheetName)) { // 紀錄_分享活動
            this.exportSharedCampaign(sheet, startDate, endDate);

        } else if ("Information".equals(sheetName)) { // 紀錄_一般資訊
            this.exportInformation(sheet, startDate, endDate);

        } else if ("InformationS".equals(sheetName)) { // 紀錄_一般資訊_統計
            this.exportInformationS(sheet, startDate, endDate);

        } else if ("Keyword".equals(sheetName)) { // 紀錄_關鍵字
            this.exportKeyword(sheet, startDate, endDate);

        } else if ("KeywordUid".equals(sheetName)) { // 紀錄_關鍵字_UID
            // this.exportKeywordUid(wb, sheet, keyword, startDate, endDate);

        } else if ("KeywordS".equals(sheetName)) { // 紀錄_關鍵字_統計
            this.exportKeywordS(sheet, startDate, endDate);

        } else if ("MarketingUrl".equals(sheetName)) { // 紀錄_行銷連結
            this.exportMarketingUrl(sheet, startDate, endDate);

        } else if ("MarketingUrlUid".equals(sheetName)) { // 紀錄_行銷連結_UID
            // this.exportMarketingUrlUid(sheet, startDate, endDate, linkUrl);

        } else if ("MarketingUrlS".equals(sheetName)) { // 紀錄_行銷連結_統計
            this.exportMarketingUrlS(sheet, startDate, endDate);
        }
    }

    private void exportManagement(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf2.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<SystemLog> loginLogList = systemLogService.findByTargetAndAction("AdminUser", "Login", startDate,
                sdf2.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("登入ID");
        row.createCell(1).setCellValue("登入日期");
        row.createCell(2).setCellValue("登入時間");
        row.createCell(3).setCellValue("活動名稱");
        row.createCell(4).setCellValue("活動類別");
        row.createCell(5).setCellValue("活動期間");
        row.createCell(6).setCellValue("修改日期");
        row.createCell(7).setCellValue("修改時間");
        row.createCell(8).setCellValue("目標項目");
        row.createCell(9).setCellValue("目標項目_日期");
        row.createCell(10).setCellValue("目標項目_時間");
        row.createCell(11).setCellValue("動作項目");
        row.createCell(12).setCellValue("動作項目_日期");
        row.createCell(13).setCellValue("動作項目_時間");

        AdminUser admin = new AdminUser();
        Map<String, AdminUser> tempMap = new HashMap<>();

        int rowCount = 1;
        for (int i = 0; i < loginLogList.size(); i++) {

            SystemLog sysLog = loginLogList.get(i);
            SystemLog nextSysLog = null;

            if (i < (loginLogList.size() - 1)) {
                nextSysLog = loginLogList.get(i + 1);
            }

            String modifyUser = sysLog.getModifyUser();

            Date loginDateTime = sysLog.getModifyTime();
            Date nextLoginDateTime = null;

            if (nextSysLog != null && modifyUser.equals(nextSysLog.getModifyUser())) {
                nextLoginDateTime = nextSysLog.getModifyTime();
            }

            if (!tempMap.containsKey(modifyUser)) {
                admin = adminUserService.findOne(modifyUser);
                tempMap.put(modifyUser, admin);
            }
            
            admin = tempMap.get(modifyUser);

            String loginDate = sdf.format(loginDateTime).split(",")[0];
            String loginTime = sdf.format(loginDateTime).split(",")[1];

            List<SystemLog> debugLogList = null;

            if (nextLoginDateTime != null) {
                debugLogList = systemLogService.findByModifyUserAndLevel(modifyUser, SystemLog.SYSTEM_LOG_LEVEL_DEBUG,
                        sdf3.format(loginDateTime), sdf3.format(nextLoginDateTime));
            } else {
                debugLogList = systemLogService.findByModifyUserAndLevel(modifyUser, SystemLog.SYSTEM_LOG_LEVEL_DEBUG,
                        sdf3.format(loginDateTime), sdf3.format(calendarEnd.getTime()));
            }

            if (debugLogList != null && debugLogList.size() > 0) {

                for (SystemLog debugLog : debugLogList) {
                    String debugDate = sdf.format(debugLog.getModifyTime()).split(",")[0];
                    String debugTime = sdf.format(debugLog.getModifyTime()).split(",")[1];

                    Row row1 = sheet.createRow(rowCount);
                    row1.createCell(0).setCellValue(admin.getMid() == null ? modifyUser : admin.getMid());
                    row1.createCell(1).setCellValue(loginDate);
                    row1.createCell(2).setCellValue(loginTime);
                    row1.createCell(3).setCellValue("null");
                    row1.createCell(4).setCellValue("null");
                    row1.createCell(5).setCellValue("null");
                    row1.createCell(6).setCellValue("null");
                    row1.createCell(7).setCellValue("null");
                    row1.createCell(8).setCellValue(debugLog.getTarget());
                    row1.createCell(9).setCellValue("null");
                    row1.createCell(10).setCellValue("null");
                    row1.createCell(11).setCellValue(debugLog.getAction());
                    row1.createCell(12).setCellValue(debugDate);
                    row1.createCell(13).setCellValue(debugTime);

                    rowCount++;
                }
            } else {
                Row row1 = sheet.createRow(rowCount);
                row1.createCell(0).setCellValue(admin.getMid() == null ? modifyUser : admin.getMid());
                row1.createCell(1).setCellValue(loginDate);
                row1.createCell(2).setCellValue(loginTime);
                row1.createCell(3).setCellValue("null");
                row1.createCell(4).setCellValue("null");
                row1.createCell(5).setCellValue("null");
                row1.createCell(6).setCellValue("null");
                row1.createCell(7).setCellValue("null");
                row1.createCell(8).setCellValue("");
                row1.createCell(9).setCellValue("null");
                row1.createCell(10).setCellValue("null");
                row1.createCell(11).setCellValue("");
                row1.createCell(12).setCellValue("");
                row1.createCell(13).setCellValue("");

                rowCount++;
            }
        }
    }

    private void exportSharedCampaign(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<UserTraceLog> logList = userTraceLogService.findByTargetAndAction(
                LOG_TARGET_ACTION_TYPE.TARGET_GameDo.toString(), LOG_TARGET_ACTION_TYPE.ACTION_ShareTrigger.toString(),
                startDate, sdf.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("分享活動名稱");
        row.createCell(2).setCellValue("分享日期");
        row.createCell(3).setCellValue("分享時間");
        row.createCell(4).setCellValue("分享次數");
        row.createCell(5).setCellValue("分享對象");
        row.createCell(6).setCellValue("被分享者點擊次數");
        row.createCell(7).setCellValue("被分享者點擊活動名稱");
        row.createCell(8).setCellValue("被分享者點擊日期");
        row.createCell(9).setCellValue("被分享者點擊時間");

        int rowCount = 1;
        for (UserTraceLog log : logList) {

            String content = log.getContent();
            content = content.substring(content.indexOf("\"") + 1, content.lastIndexOf("\""));
            String gameId = content.substring(content.indexOf(":") + 1);
            ContentGame contentGame = contentGameService.findOne(gameId);

            String gameName = "";
            if (contentGame != null) {
                gameName = contentGame.getGameName();
            }

            String shareDate = sdf2.format(log.getModifyTime()).split(",")[0];
            String shareTime = sdf2.format(log.getModifyTime()).split(",")[1];

            Row row1 = sheet.createRow(rowCount);
            row1.createCell(0).setCellValue(log.getModifyUser());
            row1.createCell(1).setCellValue(gameName);
            row1.createCell(2).setCellValue(shareDate);
            row1.createCell(3).setCellValue(shareTime);
            row1.createCell(4).setCellValue("null");
            row1.createCell(5).setCellValue("null");
            row1.createCell(6).setCellValue("null");
            row1.createCell(7).setCellValue("null");
            row1.createCell(8).setCellValue("null");
            row1.createCell(9).setCellValue("null");
            rowCount++;
        }
    }

    private void exportInformation(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf2.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<LineUser> lineUsers = lineUserService.findByCreateTime(startDate, sdf2.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("加入好友日期");
        row.createCell(2).setCellValue("加入好友時間");
        row.createCell(3).setCellValue("綁定日期");
        row.createCell(4).setCellValue("綁定時間");
        row.createCell(5).setCellValue("封鎖日期");
        row.createCell(6).setCellValue("封鎖時間");

        for (int i = 0; i < lineUsers.size(); i++) {
            LineUser lineUser = lineUsers.get(i);

            String createDateTime = sdf.format(lineUser.getCreateTime());
            String createDate = createDateTime.split(",")[0];
            String createTime = createDateTime.split(",")[1];

            List<UserTraceLog> userTraceLogs = userTraceLogService.findByModifyUserAndAction(lineUser.getMid(),
                    LOG_TARGET_ACTION_TYPE.ACTION_Binded.toString());
            UserTraceLog userTraceLog = null;
            if (userTraceLogs != null && userTraceLogs.size() > 0) {
                userTraceLog = userTraceLogs.get(0);
            }

            String bindedDate = "";
            String bindedTime = "";
            if (userTraceLog != null) {
                String bindedDateTime = sdf.format(userTraceLog.getModifyTime());
                bindedDate = bindedDateTime.split(",")[0];
                bindedTime = bindedDateTime.split(",")[1];
            }

            String blockDate = "";
            String blockTime = "";
            if (LineUser.STATUS_BLOCK.equals(lineUser.getStatus())) {
                UserTraceLog userTraceLog2 = null;
                if (userTraceLog == null) {
                    userTraceLogs = userTraceLogService.findByModifyUserAndAction(lineUser.getMid(),
                            LOG_TARGET_ACTION_TYPE.ACTION_Block.toString());
                } else {
                    userTraceLogs = userTraceLogService.findByModifyUserAndAction(lineUser.getMid(),
                            LOG_TARGET_ACTION_TYPE.ACTION_Binded2Block.toString());
                }

                if (userTraceLogs != null && userTraceLogs.size() > 0) {
                    userTraceLog2 = userTraceLogs.get(0);
                }

                if (userTraceLog2 != null) {
                    String blockDateTime = sdf.format(userTraceLog2.getModifyTime());
                    blockDate = blockDateTime.split(",")[0];
                    blockTime = blockDateTime.split(",")[1];
                }
            }

            Row row1 = sheet.createRow(i + 1);
            row1.createCell(0).setCellValue(lineUser.getMid());
            row1.createCell(1).setCellValue(createDate);
            row1.createCell(2).setCellValue(createTime);
            row1.createCell(3).setCellValue(bindedDate);
            row1.createCell(4).setCellValue(bindedTime);
            row1.createCell(5).setCellValue(blockDate);
            row1.createCell(6).setCellValue(blockTime);
        }
    }

    private void exportInformationS(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date startDateTemp = null;
        Date endDateTemp = null;
        if (StringUtils.isNoneBlank(startDate) && StringUtils.isNoneBlank(endDate)) {
            startDateTemp = sdf.parse(startDate);
            endDateTemp = sdf.parse(endDate);
        } else {
            throw new Exception("時間未輸入");
        }

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("已串聯有效好友數");
        row.createCell(2).setCellValue("未串聯有效好友數");
        row.createCell(3).setCellValue("串聯後封鎖好友數");
        row.createCell(4).setCellValue("新增好友數");
        row.createCell(5).setCellValue("封鎖好友數");
        
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(endDateTemp);
        calendarEnd.add(Calendar.DATE, 1);
        
        Map<String, Long> addCount = recordReportService
                .findRecordReportListByRecordTime(RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString(),
                        RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(), RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_ADD_COUNT.toString(), startDate, sdf.format(calendarEnd.getTime()));
        
        Map<String, Long> blockCount = recordReportService
                .findRecordReportListByRecordTime(RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString(),
                        RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(), RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_BLOCK_COUNT.toString(), startDate, sdf.format(calendarEnd.getTime()));
        
        long msOfDay = 1 * 24 * 60 * 60 * 1000;
        long days = (endDateTemp.getTime() - startDateTemp.getTime()) / msOfDay + 1;

        for (int i = 0; i < days; i++) {
            
            String start = sdf.format(new Date((startDateTemp.getTime() + msOfDay * i)));
            String end = sdf.format(new Date((startDateTemp.getTime() + msOfDay * i + msOfDay)));

            Long bindCount = sendGroupService.countDefaultGroupSize(DEFAULT_SEND_GROUP.BINDED_USER.getGroupId(), "1970-01-01",
                    end);
            Long unbindCount = sendGroupService.countDefaultGroupSize(DEFAULT_SEND_GROUP.UNBIND_USER.getGroupId(),
                    "1970-01-01", end);
            
            Long binded2BlockCount = userTraceLogService.countBinded2Block2("1970-01-01", end);
        
//            int newFriends = lineUserService.findByCreateTime(start, end).size();
//            Long newBlocks =  userTraceLogService.countBinded2Block2(start, end) + userTraceLogService.countBlock(start, end);
                       
            Long newFriends = null;
            if(addCount != null) {
                newFriends = addCount.get(start);
            }
            if(newFriends == null) {
                newFriends = msgBotReceiveService.countReceiveByType(start, end, MsgBotReceive.EVENT_TYPE_FOLLOW);
                recordReportService
                .saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
                        start,
                        RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString(),
                        RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(),
                        RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_ADD_COUNT.toString(),
                        newFriends);
            }
            
            Long newBlocks = null;
            if(blockCount != null) {
                newBlocks = blockCount.get(start);
            }
            if(newBlocks == null) {
                newBlocks = msgBotReceiveService.countReceiveByType(start, end, MsgBotReceive.EVENT_TYPE_UNFOLLOW);
                recordReportService
                .saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
                        start,
                        RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString(),
                        RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(),
                        RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_BLOCK_COUNT.toString(),
                        newBlocks);
            }
            
            Row row1 = sheet.createRow(i + 1);
            row1.createCell(0).setCellValue(start);
            row1.createCell(1).setCellValue(bindCount);
            row1.createCell(2).setCellValue(unbindCount);
            row1.createCell(3).setCellValue(binded2BlockCount);
            row1.createCell(4).setCellValue(newFriends);
            row1.createCell(5).setCellValue(newBlocks);
        }
    }

    private void exportKeyword(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<MsgBotReceive> msgBotList = msgBotReceiveService.findByReceiveDay(startDate,
                sdf.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("輸入關鍵字");
        row.createCell(2).setCellValue("輸入日期");
        row.createCell(3).setCellValue("輸入時間");
        row.createCell(4).setCellValue("回應訊息類型");
        row.createCell(5).setCellValue("回應訊息內容");
        row.createCell(6).setCellValue("回應日期");
        row.createCell(7).setCellValue("回應時間");

        Map<String, Object[]> tempMap = new HashMap<>();
        MsgInteractiveMain mainMsg = null;
        List<MsgDetail> msgDetails = null;
        
        int rowCount = 1;
        for (MsgBotReceive msgBot : msgBotList) {
            String referenceId = msgBot.getReferenceId();
            
            if(!tempMap.containsKey(referenceId)) {
                mainMsg = msgInteractiveMainService.findOne(Long.valueOf(referenceId));
                msgDetails = msgDetailService.findByMsgIdAndMsgParentType(
                        Long.valueOf(referenceId), MsgInteractiveMain.THIS_PARENT_TYPE);
                
                Object[] array = new Object[2];
                array[0] =  mainMsg;
                array[1] =  msgDetails;

                tempMap.put(referenceId, array);
            }
            
            mainMsg = (MsgInteractiveMain) tempMap.get(referenceId)[0];
            msgDetails = (List<MsgDetail>) tempMap.get(referenceId)[1];

            if (mainMsg != null && msgDetails != null && msgDetails.size() > 0) {

                for (MsgDetail msgDetail : msgDetails) {
                    Row row1 = sheet.createRow(rowCount);
                    row1.createCell(0).setCellValue(msgBot.getSourceId());
                    row1.createCell(1).setCellValue(mainMsg.getMainKeyword());
                    row1.createCell(2).setCellValue(msgBot.getReceiveDay());
                    row1.createCell(3).setCellValue(sdf2.format(msgBot.getReceiveTime()));
                    row1.createCell(4).setCellValue(msgDetail.getMsgType());
                    row1.createCell(5).setCellValue(msgDetail.getText());
                    row1.createCell(6).setCellValue(msgBot.getReceiveDay());
                    row1.createCell(7).setCellValue(sdf2.format(msgBot.getReceiveTime()));
                    rowCount++;
                }
            }
        }
    }

    private void exportKeywordUid(Workbook wb, Sheet sheet, String keyword, String startDate, String endDate)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> iMsgIds = msgInteractiveMainService.findIMsgIdByKeyword(keyword);

        Date timeEnd = sdf.parse(endDate);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(timeEnd);
        calendarEnd.add(Calendar.DATE, 1);

        String title = "關鍵字:" + keyword;
        String time = startDate + "~" + endDate;

        List<String> titles = new ArrayList<String>();
        List<List<String>> data = new ArrayList<List<String>>();

        // List<String> unbind
        // =msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(),
        // startDate, sdf.format(calendarEnd.getTime()), LineUser.STATUS_UNBIND);
        // data.add(unbind);
        // titles.add("一般使用者");
        // List<String> binded =
        // msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(),
        // startDate, sdf.format(calendarEnd.getTime()), LineUser.STATUS_BINDED);
        // data.add(binded);
        // titles.add("升級使用者");

        // exportToExcelForMid.exportMidToExcel(wb, sheet, title, time, titles, data);
    }

    private void exportKeywordS(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<String> referenceIdList = msgBotReceiveService.findReferenceId(startDate,
                sdf.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("回覆日期");
        row.createCell(1).setCellValue("關鍵字");
        row.createCell(2).setCellValue("一般使用者回應次數");
        row.createCell(3).setCellValue("一般使用者回應人數");
        row.createCell(4).setCellValue("升級使用者回應次數");
        row.createCell(5).setCellValue("升級使用者回應人數");

        Date startDateTemp = sdf.parse(startDate);
        Date endDateTemp = sdf.parse(endDate);

        long msOfDay = 1 * 24 * 60 * 60 * 1000;
        long days = (endDateTemp.getTime() - startDateTemp.getTime()) / msOfDay + 1;

        int rowCount = 1;

        for (int i = 0; i < days; i++) {
            String start = sdf.format(new Date((startDateTemp.getTime() + msOfDay * i)));
            String end = sdf.format(new Date((startDateTemp.getTime() + msOfDay * i + msOfDay)));
            for (String referenceId : referenceIdList) {
                String keyword = "";
                MsgInteractiveMain main = msgInteractiveMainService.findOne(Long.valueOf(referenceId));
                if (main != null) {
                    keyword = main.getMainKeyword();
                }

                List<Object[]> unbindCount = msgBotReceiveService.countReceiveByReferenceIdAndStatus(referenceId, start,
                        end, LineUser.STATUS_UNBIND);
                List<Object[]> bindedCount = msgBotReceiveService.countReceiveByReferenceIdAndStatus(referenceId, start,
                        end, LineUser.STATUS_BINDED);

                Row row1 = sheet.createRow(rowCount);
                row1.createCell(0).setCellValue(start);
                row1.createCell(1).setCellValue(keyword);
                row1.createCell(2).setCellValue((Integer) unbindCount.get(0)[0]);
                row1.createCell(3).setCellValue((Integer) unbindCount.get(0)[1]);
                row1.createCell(4).setCellValue((Integer) bindedCount.get(0)[0]);
                row1.createCell(5).setCellValue((Integer) bindedCount.get(0)[1]);
                rowCount++;
            }
        }
    }

    private void exportMarketingUrl(Sheet sheet, String startDate, String endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf2.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<UserTraceLog> logList = userTraceLogService.findByTargetAndAction("ContentLink", "ClickLink", startDate,
                sdf2.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("點擊連結名稱");
        row.createCell(2).setCellValue("連結點擊日期");
        row.createCell(3).setCellValue("連結點擊時間");

        Map<String, UserTraceLog> logMap = new LinkedHashMap<>();

        for (UserTraceLog log : logList) {
            List<ContentLink> linkList = contentLinkService.findByLinkUrl(log.getContent());
            if (linkList != null && linkList.size() > 0) {
                logMap.put(linkList.get(0).getLinkTitle(), log);
            }
        }

        int rowCount = 1;
        for (Map.Entry<String, UserTraceLog> entry : logMap.entrySet()) {

            String linkTitle = entry.getKey();
            UserTraceLog log = entry.getValue();

            String dateTime = sdf.format(log.getModifyTime());
            String date = dateTime.split(",")[0];
            String time = dateTime.split(",")[1];

            Row row1 = sheet.createRow(rowCount);
            row1.createCell(0).setCellValue(log.getModifyUser());
            row1.createCell(1).setCellValue(linkTitle);
            row1.createCell(2).setCellValue(date);
            row1.createCell(3).setCellValue(time);

            rowCount++;
        }
    }

    private void exportMarketingUrlUid(Sheet sheet, String startDate, String endDate, String linkUrl) throws Exception {

        if (StringUtils.isNotBlank(linkUrl)) {
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("點擊連結:" + linkUrl);
            logger.info("linkUrl:" + linkUrl);

            List<ContentLink> list = contentLinkService.findByLinkUrl(linkUrl);

            if (list == null || list.size() == 0) {
                throw new Exception("linkUrl Error");
            }

            if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date timeStart = sdf.parse(startDate);

                Date timeEnd = sdf.parse(endDate);
                Calendar calendarEnd = Calendar.getInstance();
                calendarEnd.setTime(timeEnd);
                calendarEnd.add(Calendar.DATE, 1);

                List<String> clickLinkMids = contentLinkService.findClickMidByLinkUrlAndTime(linkUrl,
                        sdf.format(timeStart), sdf.format(calendarEnd.getTime()));

                if (clickLinkMids != null) {
                    Row row1 = sheet.createRow(1);
                    row1.createCell(0).setCellValue("點擊人MID:" + clickLinkMids.size());

                    for (int i = 1; i <= clickLinkMids.size(); i++) {
                        Row row2 = sheet.createRow(i + 1);
                        row2.createCell(0).setCellValue(clickLinkMids.get(i - 1));
                    }

                    return;
                }
            }
        }
        throw new Exception("資料產生錯誤");

    }

    private void exportMarketingUrlS(Sheet sheet, String startDate, String endDate) throws Exception {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(sdf.parse(endDate));
        calendarEnd.add(Calendar.DATE, 1);

        List<UserTraceLog> logList = userTraceLogService.findByTargetAndAction("ContentLink", "ClickLink", startDate,
                sdf.format(calendarEnd.getTime()));

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("連結說明");
        row.createCell(2).setCellValue("連結");
        row.createCell(3).setCellValue("點擊次數");
        row.createCell(4).setCellValue("點擊人數");

        Set<LinkClickReportModel> linkResult = new TreeSet<LinkClickReportModel>(
                new Comparator<LinkClickReportModel>() {

                    @Override
                    public int compare(LinkClickReportModel o1, LinkClickReportModel o2) {
                        if (o1.getLinkTime().equals(o2.getLinkTime()) && o1.getLinkUrl().equals(o2.getLinkUrl())) {
                            return 0;
                        }
                        try {
                            if (sdf.parse(o1.getLinkTime()).getTime() > sdf.parse(o2.getLinkTime()).getTime()) {
                                return 1;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return -1;
                    }
                });

        for (UserTraceLog log : logList) {
            List<ContentLink> linkList = contentLinkService.findByLinkUrl(log.getContent());
            if (linkList != null && linkList.size() > 0) {
                LinkClickReportModel model = new LinkClickReportModel();
                model.setLinkUrl(linkList.get(0).getLinkUrl());
                model.setLinkId(linkList.get(0).getLinkId());
                model.setLinkTitle(linkList.get(0).getLinkTitle());
                model.setLinkTime(log.getModifyDay());
                linkResult.add(model);
            }
        }

        long msOfDay = 1 * 24 * 60 * 60 * 1000;

        for (LinkClickReportModel model : linkResult) {
            String end = sdf.format(new Date((sdf.parse(model.getLinkTime()).getTime() + msOfDay)));
            this.setLinkClickCount(model, model.getLinkTime(), end);
        }

        int rowCount = 1;
        for (LinkClickReportModel model : linkResult) {
            Row row1 = sheet.createRow(rowCount);
            row1.createCell(0).setCellValue(model.getLinkTime());
            row1.createCell(1).setCellValue(model.getLinkTitle());
            row1.createCell(2).setCellValue(model.getLinkUrl());
            row1.createCell(3).setCellValue(model.getTotalCount());
            row1.createCell(4).setCellValue(model.getUserCount());
            rowCount++;
        }
    }

    private void setLinkClickCount(LinkClickReportModel model, String start, String end) throws Exception {

        // Get Link Click Count
        AtomicLong totalCount = new AtomicLong(0);
        AtomicLong userCount = new AtomicLong(0);

        // Get Click Count
        List<Object[]> list = contentLinkService.countClickCountByLinkUrlAndTime(model.getLinkUrl(), start, end);

        if (list != null) {
            for (Object[] objArray : list) {
                totalCount.addAndGet(DBResultUtil.caseCountResult(objArray[0], false).longValue());
                userCount.addAndGet(DBResultUtil.caseCountResult(objArray[1], false).longValue());
            }
        }

        model.setTotalCount(totalCount.longValue());
        model.setUserCount(userCount.longValue());
    }
}
