package com.bcs.web.ui.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.report.builder.ExportExcelBuilder;
import com.bcs.core.report.service.ExportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailReport;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailReportParam;
import com.bcs.core.taishin.circle.pnp.db.service.PnpReportService;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpSMSMsgService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Bcs pnp report controller.
 *
 * @author ???
 * @author Alan
 */
@Slf4j(topic = "PnpRecorder")
@Controller
@RequestMapping("/bcs/pnpEmployee")
public class BcsPnpReportController {
    private final PnpReportService pnpReportService;
    private final PnpSMSMsgService pnpSMSMsgService;

    /**
     * Instantiates a new Bcs pnp report controller.
     */
    @Autowired
    public BcsPnpReportController(final PnpReportService pnpReportService, PnpSMSMsgService pnpSMSMsgService) {
        this.pnpReportService = pnpReportService;
        this.pnpSMSMsgService = pnpSMSMsgService;
    }

    /**
     * Pnp detail report page string.
     *
     * @return the string
     */
    @WebServiceLog
    @GetMapping("/pnpDetailReportPage")
    public String pnpDetailReportPage() {
        log.info("pnpDetailReportPage");
        return BcsPageEnum.PNP_DETAIL_REPORT_PAGE.toString();
    }

    /**
     * Pnp analysis report page string.
     *
     * @return the string
     */
    @WebServiceLog
    @GetMapping("/pnpAnalysisReportPage")
    public String pnpAnalysisReportPage() {
        log.info("pnpAnalysisReportPage");
        return BcsPageEnum.PNP_ANALYSIS_REPORT_PAGE.toString();
    }

    @WebServiceLog
    @PostMapping(value = "/resend/sms", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> reSendSms(@CurrentUser final CustomUser customUser,
                                       HttpServletRequest request,
                                       @RequestParam(required = false) String detailId,
                                       @RequestParam(required = false) String ftpSourceName) {
        request.getParameter("detailId");
        log.info("DetailId : {}, ftpSourceName : {}", detailId, ftpSourceName);
        if (detailId == null || StringUtils.isBlank(ftpSourceName)) {
            return new ResponseEntity<>("Detail Id Or FtpSource Is Blank", HttpStatus.BAD_REQUEST);
        }
        PnpFtpSourceEnum ftpSourceEnum = PnpFtpSourceEnum.valueOf(ftpSourceName);
        long id = Long.parseLong(detailId);
        boolean isSuccess = pnpSMSMsgService.resendSms(id, ftpSourceEnum);
        log.info("isSuccess : {}", isSuccess);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @WebServiceLog
    @PostMapping(value = "/getPNPAnalysisReport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPnpAnalysisReport(@CurrentUser final CustomUser customUser,
                                                  @RequestBody final PnpDetailReportParam param) {
        try {
            param.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpDetailReport> result = pnpReportService.getPnpDetailReportList(customUser, param);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @WebServiceLog
    @PostMapping(value = "/getPNPDetailReport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPnpDetailReport(@CurrentUser final CustomUser customUser,
                                                @RequestBody final PnpDetailReportParam param) {
        try {
            param.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpDetailReport> result = pnpReportService.getPnpDetailReportList(customUser, param);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets pnp detail report total pages.
     *
     * @param customUser the custom user
     * @return the pnp detail report total pages
     */
    @WebServiceLog
    @PostMapping(value = "/getPNPDetailReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPnpDetailReportTotalPages(@CurrentUser final CustomUser customUser,
                                                          @RequestBody final PnpDetailReportParam param) {
        try {
            param.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpDetailReport> result = pnpReportService.getPnpDetailReportList(customUser, param);
            int totalCount = result.size();
            int onePageCount = 10;
            int totalPage = DataUtils.calTotalPage(totalCount, onePageCount);
            return new ResponseEntity<>(totalPage, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog(action = "Download")
    @GetMapping("/exportPNPDetailReportExcel")
    @ResponseBody
    public void exportPnpDetailReportExcel(final HttpServletResponse response, @CurrentUser final CustomUser customUser,
                                           @RequestParam(required = false) final String startDate, @RequestParam(required = false) final String endDate,
                                           @RequestParam(required = false) final boolean isPageable, @RequestParam(required = false) final Integer page,
                                           @RequestParam(required = false) final String account, @RequestParam(required = false) final String pccCode,
                                           @RequestParam(required = false) final String sourceSystem, @RequestParam(required = false) final String employeeId,
                                           @RequestParam(required = false) final String phone, @RequestParam(required = false) final String dateType) {
        try {
            final PnpDetailReportParam pnpDetailReportParam = new PnpDetailReportParam(
                    dateType, DataUtils.convStrToDate(startDate, "yyyy-MM-dd"), DataUtils.convStrToDate(endDate, "yyyy-MM-dd"),
                    isPageable, page, account, pccCode, sourceSystem, employeeId, phone, customUser.getRole()
            );

            pnpDetailReportParam.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpDetailReport> reportList = pnpReportService.getPnpDetailReportList(customUser, pnpDetailReportParam);
            final ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("TestSheet1");

            final List<Map<Integer, String>> allMapList = new LinkedList<>();
            allMapList.add(getHeaderMap(29));
            reportList.forEach(r -> allMapList.add(getBodyMap(r, 29)));
            allMapList.forEach(rowDate -> builder.createRow(allMapList.indexOf(rowDate)).setRowValue(rowDate));

            builder.setAllColumnAutoWidth().setOutputPath(CoreConfigReader.getString("file.path"))
                    .setOutputFileName(String.format("PNPDetailReport_%s.xlsx",
                            DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));
            log.info("Builder: {}", DataUtils.toPrettyJsonUseJackson(builder));
            final ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
        } catch (final Exception e) {
            log.error("Exception", e);
        }

    }

    private Map<Integer, String> getBodyMap(final PnpDetailReport r, final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        if (r.getMainId() != null && r.getDetailId() != null) {
            row.put(0, r.getMainId() + "." + r.getDetailId());
        } else {
            row.put(0, "");
        }
        row.put(1, r.getSourceSystem());
        row.put(2, r.getProcessFlow());
        row.put(3, r.getProcessStage() + '_' + r.getFtpSource());
        row.put(4, r.getAccount());
        row.put(5, r.getPccCode());
        if (r.getMainId() != null) {
            row.put(6, Long.toString(r.getMainId()));
        } else {
            row.put(6, "");
        }
        row.put(7, r.getSn());
        if (r.getTemplateId() != null) {
            row.put(8, Long.toString(r.getTemplateId()));
        } else {
            row.put(8, "");
        }
        row.put(9, r.getMessage());
        row.put(10, r.getMessagePoint() == null ? null : r.getMessagePoint().toString());
        row.put(11, r.getCampaignId());
        row.put(12, r.getSegmentId());
        row.put(13, r.getProgramId());
        row.put(14, r.getPid());
        row.put(15, DataUtils.maskString(r.getPhone(), '*', 2, 2));
        row.put(16, r.getUid());
        row.put(17, DataUtils.convDateToStr(r.getScheduleTime(), "yyyy-MM-dd HH:mm:ss"));
        row.put(18, DataUtils.convDateToStr(r.getBcTime(), "yyyy-MM-dd HH:mm:ss"));
        row.put(19, DataUtils.convDateToStr(r.getPnpTime(), "yyyy-MM-dd HH:mm:ss"));
        row.put(20, DataUtils.convDateToStr(r.getSmsTime(), "yyyy-MM-dd HH:mm:ss"));
        row.put(21, r.getBcStatus());
        row.put(22, r.getBcHttpStatusCode());
        row.put(23, r.getPnpStatus());
        row.put(24, r.getPnpHttpStatusCode());
        row.put(25, r.getSmsStatus());
        row.put(26, r.getIsInternational() == null ? null : r.getIsInternational().toString());
        row.put(27, DataUtils.convDateToStr(r.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        row.put(28, DataUtils.convDateToStr(r.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
        return row;
    }

    private Map<Integer, String> getHeaderMap(final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, "序號");
        row.put(1, "前方來源系統");
        row.put(2, "通路流");
        row.put(3, "發送通路");
        row.put(4, "發送帳號");
        row.put(5, "掛帳PccCode");
        row.put(6, "發送廠商訊息批次代碼");
        row.put(7, "發送廠商訊息流水號");
        row.put(8, "訊息樣板");
        row.put(9, "訊息內文");
        row.put(10, "訊息內文點數");
        row.put(11, "行銷活動代碼");
        row.put(12, "行銷活動階段");
        row.put(13, "行銷活動客群代碼");
        row.put(14, "客戶ID");
        row.put(15, "客戶手機號碼");
        row.put(16, "UID");
        row.put(17, "預約時間");
        row.put(18, "BC發送時間");
        row.put(19, "PNP發送時間");
        row.put(20, "SMS發送時間");
        row.put(21, "BC發送狀態");
        row.put(22, "BC-HTTP狀態");
        row.put(23, "PNP發送狀態");
        row.put(24, "PNP-HTTP狀態");
        row.put(25, "SMS發送狀態");
        row.put(26, "是否國際簡訊");
        row.put(27, "資料建立日期");
        row.put(28, "資料更新日期");
        return row;
    }

    /**
     * Get Status Map
     *
     * @return status map
     */
    @WebServiceLog(action = "getConfig")
    @ResponseBody
    @GetMapping("/getPnpStatusEnum")
    public ResponseEntity<?> getPnpStatusEnum() {
        try {
            final Map<String, String> map = new HashMap<>(PnpStatusEnum.values().length);
            for (final PnpStatusEnum e : PnpStatusEnum.values()) {
                map.put(e.value, e.chinese);
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
