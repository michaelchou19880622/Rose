package com.bcs.core.taishin.circle.pnp.code;

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

    /* ========================================BC STATUS================================================*/

    BC_PROCESS("BC_PROCESS", "處理中"),
    BC_SENDING("BC_SENDING", "發送中"),
    BC_SENT_COMPLETE("BC_COMPLETE", "發送成功"),

    BC_SENT_FAIL("BC_FAIL", "發送失敗"),
    BC_SENT_FAIL_PNP_PROCESS("BC_FAIL_PNP_PROCESS", "發送失敗，轉發PNP"),
    BC_SENT_FAIL_SMS_PROCESS("BC_FAIL_SMS_PROCESS", "發送失敗，轉發SMS"),

    BC_UID_NOT_FOUND("BC_UID_NOT_FOUND", "手機號碼查無UID"),
    BC_UID_NOT_FOUND_PNP_PROCESS("BC_UID_NOT_FOUND_PNP_PROCESS", "手機號碼查無UID，轉發PNP"),
    BC_UID_NOT_FOUND_SMS_PROCESS("BC_UID_NOT_FOUND_SMS_PROCESS", "手機號碼查無UID，轉發SMS"),

    BC_USER_IN_BLACK_LIST("BC_IN_BLACK_LIST", "該使用者於黑名單中"),
    BC_USER_IN_BLACK_LIST_PNP_PROCESS("BC_IN_BLACK_LIST_PNP_PROCESS", "該使用者於黑名單中，轉發PNP"),
    BC_USER_IN_BLACK_LIST_SMS_PROCESS("BC_IN_BLACK_LIST_SMS_PROCESS", "該使用者於黑名單中，轉發SMS"),

    BC_USER_BLOCKED("USER_BLOCK", "使用者關閉接收通知"),
    BC_USER_BLOCKED_PNP_PROCESS("USER_BLOCK_PNP_PROCESS", "使用者關閉接收通知，轉發PNP"),
    BC_USER_BLOCKED_SMS_PROCESS("USER_BLOCK_SMS_PROCESS", "使用者關閉接收通知，轉發SMS"),

    USER_IS_SYSTEM_ADD_IGNORE("USER_IS_SYSTEM_ADD_IGNORE", "此為系統自動建立帳號，禁止發送BC"),
    USER_IS_SYSTEM_ADD_IGNORE_PNP("USER_IS_SYSTEM_ADD_IGNORE_PNP", "此為系統自動建立帳號，轉發PNP"),
    USER_IS_SYSTEM_ADD_IGNORE_SMS("USER_IS_SYSTEM_ADD_IGNORE_SMS", "此為系統自動建立帳號，轉發SMS"),

    USER_IS_UNBIND_IGNORE_TO_SMS("USER_IS_UNBIND_IGNORE_TO_SMS", "使用者未綁定，轉發SMS"),
    USER_IS_UNBIND_IGNORE_TO_PNP("USER_IS_UNBIND_IGNORE_TO_PNP", "使用者未綁定，轉發PNP"),
    USER_IS_UNBIND_IGNORE("USER_IS_UNBIND_IGNORE", "使用者未綁定，禁止發送BC"),

    /* ========================================PNP STATUS================================================*/

    PNP_SENDING("PNP_SENDING", "發送中"),
    PNP_SENT_CHECK_DELIVERY("CHECK_DELIVERY", "已發送至LINE平台"),
    PNP_SENT_EXPIRED_FAIL_SMS_PROCESS("PNP_EXPIRED_TO_SMS", "發送逾期，轉發SMS"),
    PNP_SENT_TO_LINE_FAIL_SMS_PROCESS("PNP_FAIL_SMS_PROCESS", "發送至LINE平台失敗，轉發SMS"),

    PNP_SENT_COMPLETE("PNP_COMPLETE", "發送成功"),

    PNP_USER_IN_BLACK_LIST_SMS_PROCESS("PNP_IN_BLACK_LIST_SMS_PROCESS", "該使用於黑名單中，轉發SMS"),


    /* ========================================SMS STATUS================================================*/

    SMS_RESEND("SMS_RESEND", "SMS重發"),
    SMS_SENDING("SMS_SENDING", "發送中"),
    SMS_SENT_CHECK_DELIVERY("SMS_CHECK_DELIVERY", "已發送至簡訊平台"),
    SMS_SENT_COMPLETE("SMS_COMPLETE", "發送成功"),
    SMS_SENT_FAIL("SMS_FAIL", "發送失敗"),


    /* ========================================STATUS================================================*/
    PROCESS("PROCESS", "處理中"),
    SENDING("SENDING", "發送中"),
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

    public static PnpStatusEnum findEnumByName(String value) {
        for (PnpStatusEnum e : PnpStatusEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
