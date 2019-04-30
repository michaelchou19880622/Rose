package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.report.service.ContentCouponReportService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForCouponReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForCouponReport.class);

	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentCouponReportService contentCouponReportService;
	
	/**
	 * 匯出 Coupon Report EXCEL
	 */
	public void exportToExcelForCouponReport(String exportPath, String fileName, String startDate, String endDate,  String couponId) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetCoupon = wb.createSheet("Coupon Report"); // create a new sheet
			this.exportToExcelForCouponReport(wb, sheetCoupon, startDate, endDate, couponId);
			
			// Save
			FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
	}
	
	/**
	 * Export To Excel For Coupon Report
	 * @param wb
	 * @param sheet
	 * @param startDate
	 * @param endDate
	 * @param iMsgId
	 * @param userStatus
	 * @throws Exception
	 */
	public void exportToExcelForCouponReport(Workbook wb, Sheet sheet, String startDate, String endDate, String couponId) throws Exception{
		
		ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
			
		if(contentCoupon == null){
			throw new Exception("CouponId Error");
		}
			
		String couponTitle = contentCoupon.getCouponTitle();
			
		//取得匯出資料
		Map<String, Map<String, Long>> resultGet = contentCouponReportService.getCouponGetReport(startDate, endDate, couponId);
		Map<String, Map<String, Long>> resultUse = contentCouponReportService.getCouponUseReport(startDate, endDate, couponId);
		
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("日期");
		row.createCell(1).setCellValue("優惠券");
		row.createCell(2).setCellValue("開封件數");
		row.createCell(3).setCellValue("使用張數");
		
		if(resultGet.size() != 0){
			int seqNo = 1; //序號
			for (String responseDate : resultGet.keySet()) {
				Map<String, Long> dataMapGet = resultGet.get(responseDate);
				Map<String, Long> dataMapUse = resultUse.get(responseDate);
					
				Long getCount = dataMapGet.get(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_COUNT.toString());
				if(getCount == null){
					getCount = 0L;
				}
				
				Long useCount = dataMapUse.get(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_COUNT.toString());
				if(useCount == null){
					useCount = 0L;
				}
				
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(responseDate);
				row1.createCell(1).setCellValue(couponTitle);
				row1.createCell(2).setCellValue(getCount);
				row1.createCell(3).setCellValue(useCount);
				
				seqNo++;
				
//				for (int colNum=0; colNum<row.getLastCellNum(); colNum++) {
//					sheet.autoSizeColumn(colNum);
//				}
			}
		}
	}
}
