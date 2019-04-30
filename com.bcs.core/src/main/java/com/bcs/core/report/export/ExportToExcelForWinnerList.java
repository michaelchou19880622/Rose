package com.bcs.core.report.export;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.model.WinnedCouponModel;
import com.bcs.core.model.WinnerModel;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ExportToExcelForWinnerList {

	/** Logger */
	private static Logger logger = Logger.getLogger(ExportToExcelForWinnerList.class);

	@Autowired
	private ContentGameService contentGameService;

	@Autowired
	private WinnerListService winnerListService;

	/**
	 * 匯出 Winner List EXCEL
	 */
	public void exportToExcelForWinnerList(String exportPath, String fileName, String startDate, String endDate,
			String gameId, Optional<String> couponPrizeId,Optional<Integer> pageIndex) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); // →xls // new XSSFWorkbook()→xlsx

			Sheet sheetCoupon = wb.createSheet("Winner List"); // create a new sheet
			this.exportToExcelForWinnerList(wb, sheetCoupon, startDate, endDate, gameId, couponPrizeId,pageIndex);

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
	 * 匯出 Winner List EXCEL
	 */
	public void exportToExcelForWinnerListByCouponId(String exportPath, String fileName, String startDate,
			String endDate, String couponId,Optional<Integer> pageIndex) throws Exception {
		try {
			Workbook wb = new XSSFWorkbook(); // →xls // new XSSFWorkbook()→xlsx

			Sheet sheetCoupon = wb.createSheet("Winner List"); // create a new sheet
			this.exportToExcelForWinnerListByCouponId(wb, sheetCoupon, startDate, endDate, couponId,pageIndex);

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
	 * 
	 * @param wb
	 * @param sheet
	 * @throws Exception
	 */
	public void exportToExcelForWinnerList(Workbook wb, Sheet sheet, String startDate, String endDate, String gameId,
			Optional<String> couponPrizeId,Optional<Integer> pageIndex) throws Exception {

		ContentGame contentGame = contentGameService.findOne(gameId);

		if (contentGame == null) {
			throw new Exception("GameId Error");
		}

		List<WinnedCouponModel> resultGet;
		// 取得匯出資料
		if (!couponPrizeId.isPresent()) {
			resultGet = winnerListService.queryWinnerList(gameId, startDate, endDate, pageIndex);
		} else {
			resultGet = winnerListService.queryWinnerListByPrizeId(gameId, couponPrizeId.get(), startDate, endDate, pageIndex);
		}

		Row row = sheet.createRow(0); // declare a row object reference
		row.createCell(0).setCellValue("UID");
		row.createCell(1).setCellValue("獎品名稱");
		row.createCell(2).setCellValue("中獎時間");
		row.createCell(3).setCellValue("姓名");
		row.createCell(4).setCellValue("身分證");
		row.createCell(5).setCellValue("電話");
		row.createCell(6).setCellValue("地址");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (resultGet.size() != 0) {
			int seqNo = 1;

			for (WinnedCouponModel winnedCoupon : resultGet) {
				row = sheet.createRow(seqNo);

				row.createCell(0).setCellValue(winnedCoupon.getUserMID());
				row.createCell(1).setCellValue(winnedCoupon.getCouponTitle());
				row.createCell(2).setCellValue(sdf.format(winnedCoupon.getActionTime()));

				if (winnedCoupon.getIsFillIn()) {
					row.createCell(3).setCellValue((winnedCoupon.getWinnerDetail().getUserName() != null) ? winnedCoupon.getWinnerDetail().getUserName() : "尚未填寫");
					row.createCell(4).setCellValue((winnedCoupon.getWinnerDetail().getUserIdCardNumber() != null) ? winnedCoupon.getWinnerDetail().getUserIdCardNumber() : "尚未填寫");
					row.createCell(5).setCellValue((winnedCoupon.getWinnerDetail().getUserPhoneNumber() != null) ? winnedCoupon.getWinnerDetail().getUserPhoneNumber() : "尚未填寫");
					row.createCell(6).setCellValue((winnedCoupon.getWinnerDetail().getUserAddress() != null) ? winnedCoupon.getWinnerDetail().getUserAddress() : "尚未填寫");
				} else {
					sheet.addMergedRegion(new CellRangeAddress(seqNo, seqNo,3 , 6));
					row.createCell(3).setCellValue("此優惠券無須填寫使用者資料");
				}

				seqNo++;
			}

			/* 自動調整欄寬 */
			for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
				sheet.autoSizeColumn(col_index);
			}
		}
	}

	/**
	 * Export To Excel For Winner List
	 * 
	 * @param wb
	 * @param sheet
	 * @throws Exception
	 */
	public void exportToExcelForWinnerListByCouponId(Workbook wb, Sheet sheet, String startDate, String endDate,
			String couponId,Optional<Integer> pageIndex) throws Exception {

		List<WinnedCouponModel> resultGet = winnerListService.getWinnerListAndCouponCodeByCouponId(couponId, startDate, endDate, pageIndex);

		Row row = sheet.createRow(0); // declare a row object reference
        row.createCell(0).setCellValue("UID");
        row.createCell(1).setCellValue("名稱");
        row.createCell(2).setCellValue("時間");
        row.createCell(3).setCellValue("地址");
        row.createCell(4).setCellValue("身分證");
        row.createCell(5).setCellValue("手機");
        row.createCell(6).setCellValue("電子序號");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (resultGet.size() != 0) {
			int seqNo = 1;

			for (WinnedCouponModel winnedCoupon : resultGet) {
				row = sheet.createRow(seqNo);
				WinnerModel winnerModel = winnedCoupon.getWinnerDetail();
				row.createCell(0).setCellValue(winnerModel.getUID());
				row.createCell(1).setCellValue(winnerModel.getUserName());
				row.createCell(2).setCellValue(sdf.format(winnerModel.getModifyTime()));
				row.createCell(3).setCellValue(winnerModel.getUserAddress());
				row.createCell(4).setCellValue(winnerModel.getUserIdCardNumber());
				row.createCell(5).setCellValue(winnerModel.getUserPhoneNumber());
				row.createCell(6).setCellValue(winnedCoupon.getCouponCode());

				seqNo++;
			}

			/* 自動調整欄寬 */
			for (Integer col_index = 0; col_index < sheet.getRow(0).getPhysicalNumberOfCells(); col_index++) {
				sheet.autoSizeColumn(col_index);
			}
		}
	}
}
