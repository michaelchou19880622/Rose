package com.bcs.core.invoice.service;

public class InvoiceStatus {
    
    public static final String VALID = "VALID";
    public static final String DECODE_FAIL = "DECODE_FAIL";
    public static final String NOT_IN_INTERNAL = "NOT_IN_INTERNAL";
    public static final String LESS_PAYMENT = "LESS_PAYMENT";
    /* 未取得發票資訊(48小時內) */
    public static final String NOT_FOUND = "NOT_FOUND";
    /* 未取得發票資訊(48小時後)，表示並無這張發票或賣方未上傳至電子發票平台 */
    public static final String FAKE = "FAKE";
    public static final String EXISTED = "EXISTED";

    private InvoiceStatus() {}

    public static String toChinese(String field) {
        if (field != null) {
            if (VALID.equals(field)) {
                return "符合資料";
            } else if (DECODE_FAIL.equals(field)) {
                return "圖片無法解析";
            } else if (NOT_IN_INTERNAL.equals(field)) {
                return "購買日期不符活動時間";
            } else if (LESS_PAYMENT.equals(field)) {
                return "消費金額低於活動條件";
            } else if (NOT_FOUND.equals(field)) {
                return "無法取得發票";
            } else if (FAKE.equals(field)) {
                return "資料錯誤或超過48小時仍無法取得資料";
            } else if (EXISTED.equals(field)) {
                return "發票已被使用";
            }
        }
        return field;
    }
}
