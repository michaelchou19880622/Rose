package com.bcs.core.taishin.circle.PNP.code;

/**
 * Pnp Send Status Enum
 *
 * @author Alan
 */
public enum PnpStatusEnum {

    /* ========================================FTP================================================*/

    FTP_WHITE_HEADER_VALID_FAIL("FTP_WHITE_HEADER_VALID_FAIL", "白名單檔名檢核失敗"),
    FTP_WHITE_CONTENT_VALID_FAIL("FTP_WHITE_CONTENT_VALID_FAIL", "白名單內文檢核失敗"),
    FTP_DETAIL_SAVE("DRAFT", "FTP檔案內文儲存完畢"),
    FTP_MAIN_SAVE("WAIT", "FTP檔案全部儲存完畢"),

    /* ========================================BC================================================*/

    BC_PROCESS("BC_PROCESS", "BC處理中"),
    BC_SENDING("BC_SENDING", "BC發送中"),
    BC_SENT_COMPLETE("BC_COMPLETE", "BC發送成功"),
    BC_SENT_FAIL("BC_FAIL", "BC發送失敗"),
    BC_SENT_FAIL_PNP_PROCESS("BC_FAIL_PNP_PROCESS", "BC發送失敗，轉發PNP"),
    BC_SENT_FAIL_SMS_PROCESS("BC_FAIL_SMS_PROCESS", "BC發送失敗，轉發SMS"),
    BC_USER_BLOCKED("USER_BLOCK", "使用者封鎖無法發送"),
    BC_USER_BLOCKED_SMS_PROCESS("USER_BLOCK_SMS_PROCESS", "使用者封鎖，轉發SMS"),

    /* ========================================PNP================================================*/

    PNP_SENDING("PNP_SENDING", "PNP發送中"),
    PNP_SENT_CHECK_DELIVERY("CHECK_DELIVERY", "PNP已發送至LINE平台"),
    PNP_SENT_COMPLETE("PNP_COMPLETE", "PNP發送成功"),
    PNP_SENT_FAIL("PNP_FAIL", "PNP發送失敗"),
    PNP_SENT_FAIL_SMS_PROCESS("PNP_FAIL_SMS_PROCESS", "PNP發送失敗，轉發SMS"),


    /* ========================================SMS================================================*/

    SMS_SENDING("SMS_SENDING", "SMS發送中"),
    SMS_SENT_CHECK_DELIVERY("SMS_CHECK_DELIVERY", "SMS已發送至簡訊平台"),
    SMS_SENT_COMPLETE("SMS_COMPLETE", "SMS發送成功"),
    SMS_SENT_FAIL("SMS_FAIL", "SMS發送失敗"),


    /* ========================================OLD================================================*/
    @Deprecated
    PROCESS("PROCESS", "處理中"),
    @Deprecated
    SENDING("SENDING", "發送中"),
    @Deprecated
    FINISH("FINISH", "已完成"),
    @Deprecated
    FAIL("FAIL", "失敗"),
    @Deprecated
    DELETE("DELETE", "已刪除"),
    @Deprecated
    COMPLETE("COMPLETE", "已完成");

    /**
     * Value
     */
    public final String value;

    /**
     * Chinese Value
     */
    public final String chinese;


    /**
     * Default Constructor
     *
     * @param value   Value
     * @param chinese Chinese value
     */
    PnpStatusEnum(String value, String chinese) {
        this.value = value;
        this.chinese = chinese;
    }

    public static PnpStatusEnum findEnumByName(String value){
        for (PnpStatusEnum e : PnpStatusEnum.values()){
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
