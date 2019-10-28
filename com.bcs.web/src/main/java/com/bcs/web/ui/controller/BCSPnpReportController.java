package com.bcs.web.ui.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.report.builder.ExportExcelBuilder;
import com.bcs.core.report.service.ExportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.service.PNPMaintainAccountModelService;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.taishin.service.PnpReportExcelService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.LoadFileUIService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/bcs")
public class BCSPnpReportController extends BCSBaseController {
    @Autowired
    private PNPMaintainUIService pnpMaintainUIService;
    @Autowired
    private PnpReportExcelService pnpReportExcelService;
    @Autowired
    private OracleService oraclePnpService;
    @Autowired
    private PNPMaintainAccountModelService pnpMaintainAccountModelService;


    @WebServiceLog
    @GetMapping("/pnpEmployee/pnpDetailReportPage")
    public String pnpDetailReportPage(HttpServletRequest request, HttpServletResponse response) {
        log.info("pnpDetailReportPage");
        return BcsPageEnum.PnpDetailReportPage.toString();
    }

    @WebServiceLog
    @GetMapping("/pnpEmployee/getPNPDetailReport")
    @ResponseBody
    public ResponseEntity<?> getPNPDetailReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                                @RequestParam(value = "startDate", required = false) String startDate,
                                                @RequestParam(value = "endDate", required = false) String endDate,
                                                @RequestParam(value = "account", required = false) String account,
                                                @RequestParam(value = "pccCode", required = false) String pccCode,
                                                @RequestParam(value = "sourceSystem", required = false) String sourceSystem,
                                                @RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "phoneNumber", required = false) String phoneNumber
    ) {

        try {
            String empId = customUser.getAccount().toUpperCase();
            Map<String, List<String>> result = pnpMaintainUIService.getPNPDetailReport(startDate, endDate, account, pccCode, sourceSystem, page, empId, phoneNumber);
            log.info(DataUtils.toPrettyJsonUseJackson(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @GetMapping(value = "/pnpEmployee/getPNPDetailReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPDetailReportTotalPages(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                                          @RequestParam(value = "startDate", required = false) String startDate,
                                                          @RequestParam(value = "endDate", required = false) String endDate,
                                                          @RequestParam(value = "account", required = false) String account,
                                                          @RequestParam(value = "pccCode", required = false) String pccCode,
                                                          @RequestParam(value = "sourceSystem", required = false) String sourceSystem,
                                                          @RequestParam(value = "phoneNumber", required = false) String phoneNumber
    ) throws IOException {

        log.info("getPNPDetailReportTotalPages");
        try {
            String empId = customUser.getAccount().toUpperCase();
            String count = pnpMaintainUIService.getPNPDetailReportTotalPages(startDate, endDate, account, pccCode, sourceSystem, empId, phoneNumber);
            return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog(action = "Download")
    @GetMapping("/pnpEmployee/exportPNPDetailReportExcel2")
    @ResponseBody
    public void exportPNPDetailReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                           @RequestParam(value = "startDate", required = false) String startDate,
                                           @RequestParam(value = "endDate", required = false) String endDate,
                                           @RequestParam(value = "account", required = false) String account,
                                           @RequestParam(value = "pccCode", required = false) String pccCode,
                                           @RequestParam(value = "sourceSystem", required = false) String sourceSystem) {

        // file path
        String filePath = CoreConfigReader.getString("file.path");

        // file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date();
        String fileName = "PNPDetailReport_" + sdf.format(date) + ".xlsx";

        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                boolean isMakeDirSuccess = folder.mkdirs();
                log.info("isMakeDirSuccess" + isMakeDirSuccess);
            }
            String empId = customUser.getAccount().toUpperCase();
            pnpReportExcelService.exportPNPDetailReportExcel(filePath, fileName, startDate, endDate, account, pccCode, sourceSystem, empId);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

        try {
            LoadFileUIService.loadFileToResponse(filePath, fileName, response);
        } catch (IOException e) {
            log.error("Exception", e);
        }
    }


    @WebServiceLog(action = "Download")
    @GetMapping("/pnpEmployee/exportPNPDetailReportExcel")
    @ResponseBody
    public void exportPNPDetailReportExcel2(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate,
                                            @RequestParam(value = "account", required = false) String account,
                                            @RequestParam(value = "pccCode", required = false) String pccCode,
                                            @RequestParam(value = "sourceSystem", required = false) String sourceSystem) {
        try {
            String empId = customUser.getAccount().toUpperCase();
            List<Map<Integer, String>> list = pnpMaintainAccountModelService.getPNPDetailReportExcelMapList(startDate, endDate, account, pccCode, sourceSystem, empId);
            ExportExcelBuilder builder = ExportExcelBuilder.createWorkBook().setSheetName("TestSheet1");

            for (int i = 0; i < list.size(); i++) {
                Map<Integer, String> columnDataMap = list.get(i);
                builder.createRow(i).setRowValue(columnDataMap);
            }

            builder.setAllColumnAutoWidth()
                    .setOutputPath(CoreConfigReader.getString("file.path"))
                    .setOutputFileName(String.format("PNPDetailReport_%s.xlsx", DataUtils.formatDateToString(new Date(), "yyyy-MM-dd-HHmmss")));

            ExportService exportService = new ExportService();
            exportService.exportExcel(response, builder);
        } catch (Exception e) {
            log.error("{}: {}", "Exception", e);
        }
    }
}
