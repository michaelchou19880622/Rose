package com.bcs.core.report.service;

import com.bcs.core.report.builder.ExportExcelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class ExportService {
    public void exportExcel(HttpServletResponse response, ExportExcelBuilder builder){
        builder.export();
        try (
                InputStream inputStream = new FileInputStream(builder.getOutPutPath() + builder.getOutputFileName())
        ){
            response.setContentType("application/download; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + builder.getOutputFileName());
            response.setCharacterEncoding("UTF-8");
            OutputStream outputStream = response.getOutputStream();
            log.info("[loadFileToResponse]");
            IOUtils.copy(inputStream, outputStream);

            response.flushBuffer();
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            log.error("Exception");
        }
    }
}
