package com.bcs.core.taishin.service;

import com.bcs.core.taishin.circle.PNP.db.service.PNPMaintainAccountModelService;
import com.bcs.core.utils.ErrorRecord;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PnpReportExcelService {
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(PnpReportExcelService.class);

    private int columnSize = 0;

    @Autowired
    private PNPMaintainAccountModelService pnpMaintainAccountModelService;

    public void exportPNPDetailReportExcel(String exportPath, String fileName,
                                           String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {
        try {
            logger.info("Export PNP Detail Report Start!!");
            try(
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream out = new FileOutputStream(exportPath + System.getProperty("file.separator") + fileName);
            ){
                this.getDetailReportExcel(workbook, startDate, endDate, account, pccCode, sourceSystem, empId);
                workbook.write(out);
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
        logger.info("Export PNP Detail Report Write Finish!!");
    }

    /**
     * Create Sheet and Assign Value to Column
     *
     * @param workbook workbook
     * @param startDate start Date
     * @param endDate end Date
     * @param account account
     * @param pccCode pccCode
     * @param sourceSystem source System
     * @param empId empId 員工ID
     */
    private void getDetailReportExcel(Workbook workbook, String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {
        Map<String, List<String>> lists = pnpMaintainAccountModelService.getPNPDetailReportExcelList(startDate, endDate, account, pccCode, sourceSystem, empId);
        try {
            int sheetNumber = 1;
            Sheet sheet = this.createDetailReportSheet(workbook, sheetNumber++);
            int rowNumber = 1;

            for (Map.Entry<String, List<String>> entry : lists.entrySet()) {
                List<String> list = entry.getValue();
                Row row = sheet.createRow(rowNumber);
                for (int i = 0; i < columnSize; i++) {
                    String value = list.get(i);

                    /* 欄位特殊處理 */
                    switch (i) {
                        case 17:
                        case 19:
                        case 25:
                        case 26:
                            /* Date */
                            row.createCell(i).setCellValue(dateTimeToDateOrTime(value, "Date"));
                            break;
                        case 18:
                        case 20:
                            /* Time */
                            row.createCell(i).setCellValue(dateTimeToDateOrTime(value, "Time"));
                            break;
                        case 21:
                            /* 發送狀態(PNP代碼) */
                            row.createCell(i).setCellValue(("COMPLETE".equals(value) || "FINISH".equals(value) ? "200" : "501"));
                            break;
                        case 22:
                            /* 發送狀態(PNP說明) */
                            row.createCell(i).setCellValue(englishStatusToChinese(value));
                            break;
                        case 23:
                            /* 發送狀態(簡訊) */
                            row.createCell(i).setCellValue("");
                            break;
                        case 3:
                            /* 發送通路(1.2.3.4.) */
                            row.createCell(i).setCellValue(englishSourceToChinese(value));
                            break;
                        case 2:
                            /* 通路流(1.2.3.4.) */
                            row.createCell(i).setCellValue(englishProcFlowToChinese(value));
                            break;
                        default:
                            row.createCell(i).setCellValue(value);
                            break;
                    }
                }
                /* Real RowLimit = 1048576 */
                int rowLimit = 1048500;
                rowNumber++;
                if (rowNumber > rowLimit) {
                    sheet = this.createDetailReportSheet(workbook, sheetNumber++);
                    rowNumber = 1;
                }
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * Create Excel Sheet
     * @param workbook workBook
     * @param sheetNumber sheet Number
     * @return Sheet
     */
    private Sheet createDetailReportSheet(Workbook workbook, Integer sheetNumber) {
        try {
            Sheet sheet = workbook.createSheet("PNP明細報表" + sheetNumber);

            /* first row */
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("序號");
            row.createCell(1).setCellValue("前方來源系統");
            row.createCell(2).setCellValue("通路流");
            row.createCell(3).setCellValue("發送通路");
            row.createCell(4).setCellValue("發送帳號");
            row.createCell(5).setCellValue("掛帳PccCode");
            row.createCell(6).setCellValue("發送廠商訊息批次代碼");
            row.createCell(7).setCellValue("發送廠商訊息流水號");
            row.createCell(8).setCellValue("訊息樣板");
            row.createCell(9).setCellValue("訊息內文");
            row.createCell(10).setCellValue("訊息內文點數");
            row.createCell(11).setCellValue("行銷活動代碼");
            row.createCell(12).setCellValue("行銷活動階段");
            row.createCell(13).setCellValue("行銷活動客群代碼");
            row.createCell(14).setCellValue("客戶ID");
            row.createCell(15).setCellValue("客戶手機號碼");
            row.createCell(16).setCellValue("UID");
            row.createCell(17).setCellValue("預約日期");
            row.createCell(18).setCellValue("預約時間");
            row.createCell(19).setCellValue("發送日期");
            row.createCell(20).setCellValue("發送時間");
            row.createCell(21).setCellValue("發送狀態(PNP代碼)");
            row.createCell(22).setCellValue("發送狀態(PNP說明)");
            row.createCell(23).setCellValue("發送狀態(簡訊)");
            row.createCell(24).setCellValue("是否國際簡訊");
            row.createCell(25).setCellValue("資料建立日期");
            row.createCell(26).setCellValue("資料更新日期");

            this.columnSize = row.getLastCellNum();

            logger.info("Column Size: " + this.columnSize);

            // column width
            for (int i = 0; i < this.columnSize; i++) {
                /* Ignore Column No.9 Width 27 */
                if (i == 9) {
                    continue;
                }
                sheet.setColumnWidth(i, 22 * 256);
            }
            /* Set Column No.9 Width 60 */
            sheet.setColumnWidth(9, 60 * 256);
            return sheet;
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            throw e;
        }
    }

    private String dateTimeToDateOrTime(String dateTimeStr, String dateOrTime) {
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

        if(dateTimeStr == null || dateTimeStr.trim().isEmpty()){
            logger.info("Date Time String is Null So Return Null!!");
            return null;
        }

        try {
            /* skip milliseconds */
            dateTimeStr = dateTimeStr.replaceFirst("\\.\\d*(Z)?$", "$1");
            Date dateTime = sdfDateTime.parse(dateTimeStr);

            if ("Date".equals(dateOrTime)) {
                return sdfDate.format(dateTime);
            } else if ("Time".equals(dateOrTime)) {
                return sdfTime.format(dateTime);
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
        return null;
    }

    private String englishStatusToChinese(String status) {
        switch (status) {
            case "PROCESS":
                return "發送處理進行中";
            case "FINISH":
                return "發送處理完成";
            case "FAIL":
                return "發送處理失敗";
            case "CHECK_DELIVERY":
                return "發送成功，等待回應";
            case "SENDING":
                return "發送中";
            case "DELETE":
                return "已刪除";
            case "DRAFT":
                return "正在存進資料庫";
            case "WAIT":
                return "等待進入處理程序";
            case "BC_COMPLETE":
                return "BC處理程序完成";
            case "PNP_COMPLETE":
                return "PNP處理程序完成";
            case "SMS_COMPLETE":
                return "SMS處理程序完成";
            case "COMPLETE":
                return "處理程序完成";
            case "SCHEDULED":
                return "等待預約發送";
            default:
                return null;
        }
    }

    /**
     * Transfer Source Code To Chinese Name
     * @param sourceCode Source Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    private String englishSourceToChinese(String sourceCode) {
        logger.info("sourceCode: " + sourceCode);
        switch (sourceCode) {
            case "1":
                return "三竹";
            case "2":
                return "互動";
            case "3":
                return "明宣";
            case "4":
                return "UNICA";
            default:
                return sourceCode;
        }
    }



    /**
     * Transfer procFlow Code To Chinese Name
     * @param procFlowCode procFlow Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    private String englishProcFlowToChinese(String procFlowCode) {
        logger.info("procFlowCode: " + procFlowCode);
        switch (procFlowCode) {
            case "0":
                return "SMS";
            case "1":
                return "BC";
            case "2":
                return "BC->PNP";
            case "3":
                return "BC->PNP->SMS";
            default:
                return procFlowCode;
        }
    }

}
