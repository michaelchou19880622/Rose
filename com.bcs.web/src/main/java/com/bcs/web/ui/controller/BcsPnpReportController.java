package com.bcs.web.ui.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.report.builder.ExportExcelBuilder;
import com.bcs.core.report.service.ExportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PNPBlockGTag;
import com.bcs.core.taishin.circle.pnp.db.entity.PNPBlockHistoryList;
import com.bcs.core.taishin.circle.pnp.db.entity.PNPBlockSendList;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailReport;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailReportParam;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpSendBlockParam;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpStsRptDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpStsRptParam;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpStsRptSummary;
import com.bcs.core.taishin.circle.pnp.db.service.PnpReportService;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpSMSMsgService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * Bcs pnp report controller.
 *
 * @author ???
 * @author Alan
 */
@Slf4j//(topic = "PnpRecorder")
@Controller
@RequestMapping("/bcs/pnpEmployee")
public class BcsPnpReportController {
	
    @Autowired
    private final PnpReportService pnpReportService;
    
    @Autowired
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
    public ResponseEntity<?> reSendSms(HttpServletRequest request, HttpServletResponse response,
    									@CurrentUser final CustomUser customUser,
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
    
    /**
     * Gets pnp summary report pages.
     * @Author Ean
     * @param customUser the custom user
     * @return the pnp summary report pages
     */
    @WebServiceLog
    @PostMapping(value = "/getPNPStsRptSummary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPStsRptSummary(HttpServletRequest request, HttpServletResponse response,
    		@CurrentUser final CustomUser customUser, @RequestBody final PnpStsRptParam param) {

        try {
            param.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpStsRptSummary> result = pnpReportService.getPnpStsRptSummaryList(customUser, param);
        	log.info("result = {}", result);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
//            log.info("Total date count for summary:" + result.get(0).getDate_count());
//            long totalPage = (long) Math.ceil(result.get(0).getDate_count() / Double.valueOf(param.getPageCount()));
            //long totalPage = 10;
            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @PostMapping(value = "/getPNPStsRptDetail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPStsRptDetail(HttpServletRequest request, HttpServletResponse response,
    		@CurrentUser final CustomUser customUser, @RequestBody final PnpStsRptParam param) {

    	log.info("param.getStartDate() = {}", param.getStartDate());
    	log.info("param.getEndDate() = {}", param.getEndDate());
    	log.info("param.getDateType() = {}", param.getDateType());
    	log.info("param.getPage() = {}", param.getPage());
    	log.info("param.getPageCount() = {}", param.getPageCount());
    	
        try {
            param.setEmployeeId(customUser.getAccount().toUpperCase());
            final List<PnpStsRptDetail> result = pnpReportService.getPnpStsRptDetailList(customUser, param);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @WebServiceLog
    @PostMapping(value = "/getPNPAnalysisReport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPnpAnalysisReport(HttpServletRequest request, HttpServletResponse response,
    		@CurrentUser final CustomUser customUser, @RequestBody final PnpDetailReportParam param) {
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
    public ResponseEntity<?> getPnpDetailReport(HttpServletRequest request, HttpServletResponse response,
    		@CurrentUser final CustomUser customUser, @RequestBody final PnpDetailReportParam param) {
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
    public ResponseEntity<?> getPnpDetailReportTotalPages(HttpServletRequest request, HttpServletResponse response,
    		@CurrentUser final CustomUser customUser, @RequestBody final PnpDetailReportParam param) {
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
    @GetMapping("/exportPNPStsReportExcel")
    @ResponseBody
    public void exportPNPStsReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser final CustomUser customUser,
    									@RequestParam String dateType,
                                        @RequestParam String startDate, 
                                        @RequestParam String endDate,
        								@RequestParam boolean isPageable, 
        								@RequestParam Integer page,
        								@RequestParam String account, 
        								@RequestParam String pccCode,
        								@RequestParam String sourceSystem, 
        								@RequestParam String employeeId,
        								@RequestParam String phone,
        								@RequestParam String pageCount) {
    	
    	log.info("dateType = {}", dateType);
    	log.info("startDate = {}", DataUtils.convStrToDate(startDate, "yyyy-MM-dd"));
    	log.info("endDate = {}", DataUtils.convStrToDate(endDate, "yyyy-MM-dd"));
    	log.info("isPageable = {}", isPageable);
    	log.info("page = {}", page);
    	log.info("account = {}", account);
    	log.info("pccCode = {}", pccCode);
    	log.info("sourceSystem = {}", sourceSystem);
    	log.info("employeeId = {}", employeeId);
    	log.info("phone = {}", phone);
    	log.info("pageCount = {}", pageCount);
    	
        try {
        	PnpStsRptParam param = new PnpStsRptParam();
        	param.setDateType(dateType);
        	param.setStartDate(DataUtils.convStrToDate(startDate, "yyyy-MM-dd"));
        	param.setEndDate(DataUtils.convStrToDate(endDate, "yyyy-MM-dd"));
        	param.setPageable(isPageable);
        	param.setPage(page);
        	param.setAccount(account);
        	param.setPccCode(pccCode);
        	param.setSourceSystem(sourceSystem);
        	param.setEmployeeId(employeeId);
        	param.setPhone(phone);
        	param.setPageCount(pageCount);
        	param.setEmployeeId(customUser.getAccount().toUpperCase());

            final ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("PNPStatisticsReport");

            final List<Map<Integer, String>> allMapList = new LinkedList<>();
            final Map<Integer, String> emptyRow = new LinkedHashMap<>(15);
            emptyRow.put(0, " ");
            emptyRow.put(1, " ");
            emptyRow.put(2, " ");
            emptyRow.put(3, " ");
            emptyRow.put(4, " ");
            emptyRow.put(5, " ");
            emptyRow.put(6, " ");
            emptyRow.put(7, " ");
            emptyRow.put(8, " ");
            emptyRow.put(9, " ");
            emptyRow.put(10, " ");
            emptyRow.put(11, " ");
            emptyRow.put(12, " ");
            emptyRow.put(13, " ");
            emptyRow.put(14, " ");
            
            
            // Summary
            final List<PnpStsRptSummary> summaryReportList = pnpReportService.getPnpStsRptSummaryList(customUser, param);
            allMapList.add(getStsSummaryHeaderMap(15));
            summaryReportList.forEach(r -> allMapList.add(getStsSummaryBodyMap(r, 15)));
            allMapList.add(emptyRow);

            // Detail
            final List<PnpStsRptDetail> detailReportList = pnpReportService.getPnpStsRptDetailList(customUser, param);
            allMapList.add(getStsDetailHeaderMap(15));
            detailReportList.forEach(r -> allMapList.add(getStsBodyMap(r, 15)));
            
            allMapList.forEach(rowData -> builder.createRow(allMapList.indexOf(rowData)).setRowValue(rowData));

            builder.setAllColumnAutoWidth()
            	   .setOutputPath(CoreConfigReader.getString("file.path"))
                   .setOutputFileName(String.format("PNPStsRptDetail_%s.xlsx", DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));
            log.info("Builder: {}", DataUtils.toPrettyJsonUseJackson(builder));
            
            final ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
            
        } catch (final Exception e) {
            log.error("Exception", e);
        }
    }

    private Map<Integer, String> getStsSummaryBodyMap(final PnpStsRptSummary pnpStsRptSummary, final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);

        row.put(0, String.valueOf(pnpStsRptSummary.getDate_count()));
        row.put(1, String.valueOf(pnpStsRptSummary.getTotal()));

        row.put(2, String.valueOf(pnpStsRptSummary.getBc_total()));
        row.put(3, String.valueOf(pnpStsRptSummary.getBc_ok()));
        row.put(4, String.valueOf(pnpStsRptSummary.getBc_no()));
        row.put(5, String.valueOf(pnpStsRptSummary.getBc_rate()));

        row.put(6, String.valueOf(pnpStsRptSummary.getPnp_total()));
        row.put(7, String.valueOf(pnpStsRptSummary.getPnp_ok()));
        row.put(8, String.valueOf(pnpStsRptSummary.getPnp_no()));
        row.put(9, String.valueOf(pnpStsRptSummary.getPnp_rate()));

        row.put(10, String.valueOf(pnpStsRptSummary.getSms_total()));
        row.put(11, String.valueOf(pnpStsRptSummary.getSms_ok()));
        row.put(12, String.valueOf(pnpStsRptSummary.getSms_no()));
        row.put(13, String.valueOf(pnpStsRptSummary.getSms_rate()));
        row.put(14, String.valueOf(pnpStsRptSummary.getSms_point()));

        return row;
    }
    
    private Map<Integer, String> getStsBodyMap(final PnpStsRptDetail pnpStsRptDetail, final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);

        row.put(0, String.valueOf(pnpStsRptDetail.getSend_date()));
        row.put(1, String.valueOf(pnpStsRptDetail.getTotal()));

        row.put(2, String.valueOf(pnpStsRptDetail.getBc_total()));
        row.put(3, String.valueOf(pnpStsRptDetail.getBc_ok()));
        row.put(4, String.valueOf(pnpStsRptDetail.getBc_no()));
        row.put(5, String.valueOf(pnpStsRptDetail.getBc_rate()));

        row.put(6, String.valueOf(pnpStsRptDetail.getPnp_total()));
        row.put(7, String.valueOf(pnpStsRptDetail.getPnp_ok()));
        row.put(8, String.valueOf(pnpStsRptDetail.getPnp_no()));
        row.put(9, String.valueOf(pnpStsRptDetail.getPnp_rate()));

        row.put(10, String.valueOf(pnpStsRptDetail.getSms_total()));
        row.put(11, String.valueOf(pnpStsRptDetail.getSms_ok()));
        row.put(12, String.valueOf(pnpStsRptDetail.getSms_no()));
        row.put(13, String.valueOf(pnpStsRptDetail.getSms_rate()));
        row.put(14, String.valueOf(pnpStsRptDetail.getSms_point()));

        return row;
    }
    
    private Map<Integer, String> getStsSummaryHeaderMap(final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, "發送天數(總計)");
        row.put(1, "總通數");
        row.put(2, "BC通路總通數");
        row.put(3, "BC成功通數");
        row.put(4, "BC失敗通數");
        row.put(5, "BC成功率");
        row.put(6, "PNP通路總通數");
        row.put(7, "PNP成功通數");
        row.put(8, "PNP失敗通數");
        row.put(9, "PNP成功率");
        row.put(10, "SMS通路總通數");
        row.put(11, "SMS成功通數");
        row.put(12, "SMS失敗通數");
        row.put(13, "SMS成功率");
        row.put(14, "SMS成功點數");

        return row;
    }


    private Map<Integer, String> getStsDetailHeaderMap(final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, "發送日期");
        row.put(1, "總通數");
        row.put(2, "BC通路總通數");
        row.put(3, "BC成功通數");
        row.put(4, "BC失敗通數");
        row.put(5, "BC成功率");
        row.put(6, "PNP通路總通數");
        row.put(7, "PNP成功通數");
        row.put(8, "PNP失敗通數");
        row.put(9, "PNP成功率");
        row.put(10, "SMS通路總通數");
        row.put(11, "SMS成功通數");
        row.put(12, "SMS失敗通數");
        row.put(13, "SMS成功率");
        row.put(14, "SMS成功點數");

        return row;
    }

    @WebServiceLog(action = "Download")
    @GetMapping("/exportPNPDetailReportExcel")
    @ResponseBody
    public void exportPnpDetailReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser final CustomUser customUser,
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
    
    /* PNP黑名單管理 - 排除發送中名單列表頁面 */
    @WebServiceLog
    @GetMapping("/pnpExcludeSendingListPage")
    public String pnpExcludeSendingListPage() {
        log.debug("pnpExcludeSendingListPage");
        return BcsPageEnum.PNP_EXCLUDE_SENDING_LIST_PAGE.toString();
    }
    
    /* PNP黑名單管理 - 排除發送名單單筆用戶歷程紀錄頁面 */
    @WebServiceLog
    @GetMapping("/pnpExcludeSendingSingleUserHistoryPage")
    public String pnpExcludeSendingSingleUserHistoryPage() {
        log.debug("pnpExcludeSendingSingleUserHistoryPage");
        return BcsPageEnum.PNP_EXCLUDE_SENDING_SINGLE_USER_HISTORY_PAGE.toString();
    }
    
    /* PNP黑名單管理 - 排除發送名單歷程列表頁面 */
    @WebServiceLog
    @GetMapping("/pnpExcludeSendingListHistoryPage")
    public String pnpExcludeSendingListHistoryPage() {
        log.debug("pnpExcludeSendingListHistoryPage");
        return BcsPageEnum.PNP_EXCLUDE_SENDING_LIST_HISTORY_PAGE.toString();
    }

	/* 取得排除發送名單總數 */
	@WebServiceLog
	@PostMapping(value = "/getPnpBlockSendCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPnpBlockSendCount(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("getPnpBlockSendCount");
		log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final Long pnpBlockSendCount = pnpReportService.getPnpBlockSendCount(customUser, pnpSendBlockParam);
			log.info("pnpBlockSendCount = {}", pnpBlockSendCount);
			return new ResponseEntity<>(pnpBlockSendCount, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
	/* 取得排除發送名單列表 */
	@WebServiceLog
	@PostMapping(value = "/getPnpExcludeSendingList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPnpExcludeSendingList(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("getPnpExcludeSendingList");
        log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final List<PNPBlockSendList> result = pnpReportService.qryPnpBlockSendList(customUser, pnpSendBlockParam);
//			log.info(DataUtils.toPrettyJsonUseJackson(result));
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/* 新增/取消排除發送名單狀態 */
	@WebServiceLog
	@PostMapping(value = "/updPnpBlockSend", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> updPnpBlockSend(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("updPnpBlockSend");
        log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final Long historyRefId = pnpReportService.updPnpBlockSend(customUser, pnpSendBlockParam);
			log.info("historyRefId = {}", historyRefId);
			return new ResponseEntity<>(historyRefId, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception = {}", e);
			
			if (e instanceof SQLException) {
				log.error("SQLException = {}", e);
				
				if (e.toString().contains("duplicate key")) {
					return new ResponseEntity<>("duplicate key", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/* 取得客群標籤列表 */
	@WebServiceLog
	@PostMapping(value = "/qryPNPBlockGTagList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> qryPNPBlockGTagList(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("qryPNPBlockGTagList");
        log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final List<PNPBlockGTag> lstPnpBlockGTags = pnpReportService.qryPNPBlockGTagList(customUser, pnpSendBlockParam);
			
			return new ResponseEntity<>(lstPnpBlockGTags, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/* 匯出排除發送中名單列表EXCEL */
	@WebServiceLog(action = "Download")
    @GetMapping("/exportPNPBlockListReportExcel")
    @ResponseBody
    public void exportPNPBlockListReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser final CustomUser customUser, 
    			@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate,
				@RequestParam(required = false) final String mobile, @RequestParam(required = false) final String insertUser,
				@RequestParam(required = false) final String groupTag) {
		
		log.info("exportPNPBlockListReportExcel");
		
		try { 
	        
	        final PnpSendBlockParam pnpSendBlockParam = new PnpSendBlockParam();
	        pnpSendBlockParam.setPage(1);
	        pnpSendBlockParam.setStartDate(DataUtils.convStrToDate(startDate, "yyyy-MM-dd"));
	        pnpSendBlockParam.setEndDate(DataUtils.convStrToDate(endDate, "yyyy-MM-dd"));
	        pnpSendBlockParam.setMobile(mobile);
	        pnpSendBlockParam.setInsertUser(insertUser);
	        pnpSendBlockParam.setGroupTag(groupTag);
	        pnpSendBlockParam.setRole(customUser.getRole());

	        log.info("getPnpBlockSendCount : pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
	        
			final Long pnpBlockSendCount = pnpReportService.getPnpBlockSendCount(customUser, pnpSendBlockParam);
			log.info("pnpBlockSendCount = {}", pnpBlockSendCount);
			
	        pnpSendBlockParam.setPageCount(Integer.valueOf(pnpBlockSendCount.intValue()));
	        
	        log.info("qryPnpBlockSendList : pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
			
			final List<PNPBlockSendList> result = pnpReportService.qryPnpBlockSendList(customUser, pnpSendBlockParam);
			log.info(DataUtils.toPrettyJsonUseJackson(result));
			
			final ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("PNPBlockSendList");
			
            final List<Map<Integer, String>> allMapList = new ArrayList<Map<Integer,String>>();
            allMapList.add(getPnpBlockHeaderMap(7));
            result.forEach(r -> allMapList.add(getPnpBlockBodyMap(r, 7)));
			log.info(DataUtils.toPrettyJsonUseJackson(allMapList));
            
            allMapList.forEach(rowData -> builder.createRow(allMapList.indexOf(rowData)).setRowValue(rowData));

            builder.setAllColumnAutoWidth().setOutputPath(CoreConfigReader.getString("file.path"))
                    .setOutputFileName(String.format("PNPBlockSendListReport_%s.xlsx", DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));
            log.info("Builder: {}", DataUtils.toPrettyJsonUseJackson(builder));
            
            final ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
		} catch (final Exception e) {
			log.error("Exception", e);
		}
    }
	
	/* 匯出排除發送中名單歷程記錄列表EXCEL */
	@WebServiceLog(action = "Download")
    @GetMapping("/exportPNPBlockListHistoryReportExcel")
    @ResponseBody
    public void exportPNPBlockListHistoryReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser final CustomUser customUser, 
    			@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate,
				@RequestParam(required = false) final String mobile, @RequestParam(required = false) final String insertUser,
				@RequestParam(required = false) final String groupTag, @RequestParam(required = false) final int blockEnable) {
		
		log.info("exportPNPBlockListHistoryReportExcel");
		
		try { 
			final PnpSendBlockParam pnpSendBlockParam = new PnpSendBlockParam();
	        pnpSendBlockParam.setPage(1);
	        pnpSendBlockParam.setStartDate(DataUtils.convStrToDate(startDate, "yyyy-MM-dd"));
	        pnpSendBlockParam.setEndDate(DataUtils.convStrToDate(endDate, "yyyy-MM-dd"));
	        pnpSendBlockParam.setMobile(mobile);
	        pnpSendBlockParam.setInsertUser(insertUser);
	        pnpSendBlockParam.setGroupTag(groupTag);
	        pnpSendBlockParam.setBlockEnable(blockEnable);
	        pnpSendBlockParam.setRole(customUser.getRole());
	        log.info("getPnpBlockHistoryCount : pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
			
			final Long pnpBlockHistoryCount = pnpReportService.getPnpBlockHistoryCount(customUser, pnpSendBlockParam);
			log.info("pnpBlockHistoryCount = {}", pnpBlockHistoryCount);
			
	        pnpSendBlockParam.setPageCount(Integer.valueOf(pnpBlockHistoryCount.intValue()));

	        log.info("qryPnpBlockHistoryList : pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
			
			final List<PNPBlockHistoryList> result = pnpReportService.qryPnpBlockHistoryList(customUser, pnpSendBlockParam);
			log.info(DataUtils.toPrettyJsonUseJackson(result));
			
			final ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("PNPBlockListHistory");

            final List<Map<Integer, String>> allMapList = new LinkedList<>();
            allMapList.add(getPnpBlockHeaderMap(7));
            result.forEach(r -> allMapList.add(getPnpBlockHistoryBodyMap(r, 7)));
            allMapList.forEach(rowData -> builder.createRow(allMapList.indexOf(rowData)).setRowValue(rowData));

            builder.setAllColumnAutoWidth().setOutputPath(CoreConfigReader.getString("file.path"))
                    .setOutputFileName(String.format("PNPBlockListHistoryReport_%s.xlsx", DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));
            log.info("Builder: {}", DataUtils.toPrettyJsonUseJackson(builder));
            
            final ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
		} catch (final Exception e) {
			log.error("Exception", e);
		}
    }
	
	/* 設定排除發送中名單列表EXCEL HEADER */
    private Map<Integer, String> getPnpBlockHeaderMap(final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, "手機門號");
        row.put(1, "LINE UID");
        row.put(2, "原因");
        row.put(3, "更新時間");
        row.put(4, "狀態");
        row.put(5, "客群標籤");
        row.put(6, "異動人員");
        return row;
    }

	/* 設定排除發送中名單列表EXCEL BODY */
    private Map<Integer, String> getPnpBlockBodyMap(final PNPBlockSendList r, final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, r.getPhone());
        row.put(1, r.getUid());
        row.put(2, r.getModifyReason());
        row.put(3, DataUtils.convDateToStr(r.getCreateTime(), "yyyy-MM-dd HH:mm:ss.sss"));
        row.put(4, (r.getBlockEnable() == 1)? "排除中" : "取消排除");
        row.put(5, r.getGroupTag());
        row.put(6, r.getInsertUser());
        return row;
    }

	/* 設定排除發送中名單歷程記錄列表EXCEL BODY */
    private Map<Integer, String> getPnpBlockHistoryBodyMap(final PNPBlockHistoryList r, final int columnSize) {
        final Map<Integer, String> row = new LinkedHashMap<>(columnSize);
        row.put(0, r.getMobile());
        row.put(1, r.getUid());
        row.put(2, r.getModifyReason());
        row.put(3, DataUtils.convDateToStr(r.getModifyDateTime(), "yyyy-MM-dd HH:mm:ss.sss"));
        row.put(4, (r.getBlockEnable() == 1)? "排除中" : "取消排除");
        row.put(5, r.getGroupTag());
        row.put(6, r.getInsertUser());
        return row;
    }

	/* 取得排除發送名單歷程記錄總數 */
	@WebServiceLog
	@PostMapping(value = "/getPnpBlockHistoryCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPnpBlockHistoryCount(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("getPnpBlockHistoryCount");
        log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final Long pnpBlockHistoryCount = pnpReportService.getPnpBlockHistoryCount(customUser, pnpSendBlockParam);
			log.info("pnpBlockHistoryCount = {}", pnpBlockHistoryCount);
			return new ResponseEntity<>(pnpBlockHistoryCount, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/* 取得排除發送名單歷程列表 */
	@WebServiceLog
	@PostMapping(value = "/getPnpExcludeSendingHistoryList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPnpExcludeSendingHistoryList(HttpServletRequest request, HttpServletResponse response, 
														@CurrentUser final CustomUser customUser,
														@RequestBody final PnpSendBlockParam pnpSendBlockParam) {
		log.info("getPnpExcludeSendingHistoryList");
        log.info("pnpSendBlockParam.toString() = {}", pnpSendBlockParam.toString());
		
		try { 
			final List<PNPBlockHistoryList> result = pnpReportService.qryPnpBlockHistoryList(customUser, pnpSendBlockParam);
			log.info(DataUtils.toPrettyJsonUseJackson(result));
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Exception", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
