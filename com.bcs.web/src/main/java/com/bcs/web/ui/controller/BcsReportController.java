package com.bcs.web.ui.controller;

import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.service.ContentReportService;
import com.bcs.core.db.service.PushMessageRecordService;
import com.bcs.core.db.service.PushReportService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.ExportExcelUIService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ???
 * @author Alan
 */
@Slf4j
@Controller
@RequestMapping("/bcs")
public class BcsReportController extends BCSBaseController {
    private ContentReportService contentReportService;
    private PushReportService pushReportService;
    private ExportExcelUIService exportExcelUiService;
    private UserTraceLogService userTraceLogService;
    private PushMessageRecordService pushMessageRecordService;

    @Autowired
    public BcsReportController(ContentReportService contentReportService, PushReportService pushReportService, ExportExcelUIService exportExcelUiService, UserTraceLogService userTraceLogService, PushMessageRecordService pushMessageRecordService) {
        this.contentReportService = contentReportService;
        this.pushReportService = pushReportService;
        this.exportExcelUiService = exportExcelUiService;
        this.userTraceLogService = userTraceLogService;
        this.pushMessageRecordService = pushMessageRecordService;
    }

    @GetMapping(value = "/admin/reportPage")
    public String reportPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportPage");
        return BcsPageEnum.ReportPage.toString();
    }

    @GetMapping(value = "/admin/reportCreatePage")
    public String reportCreatePage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportCreatePage");
        return BcsPageEnum.ReportCreatePage.toString();
    }

    @GetMapping(value = "/admin/reportLinkClickPage")
    public String reportLinkClickPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportLinkClickPage");
        return BcsPageEnum.ReportLinkClickPage.toString();
    }

    @GetMapping(value = "/admin/reportLinkClickDetailPage")
    public String reportLinkClickDetailPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportLinkClickDetailPage");
        return BcsPageEnum.ReportLinkClickDetailPage.toString();
    }

    @GetMapping(value = "/admin/reportPageVisitPage")
    public String reportPageVisitPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportPageVisitPage");
        return BcsPageEnum.ReportPageVisitPage.toString();
    }

    @GetMapping(value = "/admin/reportPageVisitDetailPage")
    public String reportPageVisitDetailPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportPageVisitDetailPage");
        return BcsPageEnum.ReportPageVisitDetailPage.toString();
    }

    @GetMapping(value = "/admin/reportPushApiEffectsPage")
    public String reportPushApiEffectsPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportPushApiEffectsPage");
        return BcsPageEnum.ReportPushApiEffectsPage.toString();
    }

    @GetMapping(value = "/edit/keywordAndInteractiveReportPage")
    public String keywordAndInteractiveReportPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("keywordAndInteractiveReportPage");
        return BcsPageEnum.KeywordAndInteractiveReportPage.toString();
    }

    @GetMapping(value = "/admin/reportBNEffectsPage")
    public String reportBnEffectsPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("reportBNEffectsPage");
        return BcsPageEnum.ReportBNEffectsPage.toString();
    }

    @GetMapping(value = "/admin/reportBNEffectsDetailPage")
    public String reportBnEffectsDetailPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("ReportBNEffectsDetailPage");
        return BcsPageEnum.ReportBNEffectsDetailPage.toString();
    }

    /**
     * 取得發送報告清單
     */
    @ControllerLog(description = "取得發送報告清單")
    @GetMapping("/edit/getPushReportList")
    @ResponseBody
    public ResponseEntity<?> getPushReportList(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("getReportListByPushTime");
        try {
            List<List<String>> result = contentReportService.getPushReportList(startDate, endDate);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取得特定發送報告
     */
    @ControllerLog(description = "取得特定發送報告")
    @GetMapping(value = "/edit/getPushReport")
    @ResponseBody
    public ResponseEntity<?> getPushReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam Long reportId, @RequestParam String actionType) {
        log.info("getReport");
        try {
            if ("Edit".equals(actionType)) {
                PushReport result = contentReportService.getPushReportForEdit(reportId);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                Object[] result = contentReportService.getPushReportForDialog(reportId);
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取得圖文訊息發送報告
     */
    @ControllerLog(description = "取得圖文訊息發送報告")
    @GetMapping(value = "/edit/getPushReportForRichMsg")
    @ResponseBody
    public ResponseEntity<?> getPushReportForRichMsg(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String richId) {
        log.info("getRichPushReport");

        try {
            PushReport result = contentReportService.getPushReportForRichMsg(richId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 新增與更新發送報告
     */
    @ControllerLog(description = "新增與更新發送報告")
    @PostMapping(value = "/edit/savePushReport", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> savePushReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestBody PushReport pushReport, @RequestParam String actionType) throws IOException {
        try {
            //取得登入者的帳號
            String adminUserAccount = customUser.getAccount();

            contentReportService.savePushReport(pushReport, actionType, adminUserAccount);

            String result = "建立發送報告成功";
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刪除發送報告
     */
    @ControllerLog(description = "刪除發送報告")
    @DeleteMapping(value = "/admin/deletePushReport")
    @ResponseBody
    public ResponseEntity<?> deletePushReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam Long reportId) throws IOException {
        log.info("deletePushReport");

        // Check Delete Right
        boolean isAdmin = customUser.isAdmin();
        if (isAdmin) {
            try {
                contentReportService.deletePushReport(reportId);
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("User No Delete Right", HttpStatus.OK);
        }
    }

    /**
     * 計算平均點擊率(以週為單位)
     */
    @ControllerLog(description = "計算平均點擊率(以週為單位)")
    @GetMapping(value = "/edit/getClickRateForWeek")
    @ResponseBody
    public ResponseEntity<?> getClickRateForWeek(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("getClickRateForWeek");
        try {
            List<String> result = contentReportService.getClickRateForWeek(startDate, endDate);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 計算平均點擊率(以日為單位)
     */
    @ControllerLog(description = "計算平均點擊率(以日為單位)")
    @GetMapping(value = "/edit/getClickRateForDay")
    @ResponseBody
    public ResponseEntity<?> getClickRateForDay(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("getClickRateForDay");
        try {
            List<Map<String, Map<String, BigDecimal>>> result = contentReportService.getClickRateForDay(startDate, endDate);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ControllerLog(description = "exportToExcelForClickUrl")
    @GetMapping(value = "/edit/exportToExcelForClickUrl")
    @ResponseBody
    public void exportToExcelForClickUrl(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String reportId) throws Exception {
        log.info("exportToExcelForClickUrl");

        if (StringUtils.isNotBlank(reportId)) {
            log.info("reportId:" + reportId);

            PushReport pushReport = pushReportService.findOne(Long.parseLong(reportId));

            String linkId = pushReport.getReportLinkId();
            Date pushTime = pushReport.getReportPushTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String pushTimeStr = sdf.format(pushTime);
            String nowStr = sdf.format(new Date());

            List<String> clickLinkMidList = userTraceLogService.findMidByReferenceId(linkId, pushTimeStr);

            String title = pushReport.getReportPushUrl();

            if (clickLinkMidList != null) {

                List<String> titles = new ArrayList<>();
                titles.add("點擊人UID");
                List<List<String>> data = new ArrayList<>();
                data.add(clickLinkMidList);

                String time = pushTimeStr + "~" + nowStr;
                exportExcelUiService.exportMidResultToExcel(request, response, "ReportClickUrl", "點擊連結:" + title, time, titles, data);
                return;
            }
        }
        throw new Exception("資料產生錯誤");
    }

    @ControllerLog(description = "getPushApiEffects")
    @GetMapping(value = "/edit/getPushApiEffects")
    @ResponseBody
    public ResponseEntity<?> getPushApiEffects(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "startDate", required = true) String startDate, @RequestParam(value = "endDate", required = true) String endDate) {
        log.info("-------------------- getPushApiEffects --------------------");
        log.info("[getPushApiEffects] startDate: " + startDate);
        log.info("[getPushApiEffects] endDate: " + endDate);

        try {
            return new ResponseEntity<>(pushMessageRecordService.getPushMessageEffects(startDate, endDate), HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
