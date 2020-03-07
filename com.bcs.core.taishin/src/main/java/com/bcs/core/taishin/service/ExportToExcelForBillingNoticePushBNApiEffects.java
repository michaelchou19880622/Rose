package com.bcs.core.taishin.service;

import com.bcs.core.taishin.circle.service.BillingNoticeContentTemplateMsgService;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "BNRecorder")
@Service
public class ExportToExcelForBillingNoticePushBNApiEffects {
    @Autowired
    private BillingNoticeContentTemplateMsgService billingNoticeContentTemplateMsgService;

    public void exportExcel(String exportPath, String fileName, String startDate, String endDate) {
        try {
            Workbook workbook = new XSSFWorkbook();
            FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);

            this.getBNPushApiEffects(workbook, startDate, endDate);
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private void getBNPushApiEffects(Workbook workbook, String startDate, String endDate) {
        try {
            Map<String, List<String>> bnEffects = billingNoticeContentTemplateMsgService.getBnEffects(startDate, endDate, null);

            int sheetNumber = 1;
            Sheet sheet = this.createBNPushApiEffectsSheet(workbook, sheetNumber++);
            int rowNumber = 1;
            long sumSuccess = 0;
            long sumFail = 0;
            for (Object key : bnEffects.keySet()) {
                List<String> list = bnEffects.get(key);
                //log.info("list2:" + list.toString());
                Row row = sheet.createRow(rowNumber);

                // Time Cell Style
                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

                Cell createTimeCell = row.createCell(0);
                createTimeCell.setCellValue(list.get(0));
                createTimeCell.setCellStyle(cellStyle);

                row.createCell(1).setCellValue(list.get(1));
                row.createCell(2).setCellValue(list.get(2));
                row.createCell(3).setCellValue(list.get(3));
                row.createCell(4).setCellValue(list.get(4));
                row.createCell(5).setCellValue(list.get(5));
                long total = Long.parseLong(list.get(4).toString()) + Long.parseLong(list.get(5).toString());
                row.createCell(6).setCellValue(String.valueOf(total));

                rowNumber += 1;
                sumSuccess += Long.parseLong(list.get(4).toString());
                sumFail += Long.parseLong(list.get(5).toString());
                if (rowNumber > 1048500) { // RowLimit = 1048576
                    sheet = this.createBNPushApiEffectsSheet(workbook, sheetNumber++);
                    rowNumber = 1;
                }
            }
            Row sumRow = sheet.createRow(rowNumber);

            sumRow.createCell(0).setCellValue("總計");
            sumRow.createCell(1).setCellValue("");
            sumRow.createCell(2).setCellValue("");
            sumRow.createCell(3).setCellValue("");
            sumRow.createCell(4).setCellValue(String.valueOf(sumSuccess));
            sumRow.createCell(5).setCellValue(String.valueOf(sumFail));
            sumRow.createCell(6).setCellValue(String.valueOf(sumFail + sumSuccess));

        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private Sheet createBNPushApiEffectsSheet(Workbook workbook, Integer sheetNumber) {
        Sheet sheet = null;
        try {
            sheet = workbook.createSheet("成效報表" + sheetNumber);

            // first row
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("發送時間");
            row.createCell(1).setCellValue("推播類型");
            row.createCell(2).setCellValue("樣板名稱");
            row.createCell(3).setCellValue("發送類型");
            row.createCell(4).setCellValue("發送成功數");
            row.createCell(5).setCellValue("發送失敗數");
            row.createCell(6).setCellValue("發送數量");
            // column width
            sheet.setColumnWidth(0, 13 * 256);
            sheet.setColumnWidth(1, 50 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            sheet.setColumnWidth(4, 15 * 256);
            sheet.setColumnWidth(5, 15 * 256);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return sheet;
    }

    public void exportExcel(String exportPath, String fileName, String date, String title, String sendType, String bnType) {
        try {
            Workbook workbook = new XSSFWorkbook();
            FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);

            this.getBNPushApiEffects(workbook, date, title, sendType, bnType);
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private void getBNPushApiEffects(Workbook workbook, String date, String title, String sendType, String bnType) {
        try {
            Map<String, List<String>> bnEffects = billingNoticeContentTemplateMsgService.getBnEffectsDetail(date, title, sendType, bnType, null);

            int sheetNumber = 1;
            Sheet sheet = this.createBNPushApiEffectsDetailSheet(workbook, sheetNumber++);
            int rowNumber = 1;
            for (Object key : bnEffects.keySet()) {
                List<String> list = bnEffects.get(key);
                //log.info("list2:" + list.toString());
                Row row = sheet.createRow(rowNumber);

                row.createCell(0).setCellValue(list.get(0));

                // Time Cell Style
                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("YYYY-MM-DD HH:mm:ss"));

                Cell createTimeCell = null;

                for (int i = 1; i <= 3; i++) {
                    createTimeCell = row.createCell(i);
                    createTimeCell.setCellValue(list.get(i).replaceFirst("\\.\\d*(Z)?$", "$1"));
                    createTimeCell.setCellStyle(cellStyle);
                }
                row.createCell(4).setCellValue(list.get(4));
                row.createCell(5).setCellValue(list.get(5));

                rowNumber += 1;
                if (rowNumber > 1048500) { // RowLimit = 1048576
                    sheet = this.createBNPushApiEffectsDetailSheet(workbook, sheetNumber++);
                    rowNumber = 1;
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private Sheet createBNPushApiEffectsDetailSheet(Workbook workbook, Integer sheetNumber) {
        Sheet sheet = null;
        try {
            sheet = workbook.createSheet("成效明細報表" + sheetNumber);

            // first row
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("發送時間");
            row.createCell(1).setCellValue("推播類型");
            row.createCell(2).setCellValue("訊息標題");
            row.createCell(3).setCellValue("訊息內容");
            row.createCell(4).setCellValue("發送結果");
            row.createCell(5).setCellValue("UID");

            // column width
            for (int col_index = 1; col_index <= 3; col_index++) {
                sheet.setColumnWidth(col_index, 20 * 256);
            }
            sheet.setColumnWidth(0, 40 * 256);
            sheet.setColumnWidth(4, 13 * 256);
            sheet.setColumnWidth(5, 35 * 256);

        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return sheet;
    }
}
