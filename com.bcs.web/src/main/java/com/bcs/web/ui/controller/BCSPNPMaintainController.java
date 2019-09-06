package com.bcs.web.ui.controller;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.PNPMaintainUIService;
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
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/bcs")
public class BCSPNPMaintainController extends BCSBaseController {
    @Autowired
    private PNPMaintainUIService pnpMaintainUIService;
    @Autowired
    private OracleService oraclePnpService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(BCSPNPMaintainController.class);

    @GetMapping(value = "/pnpAdmin/pnpNormalAccountListPage")
    public String pnpNormalAccountListPage() {
        logger.info("pnpNormalAccountListPage");
        return BcsPageEnum.PNPNormalAccountListPage.toString();
    }

    @GetMapping(value = "/pnpAdmin/pnpNormalAccountCreatePage")
    public String pnpNormalAccountCreatePage() {
        logger.info("pnpNormalAccountCreatePage");
        return BcsPageEnum.PNPNormalAccountCreatePage.toString();
    }

    @GetMapping(value = "/pnpAdmin/pnpUnicaAccountListPage")
    public String pnpUnicaAccountListPage() {
        logger.info("pnpUnicaAccountListPage");
        return BcsPageEnum.PNPUnicaAccountListPage.toString();
    }

    @GetMapping(value = "/pnpAdmin/pnpUnicaAccountCreatePage")
    public String pnpUnicaAccountCreatePage() {
        logger.info("pnpUnicaAccountCreatePage");
        return BcsPageEnum.PNPUnicaAccountCreatePage.toString();
    }

    /**
     * 透過員工代碼查詢員工資訊
     *
     * @param id Id
     * @return 員工資訊
     */
    @ResponseBody
    @GetMapping("/pnpAdmin/getEmpAccountInfo")
    public ResponseEntity<?> getEmpAccountInfo(@RequestParam("id") String id) {
        try {
            if (StringUtils.isBlank(id)) {
                logger.info("Employee ID is Blank!!");
                return new ResponseEntity<>("{}", HttpStatus.OK);
            }
            logger.info("Employee ID is " + id);

            TaishinEmployee taishinEmployee = oraclePnpService.findByEmployeeId(id);

            return new ResponseEntity<>(taishinEmployee, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            logger.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 透過員工代碼查詢員工資訊
     *
     * @param request    request
     * @param response   response
     * @param customUser
     * @param empId      員工ID
     * @return 員工資訊
     */
    @GetMapping(value = "/pnpAdmin/getEmpAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getEmpAccount(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                           @RequestParam(required = false) String empId) {
        try {

            if (StringUtils.isBlank(empId)) {
                logger.info("empId is blank");
                return new ResponseEntity<>("{}", HttpStatus.OK);
            }

            logger.info("getEmpAccount empId=" + empId);
            TaishinEmployee result;
            result = oraclePnpService.findByEmployeeId(empId);
            /* 更新時間及更新人員 */
            result.setModifyTime(new Date());
            result.setModifyUser(empId);
            oraclePnpService.save(result);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            logger.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/pnpAdmin/createPNPMaintainAccount", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createPNPMaintainAccount(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
                                                      @RequestBody PNPMaintainAccountModel pnpMaintainAccountModel) {
        try {
            logger.info("pnpMaintainAccountModel:" + pnpMaintainAccountModel);
            // Check Duplication
            String account = pnpMaintainAccountModel.getAccount();
            String sourceSystem = pnpMaintainAccountModel.getSourceSystem();
            String pnpContent = pnpMaintainAccountModel.getPnpContent();
            List<PNPMaintainAccountModel> sameCheck = pnpMaintainUIService.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
            logger.info("sameCheck:" + sameCheck);
            if (sameCheck.size() >= 2) {
                throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
            } else if (sameCheck.size() == 1) {
                Long addId = pnpMaintainAccountModel.getId();
                logger.info("addId:" + addId);
                // Create Mode
                if (addId == null) {
                    throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
                }

                Long sameId = sameCheck.get(0).getId();
                logger.info("sameId:" + sameId);

                //  Edit Mode & Not same Id
                if (sameId.longValue() != addId.longValue()) {
                    throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
                }
            }

            // save
            pnpMaintainAccountModel.setModifyTime(new Date());
            pnpMaintainAccountModel.setModifyUser(customUser.getAccount());
            pnpMaintainUIService.save(pnpMaintainAccountModel);

            return new ResponseEntity<>("save success", HttpStatus.OK);
        } catch (BcsNoticeException be) {
            logger.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/pnpAdmin/deletePNPMaintainAccount")
    @ResponseBody
    public ResponseEntity<?> deletePNPMaintainAccount(HttpServletRequest request, HttpServletResponse response,
                                                      @CurrentUser CustomUser customUser, @RequestParam Long id) {
        try {

            if (!customUser.isAdmin() && !AdminUser.RoleCode.ROLE_PNP_ADMIN.toString().equals(customUser.getRole())) {
                throw new BcsNoticeException("您無權限刪除");
            }

            PNPMaintainAccountModel pnpMaintainAccountModel = pnpMaintainUIService.findOne(id);
            if (pnpMaintainAccountModel == null) {
                throw new BcsNoticeException("刪除搜查錯誤");
            }

            pnpMaintainUIService.delete(pnpMaintainAccountModel);
            return new ResponseEntity<>("刪除成功", HttpStatus.OK);
        } catch (BcsNoticeException be) {
            logger.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/pnpAdmin/getPNPMaintainAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPMaintainAccount(
            HttpServletRequest request, HttpServletResponse response,
            @CurrentUser CustomUser customUser, @RequestParam Long id) {
        try {
            PNPMaintainAccountModel pnpMaintainAccountModel = pnpMaintainUIService.findOne(id);
            if (pnpMaintainAccountModel == null) {
                throw new BcsNoticeException("刪除搜查錯誤");
            }
            return new ResponseEntity<>(pnpMaintainAccountModel, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            logger.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/pnpAdmin/getPNPMaintainAccountList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPMaintainAccountList(
            HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
            @RequestBody PNPMaintainAccountModel pnpMaintainAccountModel, @RequestParam Boolean status) {
        try {
            logger.info("pnpMaintainAccountModel1:" + pnpMaintainAccountModel);
            String divisionName = pnpMaintainAccountModel.getDivisionName();
            String departmentName = pnpMaintainAccountModel.getDepartmentName();
            String groupName = pnpMaintainAccountModel.getGroupName();
            String pccCode = pnpMaintainAccountModel.getPccCode();
            String account = pnpMaintainAccountModel.getAccount();
            String employeeId = pnpMaintainAccountModel.getEmployeeId();
            String accountType = pnpMaintainAccountModel.getAccountType();

            List<PNPMaintainAccountModel> list = pnpMaintainUIService.queryUsePageCoditions(
                    divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 匯出 EXCEL
//	@ControllerLog(description="匯出 EXCEL")
//    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPNPMaintainAccount")
//    @ResponseBody
//    public void exportToExcelForBNPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
//    		@RequestParam String divisionName, @RequestParam String departmentName, @RequestParam String groupName, @RequestParam String pccCode,
//    		@RequestParam String account, @RequestParam String employeeId, @RequestParam String accountType) throws IOException {
//		
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "PNPMaintainAccount_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            pnpMaintainExcelService.exportExcel(filePath, fileName, divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
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

//	@RequestMapping(method = RequestMethod.GET, value = "/edit/findAll/{maxRange}", produces = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> findAll(HttpServletRequest request,  HttpServletResponse response, 
//			@CurrentUser CustomUser customUser, @PathVariable Integer maxRange) throws IOException {		
//		try {
//			logger.info("findAll maxRange=" + maxRange);
//			oraclePnpService.findAll(maxRange);	
//			return new ResponseEntity<>("{}", HttpStatus.OK);
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
}
