package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForRewardCardReport {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForRewardCardReport.class);
	
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	/**
	 * 匯出 Winner List EXCEL
	 */
	public void exportToExcelForWinnerList(String exportPath, String fileName, String startDate, String endDate, String rewardCardId,Optional<Integer> pageIndex) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetCoupon = wb.createSheet("Winner List"); // create a new sheet
			this.exportToExcelForRewardCardGetPointRecord(wb, sheetCoupon, startDate, endDate, rewardCardId,pageIndex);
			
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
	 * 匯出 Reward Card Coupon 兌禮紀錄
	 */
	public void exportToExcelForRewardCardCouponRecord(String exportPath, String fileName,String rewardCardId) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); //→xls // new XSSFWorkbook()→xlsx
			
			Sheet sheetCoupon = wb.createSheet("Winner List"); // create a new sheet
			this.exportToExcelForRewardCardCouponRecord(wb, sheetCoupon,rewardCardId);
			
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
	 * Export To Excel For Winner List
	 * @param wb
	 * @param sheet
	 * @throws Exception
	 */
	private void exportToExcelForRewardCardGetPointRecord(Workbook wb, Sheet sheet, String startDate, String endDate, String rewardCardId,Optional<Integer> pageIndex) throws Exception{
		//取得匯出資料
		List<RewardCardModel> resultGet= actionUserRewardCardPointDetailService.getRecordListByRewardCardId(rewardCardId, startDate, endDate,pageIndex);
		
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("UID");
		row.createCell(1).setCellValue("點數");
		row.createCell(2).setCellValue("集點時間");
		row.createCell(3).setCellValue("點數型態");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if(resultGet.size() != 0){
			int seqNo = 1; //序號
			for(RewardCardModel result:resultGet){
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(result.getMID());
				row1.createCell(1).setCellValue(result.getPointGetAmount());
				Date date = sdf.parse(result.getPointGetTime());
				row1.createCell(2).setCellValue(sdf.format(date));	
				
				if(result.getPointType().equals(ActionUserRewardCardPointDetail.POINT_TYPE_MANUAL)){
					row1.createCell(3).setCellValue("手動補點");
				}else if(result.getPointType().equals(ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC)){
					row1.createCell(3).setCellValue("使用者掃描");
				}else if(result.getPointType().equals(ActionUserRewardCardPointDetail.POINT_TYPE_SYSTEM)){
					row1.createCell(3).setCellValue("系統回饋");
				}
				seqNo++;
			}
		}
	}
	
	private void exportToExcelForRewardCardCouponRecord(Workbook wb, Sheet sheet, String rewardCardId) throws Exception{
		//取得匯出資料
		logger.info("exportToExcelForRewardCardCouponRecord");
		List<Map<String, String>> resultGet = actionUserCouponService.getCouponUseRecordListByRewardCardId(rewardCardId);
		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("UID");
		row.createCell(1).setCellValue("優惠券");
		row.createCell(2).setCellValue("兌獎時間");
		row.createCell(3).setCellValue("姓名");
		row.createCell(4).setCellValue("身分證");
		row.createCell(6).setCellValue("地址");
		row.createCell(5).setCellValue("電話");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if(resultGet.size() != 0){
			int seqNo = 1; //序號
			for(Map<String, String> result:resultGet){
				Row row1 = sheet.createRow(seqNo);
				row1.createCell(0).setCellValue(result.get("UID"));
				row1.createCell(1).setCellValue(result.get("couponTitle"));
				Date date = sdf.parse(result.get("couponActionTime"));
				row1.createCell(2).setCellValue(sdf.format(date));	
				row1.createCell(3).setCellValue(result.get("name"));
				row1.createCell(4).setCellValue(result.get("idCardNumber"));
				row1.createCell(5).setCellValue(result.get("address"));
				row1.createCell(6).setCellValue(result.get("phoneNumber"));
				seqNo++;
			}
		}
	}
}
