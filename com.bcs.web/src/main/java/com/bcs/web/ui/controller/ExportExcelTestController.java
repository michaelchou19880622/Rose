package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.ExportExcelTestService;
import com.bcs.web.ui.service.LoadFileUIService;

@Controller
@RequestMapping("/bcs")
public class ExportExcelTestController {
    
    /** Logger */
    private static Logger logger = Logger.getLogger(ExportExcelTestController.class);
    
    @Autowired
    private ExportExcelTestService exportExcelTestService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/excelExportTestPage")
    public String excelExportTestPage(
            HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        logger.info("excelExportTestPage");
                
        return BcsPageEnum.ExcelExportTestPage.toString();
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportExcelTest")
    @ResponseBody
    public void exportExcelTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException{
        
        String sheetName = request.getParameter("sheetName");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        
        String linkUrl = request.getParameter("linkUrl");
        String keyword = request.getParameter("keyword");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "ReportTest_" + sheetName + "_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportExcelTestService.exportExcel(filePath, fileName, sheetName, startDate, endDate, linkUrl, keyword);
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    } 
}
