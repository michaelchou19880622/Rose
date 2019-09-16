package com.bcs.core.bot.enums;

import lombok.Getter;

/**
 * The enum Pnp FTP type.
 *
 * @author ???
 */
@Getter
public enum PNPFTPType {

    /**
     * 前方來源系統
     */
    MITAKE("mitake", "1", "BCS_PNP_MAIN_MITAKE", "BCS_PNP_DETAIL_MITAKE"),
    EVERY8D("every8d", "2", "BCS_PNP_MAIN_EVERY8D", "BCS_PNP_DETAIL_EVERY8D"),
    MING("ming", "3", "BCS_PNP_MAIN_MING", "BCS_PNP_DETAIL_MING"),
    UNICA("unica", "4", "BCS_PNP_MAIN_UNICA", "BCS_PNP_DETAIL_UNICA");

    private String source;
    private String value;
    private String mainTable;
    private String detailTable;

    PNPFTPType(String value, String source, String mainTable, String detailTable) {
        this.value = value;
        this.source = source;
        this.mainTable = mainTable;
        this.detailTable = detailTable;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * 依照FTP Type Code 尋找對應名稱
     *
     * @param code FTP Type Code
     * @return FTP Type Name
     */
    public static String getNameByCode(String code) {
        for (PNPFTPType type : PNPFTPType.values()) {
            if (type.source.equals(code)) {
                return type.value;
            }
        }
        return "";
    }

    /**
     * 依照FTP Type Code 尋找對應 Main Table名稱
     *
     * @param code FTP Type Code
     * @return FTP Table Name
     */
    public static String getMainTableNameByCode(String code) {
        for (PNPFTPType type : PNPFTPType.values()) {
            if (type.source.equals(code)) {
                return type.mainTable;
            }
        }
        return "";
    }

    /**
     * 依照FTP Type Code 尋找對應 Detail Table名稱
     *
     * @param code FTP Type Code
     * @return FTP Table Name
     */
    public static String getDetailTableNameByCode(String code) {
        for (PNPFTPType type : PNPFTPType.values()) {
            if (type.source.equals(code)) {
                return type.detailTable;
            }
        }
        return "";
    }
}
