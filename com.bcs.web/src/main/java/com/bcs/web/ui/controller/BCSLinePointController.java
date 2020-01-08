package com.bcs.web.ui.controller;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.ExportToExcelForLinePointPushApiEffects;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.utils.service.ExcelUtilService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.LinePointUIService;
import com.bcs.web.ui.service.SendGroupUIService;
import com.bcs.web.ui.service.SendMsgUIService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/bcs")
public class BCSLinePointController extends BCSBaseController {
    @Autowired
    private LinePointUIService linePointUIService;
    @Autowired
    private LinePointPushAkkaService linePointPushAkkaService;
    @Autowired
    private ExportToExcelForLinePointPushApiEffects exportToExcelForLinePointPushApiEffects;
    @Autowired
    private ExcelUtilService excelUtilService;
    @Autowired
    private SendGroupUIService sendGroupUIService;
    @Autowired
    private SendMsgUIService sendMsgUIService;
    @Autowired
    private LinePointDetailService linePointDetailService;
    @Autowired
    private OracleService oracleService;
    @Autowired
    private LineUserService lineUserService;


    @ControllerLog(description = "建立 Line Point 活動")
    @GetMapping(value = "/edit/linePointCreatePage")
    public String linePointCreatePage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
        log.info("linePointCreatePage");
        return BcsPageEnum.LinePointCreatePage.toString();
    }

    @ControllerLog(description = "Line Point 活動列表")
    @GetMapping(value = "/edit/linePointListPage")
    public String linePointListPage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
        log.info("linePointListPage");
        return BcsPageEnum.LinePointListPage.toString();
    }

    @ControllerLog(description = "getCaveatLinePoint")
    @GetMapping(value = "/edit/getCaveatLinePoint")
    public ResponseEntity<?> getCaveatLinePoint(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
        log.info("getCaveatLinePoint");
        String CaveatLinePoint = CoreConfigReader.getString(CONFIG_STR.CAVEAT_LINEPOINT_POINT, true);
        return new ResponseEntity<>(CaveatLinePoint, HttpStatus.OK);
    }

    // ---- Data Creation ----

    @ControllerLog(description = "createLinePointMain")
    @PostMapping(value = "/edit/createLinePointMain", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createLinePointMain(HttpServletRequest request, HttpServletResponse response,
                                                 @CurrentUser CustomUser customUser, @RequestBody LinePointMain linePointMain) {
        try {
            // Null Exception
            log.info("[createLinePointMain]");
            if (linePointMain == null) {
                throw new BcsNoticeException("LinePointMain is Null");
            }

            // get Oracle Account Information
            String empId = customUser.getAccount().toUpperCase();
            log.info("empId:" + empId);
            if (StringUtils.isBlank(empId)) {
                throw new BcsNoticeException("員工編號不得為空值");
            }

            TaishinEmployee taishinEmployee = oracleService.findByEmployeeId(empId);

            if (taishinEmployee == null || StringUtils.isBlank(taishinEmployee.getDivisionName())) {
                throw new BcsNoticeException("查無此員工編號");
            }

            // get Department Full Name
            String departmentFullName = taishinEmployee.getDivisionName() + " " +
                    taishinEmployee.getDepartmentName() + " " + taishinEmployee.getGroupName();
            log.info("departmentFullName:" + departmentFullName);

            linePointMain.setDepartmentFullName(departmentFullName);
            linePointMain.setModifyUser(customUser.getAccount());
            linePointMain.setModifyTime(new Date());
            LinePointMain result = linePointUIService.saveLinePointMain(linePointMain);
            log.info("linePointMain : " + result);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BcsNoticeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@ControllerLog(description = "Save Line Point Detail List")
    @Transactional(rollbackFor = Exception.class, timeout = 300000)
    @PostMapping(value = "/edit/createLinePointDetailList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
                                                       @CurrentUser CustomUser customUser, @RequestBody List<LinePointDetail> linePointDetail) throws IOException {
        log.info("[createLinePointDetailList]");
        try {
            if (linePointDetail == null) {

                throw new Exception("linePointDetail is Null");
            }

            Long linePointMainId = linePointDetail.get(0).getLinePointMainId();
            log.info("delete linePointDetail from linePointMainId :" + linePointMainId);
            linePointDetailService.deleteFromLinePointMainId(linePointMainId);
            log.info("linePointDetail : " + linePointDetail);
            List<LinePointDetail> result = linePointUIService.saveLinePointDetailListFromUI(linePointDetail, customUser.getAccount());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    // ---- Data Search ----

    @ControllerLog(description = "findOneMainByMainId")
    @PostMapping(value = "/edit/findOneLinePointMainByMainId")
    @ResponseBody
    public ResponseEntity<?> findOneLinePointMainByMainId(HttpServletRequest request, HttpServletResponse response,
                                                          @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
        try {
            log.info("[findOneLinePointMainByMainId]");
            if (linePointMainId != null) {
                LinePointMain result = linePointUIService.linePointMainFindOne(linePointMainId);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                throw new Exception("LinePointMain is Null");
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @ControllerLog(description = "findAllLinePointDetailByMainId")
    @GetMapping(value = "/edit/findAllLinePointDetailByMainId")
    @ResponseBody
    public ResponseEntity<?> findAllLinePointDetailByMainId(HttpServletRequest request, HttpServletResponse response,
                                                            @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
        try {
            try {
                log.info("[findAllLinePointDetailByMainId] linePointMainId:" + linePointMainId);
                // get Details
                List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMainId);
                log.info("linePointDetails:" + linePointDetails);
                return new ResponseEntity<>(linePointDetails, HttpStatus.OK);
            } catch (Exception e) {
                throw new BcsNoticeException(e.getMessage());
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @ControllerLog(description = "findAllBcsLinePointMain")
    @GetMapping(value = {"/edit/findAllBcsLinePointMain"})
    @ResponseBody
    public ResponseEntity<?> findAllBcsLinePointMain(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                                     @RequestParam(value = "startDate", required = false) String startDateStr,
                                                     @RequestParam(value = "endDate", required = false) String endDateStr) throws IOException {
        try {
            log.info("[findAllBcsLinePointMain]");

            // null translation
            if (StringUtils.isBlank(startDateStr) || startDateStr.equals("null")) {
                startDateStr = "1911-01-01";
            }
            if (StringUtils.isBlank(endDateStr) || endDateStr.equals("null")) {
                endDateStr = "3099-01-01";
            }

            // parse date data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null, endDate = null;
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endDate = DateUtils.addDays(endDate, 1);
            log.info("startDate:" + startDate);
            log.info("endDate:" + endDate);

            //List<LinePointMain> result = new ArrayList<LinePointMain>();
            List<LinePointMain> list = this.linePointUIService.linePointMainFindBcsAndDate(startDate, endDate);
            for (LinePointMain linePointMain : list) {
                linePointUIService.updateLinePoint(linePointMain.getId().toString());
            }
            list = this.linePointUIService.linePointMainFindBcsAndDate(startDate, endDate);
            //log.info("list:" + list);
            List<LinePointMain> result = competence(list, customUser);

//		    result.addAll(list);
            log.info("result:" + ObjectUtil.objectToJsonStr(result));
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @ControllerLog(description = "findAllLinePointMain")
    @GetMapping(value = "/edit/findAllLinePointMain")
    @ResponseBody
    public ResponseEntity<?> getAllLinePointMainList(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) throws IOException {
        log.info("[findAllLinePointMain]");
        List<LinePointMain> result = new ArrayList<LinePointMain>();
        List<LinePointMain> list = linePointUIService.linePointMainFindAll();
        result.addAll(list);
        log.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    // ---- Front End Data Upload ----
    @Transactional(timeout = 300000)
    @ControllerLog(description = "Check Active UIds")
    @PostMapping(value = "/edit/checkActiveUids", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> checkActiveUids(HttpServletRequest request, HttpServletResponse response,
                                             @CurrentUser CustomUser customUser, @RequestBody List<String> uids) throws IOException {
        try {
            log.info("[checkActiveUids]");
            List<Integer> removeIndexs = new ArrayList();
            //改寫 這邊  這邊csv太大筆   會掛掉
            for (int i = 0; i < uids.size(); i++) {
                boolean isActive = lineUserService.checkMIDAllActive(uids.get(i));
                log.info("i=" + i + ", uid=" + uids.get(i) + ", isActive=" + isActive);
                if (!isActive) {
                    removeIndexs.add(i);
                }
            }
            log.info("removeIndexs:" + ObjectUtil.objectToJsonStr(removeIndexs));
            return new ResponseEntity<>(removeIndexs, HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @ControllerLog(description = "CSV to EXCEL")
    @PostMapping(value = "/edit/csvToExcel")
    @ResponseBody
    public ResponseEntity<?> csvToExcel(HttpServletRequest request, HttpServletResponse response,
                                        @CurrentUser CustomUser customUser, @RequestPart MultipartFile filePart) throws IOException {
        try {
            log.info("---------csvToExcel----------");
            // file path
            String filePath = CoreConfigReader.getString("file.path");

            // file name
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            Date date = new Date();
            String fileName = "LinePointSendGroupCsvToXlsx_" + sdf.format(date) + ".xlsx";

            // convert to excel file
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            InputStream isXlsx = excelUtilService.csvToXlsx(filePart.getInputStream(), filePath, fileName);

            // uploadMidSendGroup
            if (isXlsx != null) {
                String modifyUser = customUser.getAccount();
                log.info("modifyUser:" + modifyUser);

                Map<String, Object> result = sendGroupUIService.uploadMidSendGroup(isXlsx, modifyUser, new Date(), fileName);
                log.info("result :" + result);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                throw new Exception("Upload isXlsx Null");
            }
        } catch (Exception e) {
            log.info("uploadMidSendGroup Exception : " + e.getMessage().toString());
            if (e.getMessage().contains("RetrySaveUserEventSet")) {
                Map<String, Object> result = sendGroupUIService.retrySaveUserEventSet();
                log.info("uploadMidSendGroupResult1:" + result);

                return new ResponseEntity<>(result, HttpStatus.OK);
            } else if (e.getMessage().contains("TimeOut")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
            }

            log.error(ErrorRecord.recordError(e));

            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    // ---- Front End Action ----

    @ControllerLog(description = "pressSendLinePointMain")
    @PostMapping(value = "/edit/pressSendLinePointMain")
    @ResponseBody
    public ResponseEntity<?> pressSendLinePointMain(HttpServletRequest request, HttpServletResponse response,
                                                    @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
        try {
            // get linePointMain
            log.info("[pressSendLinePointMain] linePointMainId:" + linePointMainId);
            LinePointMain linePointMain = linePointUIService.linePointMainFindOne(linePointMainId);
            if (linePointMain.getSendStartTime() != null) {
                throw new BcsNoticeException("此專案已發送");
            }

            if ("ROLE_ADMIN".equals(customUser.getRole()) || "ROLE_LINE_VERIFY".equals(customUser.getRole())) {
                if ((!"ROLE_ADMIN".equals(customUser.getRole())) && customUser.getAccount().equals(linePointMain.getModifyUser())) {
                    throw new BcsNoticeException("不可發送自己創專案的line Point");
                }
            } else {
                throw new BcsNoticeException("沒有權限可以發送line Point");
            }


            // switch allowToSend
            linePointMain.setAllowToSend(!linePointMain.getAllowToSend());
            linePointUIService.saveLinePointMain(linePointMain);

            // immediate
            if (LinePointMain.SEND_TIMING_TYPE_IMMEDIATE.equals(linePointMain.getSendTimingType())) {
                try {
                    // send append message
                    Long msgId = linePointMain.getAppendMessageId();
                    log.info("msgId:" + msgId);
                    sendMsgUIService.createExecuteSendMsgRunnable(msgId);

                    // save send start time
                    //linePointMain.setModifyUser(customUser.getAccount());
                    linePointMain.setSendStartTime(new Date());
                    linePointMain.setStatus(LinePointMain.STATUS_COMPLETE);
                    //linePointMain.setModifyTime(new Date());
                    linePointMain.setSendUser(customUser.getAccount());
                    linePointUIService.saveLinePointMain(linePointMain);

                    // get Details
                    List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMainId);
                    log.info("linePointDetails:" + linePointDetails);

                    JSONArray detailIds = new JSONArray();
                    int i = 1;
                    for (LinePointDetail linePointDetail : linePointDetails) {
                        log.info("Total LinePointDetail Detail " + i + ": " + DataUtils.toPrettyJsonUseJackson(linePointDetail));
                        //FIXME 這裡會過濾掉Fail的Detail Object
                        if (!"FAIL".equals(linePointDetail.getStatus())) {
                            detailIds.put(linePointDetail.getDetailId());
                        }
                        i++;
                    }
                    log.info("To Akka Detail List Size: " + detailIds.toList().size());

                    // combine LinePointPushModel
                    LinePointPushModel linePointPushModel = new LinePointPushModel();
                    linePointPushModel.setEventId(linePointMainId);
                    linePointPushModel.setDetailIds(detailIds);
                    linePointPushModel.setSource(LinePointMain.SEND_TYPE_BCS);
                    linePointPushModel.setSendTimeType(LinePointMain.SEND_TIMING_TYPE_IMMEDIATE);
                    linePointPushModel.setTriggerTime(new Date());

                    linePointPushAkkaService.tell(linePointPushModel);

                } catch (Exception e) {
                    throw new BcsNoticeException(e.getMessage());
                }
            }
            return new ResponseEntity<>("", HttpStatus.OK);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @ControllerLog(description = "deleteLinePointMain")
    @PostMapping(value = "/edit/deleteLinePointMain")
    @ResponseBody
    public ResponseEntity<?> deleteLinePointMain(HttpServletRequest request, HttpServletResponse response,
                                                 @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
        log.info("[deleteLinePointMain]");
        log.info("linePointMainId : " + linePointMainId);

        try {
            LinePointMain linePointMain = linePointUIService.linePointMainFindOne(linePointMainId);

            if ("ROLE_LINE_SEND".equals(customUser.getRole()) || "ROLE_LINE_VERIFY".equals(customUser.getRole())) {
                if (!customUser.getAccount().equals(linePointMain.getModifyUser())) {
                    throw new BcsNoticeException("沒有權限可以刪除此line Point專案");
                }
            } else if (!"ROLE_ADMIN".equals(customUser.getRole())) {
                throw new BcsNoticeException("沒有權限可以刪除此line Point專案");
            }

            List<LinePointMain> result = linePointUIService.deleteByLinePointMainId(linePointMainId);
            log.info("delete LinePointMain : " + result);
            return new ResponseEntity<>("", HttpStatus.OK);

        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @ControllerLog(description = "getSumCaveatLinePoint")
    @GetMapping(value = "/edit/getSumCaveatLinePoint")
    @ResponseBody
    public ResponseEntity<?> getSumCaveatLinePoint(HttpServletRequest request, HttpServletResponse response,
                                                   @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {

        log.info("getSumCaveatLinePoint");
        log.info("linePointMainId : " + linePointMainId);

        String caveatLinePoint = CoreConfigReader.getString(CONFIG_STR.CAVEAT_LINEPOINT_POINT, true);

        try {
            String number = linePointDetailService.getCountLinePointDetailAmountMoreCaveatLinePoint(linePointMainId, caveatLinePoint);

            return new ResponseEntity<>(caveatLinePoint + "@" + number, HttpStatus.OK);

        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        }
    }

    public List<LinePointMain> competence(List<LinePointMain> list, CustomUser customUser) throws Exception {
        List<LinePointMain> result = new ArrayList();

        //取得權限
        String role = customUser.getRole();
        String empId = customUser.getAccount();
        // reset service name
        for (LinePointMain main : list) {
            String serviceName = "BCS";
            if (main.getSendType().equals(LinePointMain.SEND_TYPE_API)) {
                List<LinePointDetail> details = linePointDetailService.findByLinePointMainId(main.getId());
                serviceName = details.get(0).getServiceName();
            }
            main.setSendType(serviceName);

            if ("ROLE_ADMIN".equals(role) || "ROLE_REPORT".equals(role)) {
                result.add(main);
            } else if ("ROLE_LINE_SEND".equals(role) || "ROLE_LINE_VERIFY".equals(role)) {

                TaishinEmployee employee = oracleService.findByEmployeeId(empId);
//					TaishinEmployee employee = new TaishinEmployee();

//					employee.setDivisionName("XTREME");
//					employee.setDepartmentName("LINEBC");


                String Department = main.getDepartmentFullName();
                String[] Departmentname = Department.split(" ");
                //Departmentname[0]; 處  DIVISION_NAME
                //Departmentname[1]; 部  DEPARTMENT_NAME
                //Departmentname[2]; 組  GROUP_NAME

                //判斷邏輯  如果登錄者有組 那只能看到同組 顧處部組全都要一樣，沒有組有部 那就是處跟部要一樣才可以，只有處 就是處一樣即可
                if (StringUtils.isNotBlank(employee.getGroupName())) {
                    if (Departmentname[0].equals(employee.getDivisionName()) && Departmentname[1].equals(employee.getDepartmentName()) && Departmentname[2].equals(employee.getGroupName())) {
                        result.add(main);
                    }
                } else if (StringUtils.isNotBlank(employee.getDepartmentName())) {
                    if (Departmentname[0].equals(employee.getDivisionName()) && Departmentname[1].equals(employee.getDepartmentName())) {
                        result.add(main);
                    }
                } else if (StringUtils.isNotBlank(employee.getDivisionName())) {
                    if (Departmentname[0].equals(employee.getDivisionName())) {
                        result.add(main);
                    }
                }
            }
        }
        return result;
    }

//	@ControllerLog(description = "Get Manual Line Point Main")
//	@GetMapping(value = "/edit/getManualLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser) throws IOException {
//		log.info("[getManualLinePointMainList]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindManual();
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}

//	@ControllerLog(description = "Get Auto Line Point Main")
//	@GetMapping(value = "/edit/getAutoLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser) throws IOException {
//		log.info("[getAutoLinePointMainList]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindAuto();
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}

    //----
//	@ControllerLog(description = "Get All Line Point Main")
////	@GetMapping(value = "/edit/getAllLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getAllLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		log.info("[findAllLinePointMainList]");
//		try {
//			List<LinePointMain> result = new ArrayList();
//			List<LinePointMain> list = linePointUIService.linePointMainFindAll(searchText);
//			result.addAll(list);
//			log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//			return new ResponseEntity<>(result, HttpStatus.OK);
//		}catch(Exception e) {
//			log.info("Error1: " + e.getMessage());
//			return new ResponseEntity<>("Error1: " + e.getMessage(), HttpStatus.OK);
//		}
//
//	}

//	@ControllerLog(description = "Get Manual Line Point Main")
//	@GetMapping(value = "/edit/getManualLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getManualLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		log.info("[getManualLinePointMainListSearch]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindManual(searchText);
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//
//	@ControllerLog(description = "Get Auto Line Point Main")
//	@GetMapping(value = "/edit/getAutoLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getAutoLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		log.info("[getAutoLinePointMainListSearch]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindAuto(searchText);
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	//----
//	@ControllerLog(description = "Get Undone Manual Line Point Main")
//	@GetMapping(value = "/edit/getUndoneManualLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getUndoneManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser ) throws IOException {
//		log.info("getUndoneManualLinePointMainList");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneManual();
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//
//	@ControllerLog(description = "Get Undone Auto Line Point Main")
//	@GetMapping(value = "/edit/getUndoneAutoLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getUndoneAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser ) throws IOException {
//		log.info("getUndoneAutoLinePointMainList");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneAuto();
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	@ControllerLog(description = "Get Success Line Point Detail")
//	@GetMapping(value = "/edit/getSuccessLinePointDetailList/{linePointMainId}")
//	@ResponseBody
//	public ResponseEntity<?> getSuccessLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
//		log.info("getSuccessLinePointDetailList");
//		Long mainId = Long.parseLong(linePointMainId);
//
//		List<LinePointDetail> result = new ArrayList();
//		List<LinePointDetail> list = linePointUIService.findSuccess(mainId);
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	@ControllerLog(description = "Get Fail Line Point Detail")
//	@GetMapping(value = "/edit/getFailLinePointDetailList/{linePointMainId}")
//	@ResponseBody
//	public ResponseEntity<?> getFailLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
//		log.info("getFailLinePointDetailList");
//		Long mainId = Long.parseLong(linePointMainId);
//
//		List<LinePointDetail> result = new ArrayList();
//		List<LinePointDetail> list = linePointUIService.findFail(mainId);
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	@ControllerLog(description = "Get Success Line Point Detail")
//	@GetMapping(value = "/edit/getLinePointScheduledDetailList/{mainId}")
//	@ResponseBody
//	public ResponseEntity<?> getLinePointScheduledDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable Long mainId) throws IOException {
//		log.info("getLinePointScheduledDetailList");
//
//		List<LinePointScheduledDetail> result = new ArrayList();
//		List<LinePointScheduledDetail> list = linePointUIService.findScheduledDetailList(mainId);
//		result.addAll(list);
//		log.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}

//	@ControllerLog(description="deleteLinePointMain")
//	@RequestMapping(method = RequestMethod.DELETE, value = "/edit/deleteLinePointMain")
//	@ResponseBody
//	public ResponseEntity<?> deleteLinePointMain( HttpServletRequest request,  HttpServletResponse response, @CurrentUser CustomUser customUser,
//			@RequestParam(required=false) String campaignId, @RequestParam(required=false) String listType) throws IOException {
//		log.info("deleteLinePointMain");
//		try{
//			if(StringUtils.isNotBlank(campaignId)){
//				log.info("campaignId:" + campaignId);
//				linePointUIService.deleteFromUI(Long.parseLong(campaignId), customUser.getAccount(), listType);
//				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
//			} else
//				throw new Exception("ID IS NULL");
//		} catch(Exception e) {
//			log.error(ErrorRecord.recordError(e));
//			if(e instanceof BcsNoticeException)
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
}