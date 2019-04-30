package com.bcs.core.bot.report.export;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.report.service.InteractiveReportService;
import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.service.ContentReportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForContentPushReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForContentPushReport.class);

	@Autowired
	private ExportToExcelUserGroup exportToExcelUserGroup;	
	@Autowired
	private InteractiveReportService interactiveReportService;
	@Autowired
	private ContentReportService contentReportService;
	
	/**
	 * 匯出EXCEL
	 */
	public void exportToExcel(String exportPath, String startDate, String endDate, String fileName) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			/**
			 * ExportReportToExcelLinkRepot
			 */
			Sheet sheetLink = wb.createSheet("Send Report"); // create a new sheet
			this.exportReportToExcelLinkReport(wb, sheetLink, startDate, endDate);
			
			/**
			 * ExportReportToExcelUserGroup
			 */
			Sheet sheetUserGroup = wb.createSheet("User Group"); // create a new sheet
			exportToExcelUserGroup.exportToExcelUserGroup(sheetUserGroup, startDate, endDate);
			
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
	 * Export Report To Excel Link Report
	 * @param wb
	 * @param sheet
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	public void exportReportToExcelLinkReport(Workbook wb, Sheet sheet, String startDate, String endDate) throws Exception{

		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("序號");
		row.createCell(1).setCellValue("發送日期");
		row.createCell(2).setCellValue("訊息類別");
		row.createCell(3).setCellValue("圖片");
		row.createCell(4).setCellValue("文字");
		row.createCell(5).setCellValue("縮址");
		row.createCell(6).setCellValue("Tracking Code");
		row.createCell(7).setCellValue("收訊人數");
		row.createCell(8).setCellValue("Clicks");
		row.createCell(9).setCellValue("TTL Clicks");
		row.createCell(10).setCellValue("CTR");
		row.createCell(11).setCellValue("Click-Throughs");
		row.createCell(12).setCellValue("Visits");
		row.createCell(13).setCellValue("Product Views");
		row.createCell(14).setCellValue("深度");
		row.createCell(15).setCellValue("TTL Click-throughs");
		row.createCell(16).setCellValue("TTL Visits");
		row.createCell(17).setCellValue("TTL Product Views ");
		row.createCell(18).setCellValue("TTL 深度");
		
		//取得匯出資料
		List<PushReport> list = contentReportService.getReportList(startDate, endDate);
		
		if(list != null && list.size() > 0){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			//設定圖片欄位的寬度(16個英文字寬)
			sheet.setColumnWidth(3, 16*256); //第四欄
			int seqNo = 1; //序號
			int firstIndex = 0; //當取得richMsg時，記錄index，以便做合併儲存格
			int sameRichCount = 0; //相同richId其連結數
			for (int i=1; i<=list.size(); i++) {
				PushReport pushReport = list.get(i-1);
				if (sameRichCount == 0) {
					firstIndex = i;
					// Define a few rows
					Row row1 = sheet.createRow(i);
					row1.setHeight((short)768); //三個字高度
					row1.createCell(0).setCellValue(checkNull(seqNo));
					row1.createCell(1).setCellValue(checkNull(sdf.format(pushReport.getReportPushTime())));
					row1.createCell(2).setCellValue(checkNull(pushReport.getReportPushType()));
					//跳過第三欄圖片欄
					row1.createCell(4).setCellValue(checkNull(pushReport.getReportPushTxt()));
					row1.createCell(5).setCellValue(checkNull(pushReport.getReportPushUrl()));
					row1.createCell(6).setCellValue(checkNull(pushReport.getReportPushTrackingCode()));
					row1.createCell(7).setCellValue(checkNull(pushReport.getReportPushNumber()));
					row1.createCell(8).setCellValue(checkNull(pushReport.getReportPushUrlClick()));
					row1.createCell(9).setCellValue(checkNull(pushReport.getReportPushTotalClick()));
					row1.createCell(10).setCellValue(checkNull(pushReport.getReportPushCtrValue()) + "%");
					row1.createCell(11).setCellValue(checkNull(pushReport.getReportPushUrlClickThrough()));
					row1.createCell(12).setCellValue(checkNull(pushReport.getReportPushUrlVisit()));
					row1.createCell(13).setCellValue(checkNull(pushReport.getReportPushUrlProductView()));
					row1.createCell(14).setCellValue(checkNull(pushReport.getReportPushUrlDeep()));
					row1.createCell(15).setCellValue(checkNull(pushReport.getReportPushTotalClickThrough()));
					row1.createCell(16).setCellValue(checkNull(pushReport.getReportPushTotalVisit()));
					row1.createCell(17).setCellValue(checkNull(pushReport.getReportPushTotalProductView()));
					row1.createCell(18).setCellValue(checkNull(pushReport.getReportPushTotalDeep()));
				} else {
					Row row2 = sheet.createRow(i);
					row2.setHeight((short)768);
					row2.createCell(4).setCellValue(checkNull(pushReport.getReportPushTxt()));
					row2.createCell(5).setCellValue(checkNull(pushReport.getReportPushUrl()));
					row2.createCell(6).setCellValue(checkNull(pushReport.getReportPushTrackingCode()));
					row2.createCell(8).setCellValue(checkNull(pushReport.getReportPushUrlClick()));
					row2.createCell(11).setCellValue(checkNull(pushReport.getReportPushUrlClickThrough()));
					row2.createCell(12).setCellValue(checkNull(pushReport.getReportPushUrlVisit()));
					row2.createCell(13).setCellValue(checkNull(pushReport.getReportPushUrlProductView()));
					row2.createCell(14).setCellValue(checkNull(pushReport.getReportPushUrlDeep()));
				}
				
				if (StringUtils.isNotBlank(pushReport.getReportRichId())) { //屬於richMsg
					if (i < list.size()) {
						PushReport nextReport = list.get(i);
						if (pushReport.getReportMsgSendId().equals(nextReport.getReportMsgSendId())) { //下一筆的MsgSendId與此筆相同
							sameRichCount++;
							continue;
						}
					}
				}
				
				if (sameRichCount > 0) {
					//序號、發送日期、訊息類別、收訊人數、TTL Clicks、CTR、TTL Click-throughs、TTL Visits、TTL Product Views、TTL 深度
					int cells[] = {0, 1, 2, 3, 7, 9, 10, 15, 16, 17, 18};
					for (int cell : cells) {
						//合併儲存格
						CellRangeAddress region = new CellRangeAddress(firstIndex, firstIndex+sameRichCount, cell, cell); //CellRangeAddress(firstRow, lastRow, firstCol, lastCol)
						sheet.addMergedRegion(region);
					}
				}
				
				if (StringUtils.isNotBlank(pushReport.getReportImageId())) {
					FileInputStream fis = new FileInputStream(CoreConfigReader.getString("file.path") + "/IMAGE/IMAGE/" + pushReport.getReportImageId()); //縮圖路徑
					byte[] imageBytes = IOUtils.toByteArray(fis);
					int pictureIndex = wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
					Drawing drawing = sheet.createDrawingPatriarch();
					CreationHelper helper = wb.getCreationHelper();
					ClientAnchor anchor = helper.createClientAnchor();
					anchor.setRow1(firstIndex); //長2列
					anchor.setRow2(firstIndex+2);
					anchor.setCol1(3); //寬1欄
					anchor.setCol2(4);
					Picture pic = drawing.createPicture(anchor, pictureIndex);
					pic.resize(1, 1); //fit定位欄列的大小(不設定載入原圖大小)
					fis.close();
				}
				
				seqNo++;
				sameRichCount = 0;
			}
			
//			for (int colNum=0; colNum<row.getLastCellNum(); colNum++) {
//				if (colNum == 3) continue; //跳過圖片欄位
//				sheet.autoSizeColumn(colNum);
//			}
		}
	}
	
	private int checkNull(Integer obj){
		
		if(obj == null){
			return 0;
		}
		
		return obj;
	}
	
	private String checkNull(BigDecimal obj){
		
		if(obj == null){
			return "0";
		}
		
		return obj.toString();
	}
	
	private String checkNull(String obj){
		
		if(obj == null){
			return "";
		}
		
		return obj;
	}
	
	private Long checkNull(Long obj){
		
		if(obj == null){
			return 0L;
		}
		
		return obj;
	}
}
