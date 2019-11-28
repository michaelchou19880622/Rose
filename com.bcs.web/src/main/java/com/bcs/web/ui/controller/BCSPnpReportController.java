package com.bcs.web.ui.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.report.builder.ExportExcelBuilder;
import com.bcs.core.report.service.ExportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.PNP.db.service.PNPMaintainAccountModelService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.PNPMaintainUIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Bcs pnp report controller.
 *
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/bcs/pnpEmployee")
public class BCSPnpReportController {
    private PNPMaintainUIService pnpMaintainUiService;
    private PNPMaintainAccountModelService pnpMaintainAccountModelService;

    /**
     * Instantiates a new Bcs pnp report controller.
     *
     * @param pnpMaintainUiService           the pnp maintain ui service
     * @param pnpMaintainAccountModelService the pnp maintain account model service
     */
    @Autowired
    public BCSPnpReportController(PNPMaintainUIService pnpMaintainUiService,
                                  PNPMaintainAccountModelService pnpMaintainAccountModelService) {
        this.pnpMaintainUiService = pnpMaintainUiService;
        this.pnpMaintainAccountModelService = pnpMaintainAccountModelService;
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
        return BcsPageEnum.PnpDetailReportPage.toString();
    }

    /**
     * Gets pnp detail report.
     *
     * @param customUser   the custom user
     * @param startDate    the start date
     * @param endDate      the end date
     * @param account      the account
     * @param pccCode      the pcc code
     * @param sourceSystem the source system
     * @param page         the page
     * @param phoneNumber  the phone number
     * @return the pnp detail report
     */
    @WebServiceLog
    @GetMapping("/getPNPDetailReport")
    @ResponseBody
    public ResponseEntity<?> getPnpDetailReport(@CurrentUser CustomUser customUser,
                                                @RequestParam(value = "startDate", required = false) String startDate,
                                                @RequestParam(value = "endDate", required = false) String endDate,
                                                @RequestParam(value = "account", required = false) String account,
                                                @RequestParam(value = "pccCode", required = false) String pccCode,
                                                @RequestParam(value = "sourceSystem", required = false) String sourceSystem,
                                                @RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "phoneNumber", required = false) String phoneNumber) {
        try {
            String empId = customUser.getAccount().toUpperCase();
            Map<String, List<String>> result = pnpMaintainUiService.getPNPDetailReport(startDate, endDate, account, pccCode, sourceSystem, page, empId, phoneNumber);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets pnp detail report total pages.
     *
     * @param customUser   the custom user
     * @param startDate    the start date
     * @param endDate      the end date
     * @param account      the account
     * @param pccCode      the pcc code
     * @param sourceSystem the source system
     * @param phoneNumber  the phone number
     * @return the pnp detail report total pages
     */
    @WebServiceLog
    @GetMapping(value = "/getPNPDetailReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPnpDetailReportTotalPages(@CurrentUser CustomUser customUser,
                                                          @RequestParam(value = "startDate", required = false) String startDate,
                                                          @RequestParam(value = "endDate", required = false) String endDate,
                                                          @RequestParam(value = "account", required = false) String account,
                                                          @RequestParam(value = "pccCode", required = false) String pccCode,
                                                          @RequestParam(value = "sourceSystem", required = false) String sourceSystem,
                                                          @RequestParam(value = "phoneNumber", required = false) String phoneNumber) {

        try {
            String empId = customUser.getAccount().toUpperCase();
            int count = pnpMaintainUiService.getPNPDetailReportTotalPages(startDate, endDate, account, pccCode, sourceSystem, empId, phoneNumber);
            return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Export pnp detail report excel.
     *
     * @param response     the response
     * @param customUser   the custom user
     * @param startDate    the start date
     * @param endDate      the end date
     * @param account      the account
     * @param pccCode      the pcc code
     * @param sourceSystem the source system
     */
    @WebServiceLog(action = "Download")
    @GetMapping("/exportPNPDetailReportExcel")
    @ResponseBody
    public void exportPnpDetailReportExcel(HttpServletResponse response, @CurrentUser CustomUser customUser,
                                           @RequestParam(value = "startDate", required = false) String startDate,
                                           @RequestParam(value = "endDate", required = false) String endDate,
                                           @RequestParam(value = "account", required = false) String account,
                                           @RequestParam(value = "pccCode", required = false) String pccCode,
                                           @RequestParam(value = "sourceSystem", required = false) String sourceSystem) {
        try {
            String empId = customUser.getAccount().toUpperCase();
            List<Map<Integer, String>> list = pnpMaintainAccountModelService.getPNPDetailReportExcelMapList(startDate, endDate, account, pccCode, sourceSystem, empId);
            ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("TestSheet1");

            for (int i = 0, listSize = list.size(); i < listSize; i++) {
                Map<Integer, String> columnDataMap = list.get(i);
                builder.createRow(i).setRowValue(columnDataMap);
            }

            builder.setAllColumnAutoWidth()
                    .setOutputPath(CoreConfigReader.getString("file.path"))
                    .setOutputFileName(String.format("PNPDetailReport_%s.xlsx", DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));

            ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * Get Status Map
     * @return status map
     */
    @WebServiceLog(action = "getConfig")
    @ResponseBody
    @GetMapping("/getPnpStatusEnum")
    public ResponseEntity<?> getPnpStatusEnum() {
        try {
            Map<String, String> map = new HashMap<>(PnpStatusEnum.values().length);
            for(PnpStatusEnum e : PnpStatusEnum.values()){
                map.put(e.value, e.chinese);
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
