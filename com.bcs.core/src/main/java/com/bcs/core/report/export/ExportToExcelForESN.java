package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.entity.ContentEsnMain;
import com.bcs.core.db.service.ContentEsnDetailService;
import com.bcs.core.db.service.ContentEsnMainService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForESN {
    
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForESN.class);

	@Autowired
	private ContentEsnMainService contentEsnMainService;
	@Autowired
	private ContentEsnDetailService contentEsnDetailService;
	
	/**
	 * 匯出 
	 */
	public void exportToExcel(String exportPath, String fileName, String esnId) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
	        ContentEsnMain main = contentEsnMainService.findOne(esnId);
			
			Sheet sheetLink = wb.createSheet(main.getEsnName()); // create a new sheet
			
			this.exportFinishEsnDetail(wb, sheetLink, esnId);
			
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void exportFinishEsnDetail(Workbook wb, Sheet sheet, String esnId) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        List<ContentEsnDetail> details = contentEsnDetailService.findByEsnId(esnId);
        
        Row row = sheet.createRow(0); // declare a row object reference
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("電子序號");
        row.createCell(2).setCellValue("發送時間");
        row.createCell(3).setCellValue("發送狀態");
        
        if(details.size() != 0){
            int seqNo = 1; //序號
            
            for(ContentEsnDetail detail : details) {

                Row row1 = sheet.createRow(seqNo);
                row1.createCell(0).setCellValue(detail.getUid() == null? "" : detail.getUid());
                row1.createCell(1).setCellValue(detail.getEsn() == null? "" : detail.getEsn());
                row1.createCell(2).setCellValue(detail.getSendTime() == null? "" : sdf.format(detail.getSendTime()));
                row1.createCell(3).setCellValue(status2Chinese(detail.getStatus().toUpperCase()));
                
                seqNo++;
            }
        }
    }
	
	private String status2Chinese(String status) {
	    switch(status) {
    	    case ContentEsnDetail.STATUS_FINISH : return "成功"; 
    	    case ContentEsnDetail.STATUS_FAIL : return "失敗";
    	    case ContentEsnDetail.STATUS_READY : return "待發送";
    	    default : return "";
	    }
	}
}
