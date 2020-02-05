package com.bcs.web.ui.controller;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.taishin.circle.pnp.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpFlexTemplate;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.PNPMaintainUIService;
import com.bcs.web.ui.service.PnpFlexTemplateService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/bcs")
public class BCSPNPMaintainController extends BCSBaseController {
    private PNPMaintainUIService pnpMaintainUIService;
    private OracleService oraclePnpService;
    private SystemConfigService systemConfigService;
    private PnpFlexTemplateService pnpFlexTemplateService;

    @Autowired
    public BCSPNPMaintainController(PNPMaintainUIService pnpMaintainUIService,
                                    OracleService oraclePnpService,
                                    SystemConfigService systemConfigService,
                                    PnpFlexTemplateService pnpFlexTemplateService) {
        this.pnpMaintainUIService = pnpMaintainUIService;
        this.oraclePnpService = oraclePnpService;
        this.systemConfigService = systemConfigService;
        this.pnpFlexTemplateService = pnpFlexTemplateService;
    }


    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/pnpNormalAccountListPage")
    public String pnpNormalAccountListPage(Model model) {
        log.info("pnpNormalAccountListPage");
        model.addAttribute("t", "Normal");
        return BcsPageEnum.PNPNormalAccountListPage.toString();
    }

    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/pnpNormalAccountCreatePage")
    public String pnpNormalAccountCreatePage(Model model) {
        log.info("pnpNormalAccountCreatePage");
        model.addAttribute("t", "Normal");
        return BcsPageEnum.PNPNormalAccountCreatePage.toString();
    }

    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/pnpUnicaAccountListPage")
    public String pnpUnicaAccountListPage(Model model) {
        log.info("pnpUnicaAccountListPage");
        model.addAttribute("t", "Unica");
        return BcsPageEnum.PNPNormalAccountListPage.toString();
    }

    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/pnpUnicaAccountCreatePage")
    public String pnpUnicaAccountCreatePage(Model model) {
        log.info("pnpUnicaAccountCreatePage");
        model.addAttribute("t", "Unica");
        return BcsPageEnum.PNPNormalAccountCreatePage.toString();
    }

    /**
     * 透過員工代碼查詢員工資訊
     *
     * @param id Id
     * @return 員工資訊
     */
    @ResponseBody
    @WebServiceLog
    @GetMapping("/pnpAdmin/getEmpAccountInfo")
    public ResponseEntity<?> getEmpAccountInfo(@RequestParam("id") String id) {
        try {
            if (StringUtils.isBlank(id)) {
                log.info("Employee ID is Blank!!");
                return new ResponseEntity<>("{}", HttpStatus.OK);
            }
            log.info("Employee ID is " + id);

            TaishinEmployee taishinEmployee = oraclePnpService.findByEmployeeId(id);

            return new ResponseEntity<>(taishinEmployee, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 透過員工代碼查詢員工資訊
     *
     * @param customUser customUser
     * @param empId      員工ID
     * @return 員工資訊
     */
    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/getEmpAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getEmpAccount(@CurrentUser CustomUser customUser, @RequestParam(required = false) String empId) {
        try {

            if (StringUtils.isBlank(empId)) {
                log.info("empId is blank");
                return new ResponseEntity<>("{}", HttpStatus.OK);
            }

            log.info("getEmpAccount empId=" + empId);
            TaishinEmployee result;
            result = oraclePnpService.findByEmployeeId(empId);
            /* 更新時間及更新人員 */
            result.setModifyTime(new Date());
            result.setModifyUser(empId);
            oraclePnpService.save(result);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/getFlexConfig/{templateId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getUserFlexMessageConfig(@PathVariable("templateId") Long templateId) {
        try {
            PnpFlexTemplate pnpFlexTemplate = pnpFlexTemplateService.getTemplate(templateId);
            if (pnpFlexTemplate == null || pnpFlexTemplate.getId() == null) {
                throw new BcsNoticeException("取得樣板失敗");
            }
            log.info("pnpFlexTemplate : {}", DataUtils.toPrettyJsonUseJackson(pnpFlexTemplate));
            return new ResponseEntity<>(pnpFlexTemplate, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 提供FlexUI設定檔
     *
     * @return FlexUI設定檔
     */
    @WebServiceLog
    @GetMapping(value = "/pnpAdmin/getFlexConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getFlexMessageConfig() {
        try {
            List<Object[]> configList = systemConfigService.findLikeConfigId(".flex.ui.%");
            Map<String, String> configMap = new HashMap<>();
            for (Object[] configArray : configList) {
                if (configArray.length >= 2) {
                    String key = configArray[0].toString();
                    String value = configArray[1].toString();
                    configMap.put(key, value);
                }
            }

            log.info(DataUtils.toPrettyJsonUseJackson(configMap));

            Map<String, Object> headerMap = getHeaderMap(configMap);
            Map<String, Object> descriptionMap = getDescMap(configMap);
            Map<String, Object> buttonMap = getButtonMap(configMap);
            Map<String, Object> bodyMap = getBodyMap(configMap);
            bodyMap.put("description", descriptionMap);
            bodyMap.put("button", buttonMap);

            Map<String, Object> heroMap = getHeroMap(configMap);
            Map<String, Object> footerMap = getFooterMap(configMap);
            Map<String, Object> response = getResponseMap(configMap);
            response.put("header", headerMap);
            response.put("hero", heroMap);
            response.put("body", bodyMap);
            response.put("footer", footerMap);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> getResponseMap(final Map<String, String> configMap) {
        Map<String, Object> response = new HashMap<>(18);
        response.put("isEnableHeaderCustomBackground", getOrDefault(configMap, ".flex.ui.header.custom.background.enable", false));
        response.put("isEnableHeaderCustomTextSize", getOrDefault(configMap, ".flex.ui.header.custom.text.size.enable", true));
        response.put("isEnableHeaderCustomTextColor", getOrDefault(configMap, ".flex.ui.header.custom.text.color.enable", true));
        response.put("isEnableHeaderCustomTextWeight", getOrDefault(configMap, ".flex.ui.header.custom.text.weight.enable", true));
        response.put("isEnableHeaderCustomTextStyle", getOrDefault(configMap, ".flex.ui.header.custom.text.style.enable", true));
        response.put("isEnableHeaderCustomTextDecoration", getOrDefault(configMap, ".flex.ui.header.custom.text.decoration.enable", true));
        response.put("isEnableHeroCustomBackground", getOrDefault(configMap, ".flex.ui.hero.custom.background.enable", false));
        response.put("isEnableHeroCustomTextSize", getOrDefault(configMap, ".flex.ui.hero.custom.text.size.enable", true));
        response.put("isEnableHeroCustomTextColor", getOrDefault(configMap, ".flex.ui.hero.custom.text.color.enable", true));
        response.put("isEnableHeroCustomTextWeight", getOrDefault(configMap, ".flex.ui.hero.custom.text.weight.enable", true));
        response.put("isEnableHeroCustomTextStyle", getOrDefault(configMap, ".flex.ui.hero.custom.text.style.enable", true));
        response.put("isEnableHeroCustomTextDecoration", getOrDefault(configMap, ".flex.ui.hero.custom.text.decoration.enable", true));
        response.put("isEnableBodyButtonCustomColor", getOrDefault(configMap, ".flex.ui.body.button.custom.color.enable", true));
        response.put("isEnableBodyButtonCustomStyle", getOrDefault(configMap, ".flex.ui.body.button.custom.style.enable", true));
        response.put("buttonMaxQuantity", getOrDefault(configMap, ".flex.ui.button.max.quantity", 3));

        return response;
    }

    private Map<String, Object> getFooterMap(final Map<String, String> configMap) {
        Map<String, Object> footerMap = new HashMap<>(2);
        footerMap.put("linkText", getOrDefault(configMap, ".flex.ui.footer.link.text", "收到此訊息的原因是？"));
        footerMap.put("linkUrl", getOrDefault(configMap, ".flex.ui.footer.link.url", "https://tsbk.tw/LINEPNP/"));
        return footerMap;
    }

    private Map<String, Object> getHeroMap(final Map<String, String> configMap) {
        Map<String, Object> heroMap = new HashMap<>(7);
        heroMap.put("background", getOrDefault(configMap, ".flex.ui.hero.default.background", "#FFDDDD"));
        heroMap.put("textSize", getOrDefault(configMap, ".flex.ui.hero.default.text.size", "md"));
        heroMap.put("textColor", getOrDefault(configMap, ".flex.ui.hero.default.text.color", "#000000"));
        heroMap.put("textWeight", getOrDefault(configMap, ".flex.ui.hero.default.text.weight", "regular"));
        heroMap.put("textStyle", getOrDefault(configMap, ".flex.ui.hero.default.text.style", "normal"));
        heroMap.put("textDecoration", getOrDefault(configMap, ".flex.ui.hero.default.text.decoration", "none"));
        heroMap.put("text", getOrDefault(configMap, ".flex.ui.hero.default.text", "這是 PNP Flex 訊息內容!!"));
        return heroMap;
    }

    private Map<String, Object> getBodyMap(final Map<String, String> configMap) {
        Map<String, Object> bodyMap = new HashMap<>(3);
        bodyMap.put("background", getOrDefault(configMap, "", "#FFFFFF"));
        return bodyMap;
    }

    private Map<String, Object> getButtonMap(final Map<String, String> configMap) {
        Map<String, Object> buttonMap = new HashMap<>(3);
        buttonMap.put("buttonColor", getOrDefault(configMap, ".flex.ui.body.default.button.color", "#D80C18"));
        buttonMap.put("buttonStyle", getOrDefault(configMap, ".flex.ui.body.default.button.style", "primary"));
        buttonMap.put("buttonText", getOrDefault(configMap, ".flex.ui.body.default.button.text", "Button!!"));
        return buttonMap;
    }

    private Map<String, Object> getDescMap(final Map<String, String> configMap) {
        Map<String, Object> descriptionMap = new HashMap<>(6);
        descriptionMap.put("textSize", getOrDefault(configMap, ".flex.ui.hero.default.desc.text.size", "xs"));
        descriptionMap.put("textColor", getOrDefault(configMap, ".flex.ui.hero.default.desc.text.color", "#0000FF"));
        descriptionMap.put("textWeight", getOrDefault(configMap, ".flex.ui.hero.default.desc.text.weight", "regular"));
        descriptionMap.put("textStyle", getOrDefault(configMap, ".flex.ui.hero.default.desc.text.style", "normal"));
        descriptionMap.put("textDecoration", getOrDefault(configMap, ".flex.ui.hero.default.desc.text.decoration", "none"));
        descriptionMap.put("text", getOrDefault(configMap, ".flex.ui.body.default.desc.text",
                "此通知在用戶簽約時所註冊之電話號碼和LINE上所註冊之電話號碼一致時發送。"));
        return descriptionMap;
    }

    private Map<String, Object> getHeaderMap(final Map<String, String> configMap) {
        Map<String, Object> headerMap = new HashMap<>(7);
        headerMap.put("background", getOrDefault(configMap, ".flex.ui.header.default.background", "#D80C18"));
        headerMap.put("textSize", getOrDefault(configMap, ".flex.ui.header.default.text.size", "md"));
        headerMap.put("textColor", getOrDefault(configMap, ".flex.ui.header.default.text.color", "#000000"));
        headerMap.put("textWeight", getOrDefault(configMap, ".flex.ui.header.default.text.weight", "regular"));
        headerMap.put("textStyle", getOrDefault(configMap, ".flex.ui.header.default.text.style", "normal"));
        headerMap.put("textDecoration", getOrDefault(configMap, ".flex.ui.header.default.text.decoration", "none"));
        headerMap.put("text", getOrDefault(configMap, ".flex.ui.header.default.text", "訊息標題"));
        return headerMap;
    }

    private Object getOrDefault(Map map, Object key, Object defaultValue) {
        return map.get(key) != null || map.containsKey(key) ? map.get(key) : defaultValue;
    }


    /**
     * Create PNP Maintain Account
     *
     * @return ?
     */
    @SuppressWarnings("unchecked")
    @WebServiceLog
    @PostMapping(value = "/pnpAdmin/createPNPMaintainAccount", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createPNPMaintainAccount(@CurrentUser CustomUser customUser, @RequestBody Map<String, Object> data) {
        try {
            log.info("data: {}", DataUtils.toPrettyJsonUseJackson(data));

            if (data.containsKey("flexTemplate") && data.get("flexTemplate") != null) {
                Map<String, Object> templateMap = new Gson().fromJson(Objects.toString(data.get("flexTemplate")), HashMap.class);

                PnpFlexTemplate pnpFlexTemplate = pnpFlexTemplateService.saveTemplate(templateMap);
                log.info("pnpFlexTemplate:{}", DataUtils.toPrettyJsonUseJackson(pnpFlexTemplate));
                if (pnpFlexTemplate == null || pnpFlexTemplate.getId() == null) {
                    throw new BcsNoticeException("儲存白名單及樣板失敗");
                }

                if (data.containsKey("maintainAccount") && data.get("maintainAccount") != null) {
                    PNPMaintainAccountModel pnpMaintainAccountModel = new Gson().fromJson((String) data.get("maintainAccount"), PNPMaintainAccountModel.class);
                    log.info(DataUtils.toPrettyJsonUseJackson(pnpMaintainAccountModel));
                    pnpMaintainAccountModel.setTemplate(pnpFlexTemplate.getId() == null ? "0" : pnpFlexTemplate.getId().toString());
                    validMaintainAccountModel(pnpMaintainAccountModel);
                    log.info("pnpMaintainAccountModel:{}", DataUtils.toPrettyJsonUseJackson(pnpMaintainAccountModel));
                    saveMaintainAccountModel(customUser, pnpMaintainAccountModel);
                }
            }
            return new ResponseEntity<>("save success", HttpStatus.OK);
        } catch (BcsNoticeException be) {
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveMaintainAccountModel(@CurrentUser CustomUser customUser, @RequestBody PNPMaintainAccountModel pnpMaintainAccountModel) {
        // save
        pnpMaintainAccountModel.setModifyTime(new Date());
        pnpMaintainAccountModel.setModifyUser(customUser.getAccount());
        pnpMaintainUIService.save(pnpMaintainAccountModel);
    }

    private void validMaintainAccountModel(@RequestBody PNPMaintainAccountModel pnpMaintainAccountModel) throws BcsNoticeException {
        String account = pnpMaintainAccountModel.getAccount();
        String sourceSystem = pnpMaintainAccountModel.getSourceSystem();
        String pnpContent = pnpMaintainAccountModel.getPnpContent();
        List<PNPMaintainAccountModel> sameCheck = pnpMaintainUIService.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
        log.info("sameCheck:" + sameCheck);
        if (sameCheck.size() > 1) {
            throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
        } else if (sameCheck.size() == 1) {
            // Create Mode
            if (pnpMaintainAccountModel.getId() == null) {
                throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
            }
            // Edit Mode & Not same Id
            if (sameCheck.get(0).getId().longValue() != pnpMaintainAccountModel.getId().longValue()) {
                throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！");
            }
        }
    }

    @WebServiceLog
    @DeleteMapping(value = "/pnpAdmin/deletePNPMaintainAccount")
    @ResponseBody
    public ResponseEntity<?> deletePNPMaintainAccount(@CurrentUser CustomUser customUser, @RequestParam Long id) {
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
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @PostMapping(value = "/pnpAdmin/getPNPMaintainAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPMaintainAccount(@CurrentUser CustomUser customUser, @RequestParam Long id) {
        try {
            PNPMaintainAccountModel pnpMaintainAccountModel = pnpMaintainUIService.findOne(id);
            if (pnpMaintainAccountModel == null) {
                throw new BcsNoticeException("刪除搜查錯誤");
            }
            return new ResponseEntity<>(pnpMaintainAccountModel, HttpStatus.OK);
        } catch (BcsNoticeException be) {
            log.error(ErrorRecord.recordError(be));
            return new ResponseEntity<>(be.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WebServiceLog
    @PostMapping(value = "/pnpAdmin/getPNPMaintainAccountList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getPNPMaintainAccountList(@CurrentUser CustomUser customUser,
            @RequestBody PNPMaintainAccountModel pnpMaintainAccountModel, @RequestParam Boolean status) {
        try {
            log.info("pnpMaintainAccountModel1:" + pnpMaintainAccountModel);
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
            log.error("", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
