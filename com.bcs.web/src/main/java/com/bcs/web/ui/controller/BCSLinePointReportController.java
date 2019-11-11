package com.bcs.web.ui.controller;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.utils.service.LinePointReportExcelService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.utils.RestfulUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.LinePointUIService;
import com.bcs.web.ui.service.LoadFileUIService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/bcs")
public class BCSLinePointReportController extends BCSBaseController {
    @Autowired
    private LinePointUIService linePointUIService;
    @Autowired
    private LinePointDetailService linePointDetailService;
    @Autowired
    private LinePointReportExcelService linePointReportExcelService;
    @Autowired
    private LinePointMainService linePointMainService;
    @Autowired
    private OracleService oracleService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(BCSLinePointReportController.class);

    @ControllerLog(description = "Line Point 統計報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/linePointStatisticsReportPage")
    public String linePointStatisticsReportPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("linePointStatisticsReportPage");
        return BcsPageEnum.LinePointStatisticsReportPage.toString();
    }

    @ControllerLog(description = "Line Point 統計報表明細")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/linePointStatisticsReportDetailPage")
    public String linePointStatisticsReportDetailPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("linePointStatisticsReportPage");
        return BcsPageEnum.LinePointStatisticsReportDetailPage.toString();
    }

    // ---- Statistics Report ----

    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReport")
    @ResponseBody
    public ResponseEntity<?> getLPStatisticsReport(HttpServletRequest request, HttpServletResponse response,
                                                   @CurrentUser CustomUser customUser,
                                                   @RequestParam(value = "startDate", required = false) String startDateStr,
                                                   @RequestParam(value = "endDate", required = false) String endDateStr,
                                                   @RequestParam(value = "modifyUser", required = false) String modifyUser,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "page", required = true) Integer page) throws IOException {
        try {
            logger.info("[getLPStatisticsReport]");

            // null translation
            if (StringUtils.isBlank(startDateStr) || startDateStr.equals("null"))
                startDateStr = "1911-01-01";
            if (StringUtils.isBlank(endDateStr) || endDateStr.equals("null"))
                endDateStr = "3099-01-01";
            if (StringUtils.isBlank(title) || title.equals("null"))
                title = "";
            if (StringUtils.isBlank(modifyUser) || modifyUser.equals("null"))
                modifyUser = "";

            // parse date data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null, endDate = null;
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endDate = DateUtils.addDays(endDate, 1);
            logger.info("startDate:" + startDate);
            logger.info("endDate:" + endDate);
            logger.info("title:" + title);
            logger.info("modifyUser:" + modifyUser);

            // get result list
            List<LinePointMain> result = new ArrayList();
//			List<LinePointMain> list = linePointUIService.getLinePointStatisticsReport(startDate, endDate, modifyUser,
//					title, page);

            List<LinePointMain> list = linePointMainService.findByTitleAndModifyUserAndSendDate(startDate, endDate,
                    modifyUser, title);
            // 權限後拿到的資料顯示在
            result = competence(list, customUser);


            List<Object[]> SuccessandFailCount = linePointDetailService.getSuccessandFailCount(startDate, endDate);
            logger.info("SuccessandFailCount : " + SuccessandFailCount);
            for (Object[] o : SuccessandFailCount) {
                String mainId = o[0].toString();
                String success = o[1].toString();
                String fail = o[2].toString();
                String successfulAmount = "";
                if (null == o[3]) {
                    successfulAmount = "0";
                } else {
                    successfulAmount = o[3].toString();
                }
                logger.info("mainId" + mainId);
                logger.info("success" + success);
                logger.info("fail" + fail);
                logger.info("totleAmount" + successfulAmount);

                for (LinePointMain linePointMain : result) {
                    if (linePointMain.getId().toString().equals(mainId)) {
                        linePointMain.setSuccessfulCount(Long.parseLong(success));
                        linePointMain.setFailedCount(Long.parseLong(fail));
                        linePointMain.setTotalCount(Long.parseLong(success) + Long.parseLong(fail));
                        linePointMain.setSuccessfulAmount(Long.parseLong(successfulAmount));
                        break;
                    }
                }
            }

            logger.info("result:" + ObjectUtil.objectToJsonStr(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<?> getLPStatisticsReportTotalPages(HttpServletRequest request, HttpServletResponse response,
                                                             @CurrentUser CustomUser customUser,
                                                             @RequestParam(value = "startDate", required = false) String startDateStr,
                                                             @RequestParam(value = "endDate", required = false) String endDateStr,
                                                             @RequestParam(value = "modifyUser", required = false) String modifyUser,
                                                             @RequestParam(value = "title", required = false) String title) throws IOException {
        try {
            logger.info("[getLPStatisticsReportTotalPages]");

            // null translation
            if (StringUtils.isBlank(startDateStr) || startDateStr.equals("null"))
                startDateStr = "1911-01-01";
            if (StringUtils.isBlank(endDateStr) || endDateStr.equals("null"))
                endDateStr = "3099-01-01";
            if (StringUtils.isBlank(title) || title.equals("null"))
                title = "";
            if (StringUtils.isBlank(modifyUser) || modifyUser.equals("null"))
                modifyUser = "";

            // parse date data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null, endDate = null;
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endDate = DateUtils.addDays(endDate, 1);
            logger.info("startDate:" + startDate);
            logger.info("endDate:" + endDate);
            logger.info("title:" + title);
            logger.info("modifyUser:" + modifyUser);

            // calculate count

            // 應該設一個全域變數 來判斷使用者應該會看到幾筆資料
            Long count = linePointUIService.getLinePointStatisticsReportTotalPages(startDate, endDate, modifyUser,
                    title);
            if (count % 10L == 0L) {
                count /= 10;
            } else {
                count = count / 10 + 1;
            }

            return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count.toString() + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/linePointCancelFromDetailId")
    @ResponseBody
    public ResponseEntity<?> linePointCancelFromDetailId(HttpServletRequest request, HttpServletResponse response,
                                                         @CurrentUser CustomUser customUser, @RequestParam(value = "detailId", required = false) Long detailId)
            throws IOException {
        logger.info("[linePointCancelFromDetailId]");
        logger.info(" detailId : " + detailId);
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date current = new Date();
        String message = customUser.getAccount() + " " + sdFormat.format(current) + " " + "收回點數";

        // 取回收回點數的那一筆資料
        List<LinePointDetail> result = linePointDetailService.findByDetailId(detailId);
        LinePointDetail linePointDetail = result.get(0);

        LinePointMain linePointMain = linePointUIService.linePointMainFindOne(linePointDetail.getLinePointMainId());
        try {
            if (!"ROLE_ADMIN".equals(customUser.getRole())) {
                if (!"ROLE_LINE_VERIFY".equals(customUser.getRole())) {
                    throw new BcsNoticeException("只有管理者或是發送人員才可收回點數");
                } else if (!customUser.getAccount().equals(linePointMain.getSendUser())) {
                    throw new BcsNoticeException("只有管理者或是發送人員才可收回點數");
                }
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            // String accessToken = linePointApiService.getLinePointChannelAccessToken();
            String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
                    CONFIG_STR.ChannelToken.toString(), true);
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            // 收回點數的 jsonbody
            JSONObject requestBody = new JSONObject();
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_CANCEL_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
            url = url.replace("{transactionId}", linePointDetail.getTranscationId());
            String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
            requestBody.put("clientId", clientId);
            requestBody.put("memberId", linePointDetail.getUid());
            requestBody.put("orderKey", linePointDetail.getOrderKey() + "a"); // order key 不能重複
            requestBody.put("amount", "");
            requestBody.put("note", message);

            HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            JSONObject responseObject = null;

            try {
                responseObject = restfulUtil.execute();

                String Id = responseObject.getString("transactionId");
                Long Time = responseObject.getLong("transactionTime");
                String Type = responseObject.getString("transactionType");
                Integer cancelledAmount = responseObject.getInt("cancelledAmount");
                Integer remainingAmount = responseObject.getInt("remainingAmount");
                Integer Balance = responseObject.getInt("balance");

                linePointDetail.setMessage(message);
                linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointDetail.setCancelTime(new Date());
                linePointDetailService.save(linePointDetail);
                logger.info(" linePointDetail save: " + linePointDetail);

                linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() - 1L);
                linePointMain.setFailedCount(linePointMain.getFailedCount() + 1L);
                linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() - linePointDetail.getAmount());
                linePointMainService.save(linePointMain);
                logger.info(" linePointMain save: " + linePointMain);

            } catch (HttpClientErrorException e) {
                logger.info("[LinePointApi] Status code: " + e.getStatusCode());
                logger.info("[LinePointApi]  Response body: " + e.getResponseBodyAsString());

                return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR); // e.getStatusCode()
            }

            return new ResponseEntity<>("", HttpStatus.OK);
        } catch (Exception e) {
            logger.info("e:" + e.toString());
            if (e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}",
                        HttpStatus.BAD_REQUEST);
            else if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException
                    || e instanceof IllegalArgumentException)
                return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}",
                        HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportExcel")
    @ResponseBody
    public void getLPStatisticsReportExcel(HttpServletRequest request, HttpServletResponse response,
                                           @CurrentUser CustomUser customUser,
                                           @RequestParam(value = "startDate", required = false) String startDateStr,
                                           @RequestParam(value = "endDate", required = false) String endDateStr,
                                           @RequestParam(value = "modifyUser", required = false) String modifyUser,
                                           @RequestParam(value = "title", required = false) String title) throws IOException {
        try {
            logger.info("[exportLPStatisticsReportExcel]");

            // null translation
            if (StringUtils.isBlank(startDateStr) || startDateStr.equals("null"))
                startDateStr = "1911-01-01";
            if (StringUtils.isBlank(endDateStr) || endDateStr.equals("null"))
                endDateStr = "3099-01-01";
            if (StringUtils.isBlank(title) || title.equals("null"))
                title = "";
            if (StringUtils.isBlank(modifyUser) || modifyUser.equals("null"))
                modifyUser = "";

            // parse date data
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null, endDate = null;
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endDate = DateUtils.addDays(endDate, 1);
            logger.info("startDate:" + startDate);
            logger.info("endDate:" + endDate);
            logger.info("title:" + title);
            logger.info("modifyUser:" + modifyUser);

            // set file path
            String filePath = CoreConfigReader.getString("file.path");
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // set file name
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            Date date = new Date();
            String fileName = "LinePointStatisticReport_" + sdf2.format(date) + ".xlsx";

            // combine & export excel file
            String filePathAndName = filePath + System.getProperty("file.separator") + fileName;
            // 取得權限後，才能看到的資料
            List<LinePointMain> mains = linePointMainService.findByTitleAndModifyUserAndSendDate(startDate, endDate,
                    modifyUser, title);
            List<LinePointMain> result = competence(mains, customUser);
            logger.info("LinePointMain : " + mains);

            linePointReportExcelService.exportExcel_LinePointStatisticsReport(filePathAndName, result);
            LoadFileUIService.loadFileToResponse(filePath, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(ErrorRecord.recordError(e));
        }
    }

    @ControllerLog(description = "findLinePointDetailByMainId")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/findLinePointDetailByMainId")
    @ResponseBody
    public ResponseEntity<?> findAllLinePointDetailByMainId(HttpServletRequest request, HttpServletResponse response,
                                                            @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId
            , @RequestParam String startDateStr, @RequestParam String endDateStr) throws IOException {
        try {
            try {
                logger.info("[findLinePointDetailByMainId] linePointMainId:" + linePointMainId);


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = null, endDate = null;
                startDate = sdf.parse(startDateStr);
                endDate = sdf.parse(endDateStr);
                endDate = DateUtils.addDays(endDate, 1);
                logger.info("startDate:" + startDate);
                logger.info("endDate:" + endDate);

                // get Details
                List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainIdAndSendDate(linePointMainId, startDate, endDate);
                logger.info("linePointDetails:" + linePointDetails);
                return new ResponseEntity<>(linePointDetails, HttpStatus.OK);
            } catch (Exception e) {
                throw new BcsNoticeException(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


    // ---- Statistics Report Detail ----

    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportDetailExcel")
    @ResponseBody
    public void getLPStatisticsReportDetailExcel(HttpServletRequest request, HttpServletResponse response,
                                                 @CurrentUser CustomUser customUser, @RequestParam Long linePointMainId
            , @RequestParam String startDateStr, @RequestParam String endDateStr) throws IOException {
        try {
            logger.info("[getLPStatisticsReportDetailExcel]");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null, endDate = null;
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endDate = DateUtils.addDays(endDate, 1);
            logger.info("startDate:" + startDate);
            logger.info("endDate:" + endDate);


            // set file path
            String filePath = CoreConfigReader.getString("file.path");
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // set file name
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            Date date = new Date();
            String fileName = "LinePointStatisticReportDetail_" + sdf2.format(date) + ".xlsx";

            // combine & export excel file
            String filePathAndName = filePath + System.getProperty("file.separator") + fileName;
            linePointReportExcelService.exportExcel_LinePointStatisticsReportDetail(filePathAndName, linePointMainId, startDate, endDate);
            LoadFileUIService.loadFileToResponse(filePath, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(ErrorRecord.recordError(e));
        }
    }

    public List<LinePointMain> competence(List<LinePointMain> list, CustomUser customUser) throws Exception {
        List<LinePointMain> result = new ArrayList();

        // 取得權限
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
                // logger.info("employee : " + employee);

//				TaishinEmployee employee = new TaishinEmployee();
//				
//				employee.setDivisionName("XTREME");
//				employee.setDepartmentName("LINEBC");

                String Department = main.getDepartmentFullName();
                String[] Departmentname = Department.split(" ");
                // Departmentname[0]; 處 DIVISION_NAME
                // Departmentname[1]; 部 DEPARTMENT_NAME
                // Departmentname[2]; 組 GROUP_NAME

                // 判斷邏輯 如果登錄者有組 那只能看到同組 顧處部組全都要一樣，沒有組有部 那就是處跟部要一樣才可以，只有處 就是處一樣即可
                if (StringUtils.isNotBlank(employee.getGroupName())) {
                    if (Departmentname[0].equals(employee.getDivisionName())
                            && Departmentname[1].equals(employee.getDepartmentName())
                            && Departmentname[2].equals(employee.getGroupName())) {
                        result.add(main);
                    }
                } else if (StringUtils.isNotBlank(employee.getDepartmentName())) {
                    if (Departmentname[0].equals(employee.getDivisionName())
                            && Departmentname[1].equals(employee.getDepartmentName())) {
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
//	//  匯出 Push API 成效報表
//	@ControllerLog(description="匯出Line Point Push API 成效報表")
//    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffects")
//    @ResponseBody
//    public void exportToExcelForLPPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
//      
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "LPPushApiEffects_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName);
//        } catch (Exception e) {
//            logger.error(ErrorRecord.recordError(e));
//        }
//
//        try {
//			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//	// 匯出 Push API 成效報表
//	@ControllerLog(description="匯出Line Point Push API Detail 成效報表")
//    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffectsDetail/{mainId}/{status}")
//    @ResponseBody
//    public void exportToExcelForLPPushApiEffectsDetail(HttpServletRequest request, HttpServletResponse response, 
//    		@CurrentUser CustomUser customUser, @PathVariable Long mainId, @PathVariable String status) {
//      
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "LPPushApiEffectsDetail_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName, mainId, status);
//        } catch (Exception e) {
//            logger.error(ErrorRecord.recordError(e));
//        }
//
//        try {
//			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }

}
