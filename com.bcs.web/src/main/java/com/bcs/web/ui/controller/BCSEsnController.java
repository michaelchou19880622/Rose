package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentEsnMain;
import com.bcs.core.db.service.ContentEsnMainService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.report.export.ExportToExcelForESN;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.EsnUIService;
import com.bcs.web.ui.service.LoadFileUIService;

@Controller
@RequestMapping("/bcs")
public class BCSEsnController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSEsnController.class);

	@Autowired
	private EsnUIService esnUIService;
	@Autowired
	private ContentEsnMainService contentEsnMainService;
	@Autowired
	private ExportToExcelForESN exportToExcelForESN;
	
	/**
	 * 電子序號建立頁面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/esnCreatePage")
	public String esnCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("esnCreatePage");
		return BcsPageEnum.EsnCreatePage.toString();
	}

	/**
	 * 電子序號列表頁面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/esnListPage")
	public String esnListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("esnListPage");
		return BcsPageEnum.EsnListPage.toString();
	}
	
	/**
     * 建立電子序號
     * 
     * @param request
     * @param response
     * @param esnId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/admin/createEsn")
    public ResponseEntity<?> createEsn(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "esnId", required = false) String esnId,
            @RequestParam(value = "esnName") String esnName,
            @RequestParam(value = "esnMsg") String esnMsg,
            @CurrentUser CustomUser customUser) {
        logger.info("createEsn");
        try {
            // 檢查是否為Admin
            if (customUser.isAdmin()) {
                esnUIService.createEsnData(esnId, esnName, esnMsg, file, customUser);
            } else {
                throw new BcsNoticeException("此帳號沒有建立電子序號權限");
            }
            return new ResponseEntity<>("Create Success", HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            } else {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/edit/getContentEsnMain")
    @ResponseBody
    public ResponseEntity<?> getContentEsnMain(
            @RequestParam String esnId,  
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("getContentEsnMain");                
        
        try{
            if(esnId != null){
                logger.info("esnId:" + esnId);
                ContentEsnMain contentEsnMain = contentEsnMainService.findOne(esnId);
                
                if(contentEsnMain != null){
                    return new ResponseEntity<>(contentEsnMain, HttpStatus.OK);
                }
            }
            
            throw new Exception("ContentEsnMain Null");
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));
            
            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * 取得電子序號列表清單
     * 
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit/getEsnList")
    public ResponseEntity<?> getEsnList(HttpServletRequest request, HttpServletResponse response) {
        logger.info("getEsnList");
        
        try {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            
            List<String> statusList = new ArrayList<>();
            statusList.add(ContentEsnMain.STATUS_ACTIVE);
            statusList.add(ContentEsnMain.STATUS_DISABLE);
            
            List<Object[]> list = contentEsnMainService.findDataByStatusAndModifyTime(statusList, startDate, endDate);
            
            return new ResponseEntity<>(convertToMapList(list), HttpStatus.OK);
            
        }catch(Exception e){
            logger.error(ErrorRecord.recordError(e));
            
            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    private List<Map<String, Object>> convertToMapList(List<Object[]> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for(Object[] o : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("esnId", o[0]);
            map.put("esnName", o[1]);
            map.put("esnMsg", o[2]);
            map.put("modifyTime", o[3]);
            map.put("modifyUser", o[4]);
            map.put("sendStatus", o[5]);
            map.put("status", o[6]);
            map.put("totalCount", o[7]);
            map.put("finishCount", o[8]);
            
            result.add(map);
        }
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/edit/sendEsnMsg")
    @ResponseBody
    public ResponseEntity<?> sendEsnMsg(
            @RequestParam(value = "esnId") String esnId, 
            @CurrentUser CustomUser customUser, 
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("sendEsnMsg");
        
        try{
            esnUIService.sendEsnMsg(esnId, customUser);
            
            return new ResponseEntity<>("success", HttpStatus.OK);
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));
            
            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForEsnFinish")
    @ResponseBody
    public void exportToExcelForEsnFinish(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String esnId = request.getParameter("esnId");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        
        Date date = new Date();
        String fileName = "SENT_ESN_"+ sdf.format(date) + ".xlsx";
        
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportToExcelForESN.exportToExcel(filePath, fileName, esnId);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
}
