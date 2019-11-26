package com.bcs.core.taishin.circle.PNP.code;


/**
 * Pnp Ftp Source Enum
 * 
 * @author Alan
 */
public enum PnpFtpSourceEnum {
    /**
     * 三竹來源
     */
    MITAKE(1, "1", "mitake", "三竹", "BCS_PNP_MAIN_MITAKE", "BCS_PNP_DETAIL_MITAKE"),
    /**
     * 互動來源
     */
    EVERY8D(2, "2", "every8d", "互動", "BCS_PNP_MAIN_EVERY8D", "BCS_PNP_DETAIL_EVERY8D"),
    /**
     * 明宣來源
     */
    MING(3, "3", "ming", "明軒", "BCS_PNP_MAIN_MING", "BCS_PNP_DETAIL_MING"),

    /**
     * UNICA來源
     */
    UNICA(4, "4", "unica", "Unica", "BCS_PNP_MAIN_UNICA", "BCS_PNP_DETAIL_UNICA");

    /**
     * Id
     */
    public final int id;
    public final String code;
    public final String english;
    public final String chinese;
    public final String mainTable;
    public final String detailTable;

    /**
     * Default Constructor
     * @param id id
     */
    PnpFtpSourceEnum(int id, String code, String english, String chinese, String mainTable, String detailTable) {
        this.id = id;
        this.code = code;
        this.english = english;
        this.chinese = chinese;
        this.mainTable = mainTable;
        this.detailTable = detailTable;
    }

    public static PnpFtpSourceEnum findEnumByCode(String code){
        for (PnpFtpSourceEnum e : PnpFtpSourceEnum.values()){
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
