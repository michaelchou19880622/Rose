package com.bcs.core.report.builder;

import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.Map;

/**
 * Export Excel Builder
 * <p>
 * Example:
 * ExportExcelBuilder.createWorkBook()
 * .setSheetName("TestSheet1")
 * .createHeaderRow().setRowValue(headerMap)
 * .createRow(1).setRowValue(rowValueMap1)
 * .createRow(2).setRowValue(rowValueMap2)
 * .createRow(30).setRowValue(rowValueMap3)
 * .setAllColumnAutoWidth()
 * .setOutputPath(filePath)
 * .setOutputFileName(fileName)
 * .export();
 *
 * @author Alan
 */
@Slf4j
public class ExportExcelBuilder {
    /**
     * Excel Base Object
     */
    private Workbook workbook;

    /**
     * Excel Sheet
     */
    private Sheet sheet;

    /**
     * Excel Row
     */
    private Row row;

    /**
     * Output File Path
     */
    private String outPutPath;

    /**
     * Output File Name
     * ex: fileA.xlsx
     */
    private String outputFileName;

    /**
     * Max Cell Index For Iterator Set Column Width
     */
    private int maxCellIndex = 0;

    private ExportExcelBuilder() {
        this.workbook = new XSSFWorkbook();
    }

    public static ExportExcelBuilder createWorkBook() {
        return new ExportExcelBuilder();
    }

    /**
     * Create Sheet And Assign Name
     * Notice: Need add Suffix .xlsx
     *
     * @param sheetName New Sheet Name
     * @return this
     */
    public ExportExcelBuilder setSheetName(String sheetName) {
        this.sheet = this.workbook.createSheet(sheetName);
        return this;
    }


    /**
     * Create row By Row Number
     *
     * @param rowNumber Row Number
     * @return this
     */
    public ExportExcelBuilder createRow(int rowNumber) {
        this.row = this.sheet.createRow(rowNumber);
        return this;
    }

    /**
     * Create Header Row
     *
     * @return this
     */
    public ExportExcelBuilder createHeaderRow() {
        return createRow(0);
    }

    /**
     * @param rowValueMap Key: Cell Index Number,  Value: Cell Value
     * @return this
     */
    public ExportExcelBuilder setRowValue(Map<Integer, String> rowValueMap) {
        log.info("Current Row Number is {}\n{}", this.row.getRowNum(), DataUtils.toPrettyJsonUseJackson(rowValueMap));
        for (Map.Entry<Integer, String> entry : rowValueMap.entrySet()) {
            Integer cellIndex = entry.getKey();
            String cellValue = entry.getValue();
            this.row.createCell(cellIndex).setCellValue(cellValue);
            recordMaxCellIndex(cellIndex);
        }
        return this;
    }

    /**
     * Record Max Cell Index
     *
     * @param cellIndex Current Cell Index
     */
    private void recordMaxCellIndex(int cellIndex) {
        if (this.maxCellIndex < cellIndex) {
            maxCellIndex = cellIndex;
            log.debug("Max Cell Index is {}", this.maxCellIndex);
        }
    }


    /**
     * Set Column Width By Column Index
     *
     * @param columnIndex Column Index Number
     * @param width       width
     * @return this
     */
    public ExportExcelBuilder setColumnWidth(int columnIndex, int width) {
        this.sheet.setColumnWidth(columnIndex, width * 256);
        return this;
    }

    /**
     * Set All Column Default Width By Max Cell Index Number
     *
     * @return this
     */
    public ExportExcelBuilder setAllColumnDefaultWidth() {
        for (int i = 0; i < this.maxCellIndex; i++) {
            this.sheet.setColumnWidth(i, 35 * 256);
        }
        return this;
    }

    /**
     * Set Column Auto Width By Column Index
     *
     * @param columnIndex Column Index
     * @return this
     */
    public ExportExcelBuilder setAutoWidth(int columnIndex) {
        this.sheet.autoSizeColumn(columnIndex);
        return this;
    }

    public ExportExcelBuilder setAllColumnAutoWidth() {
        for (int i = 0; i < this.maxCellIndex; i++) {
            this.sheet.autoSizeColumn(i);
        }
        return this;
    }

    public ExportExcelBuilder setOutputPath(String outPutPath) {
        this.outPutPath = outPutPath;
        return this;
    }

    public ExportExcelBuilder setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
        return this;
    }

    public void export() {
        log.info("Last Row Number is {}", this.sheet.getLastRowNum());
        try {
            FileOutputStream out = new FileOutputStream(this.outPutPath + this.outputFileName);
            this.workbook.write(out);
            out.close();
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public String getOutPutPath() {
        return this.outPutPath;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }
}
