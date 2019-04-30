package com.bcs.core.api.msg;

public interface MsgGeneratorExtend extends MsgGenerator {

    public static final String MSG_TYPE_BCS_PAGE = "BCS_PAGE";
    public static final String MSG_TYPE_COUPON = "COUPON";
    public static final String MSG_TYPE_REWARDCARD = "REWARDCARD";
    public static final String MSG_TYPE_INTERACTIVE_LINK = "INTERACTIVE_LINK";

    public static final String EVENT_TYPE_UPLOAD_INVOICE = "UPLOAD_INVOICE";
    public static final String EVENT_TYPE_UPLOAD_INVOICE_SUCCESS = "UPLOAD_INVOICE_SUCCESS";
    public static final String EVENT_TYPE_UPLOAD_INVOICE_FAIL = "UPLOAD_INVOICE_FAIL";
    public static final String EVENT_TYPE_DECODE_FAIL = "DECODE_FAIL";
    public static final String EVENT_TYPE_NOT_FOUND = "NOT_FOUND";
    public static final String EVENT_TYPE_NOT_IN_INTERNAL = "NOT_IN_INTERNAL";
    public static final String EVENT_TYPE_LESS_PAYMENT = "LESS_PAYMENT";
    public static final String EVENT_TYPE_TYPE_IN_INVTERN = "TYPE_IN_INVTERN";
    public static final String EVENT_TYPE_TYPE_IN_INVTERN_FAIL = "TYPE_IN_INVTERN_FAIL";
    public static final String EVENT_TYPE_TYPE_IN_RANDON_NUM = "TYPE_IN_RANDON_NUM";
    public static final String EVENT_TYPE_TYPE_IN_RANDON_NUM_FAIL = "TYPE_IN_RANDON_NUM_FAIL";
    public static final String EVENT_TYPE_TOO_MUCH_ERROR = "TOO_MUCH_ERROR";
    public static final String EVENT_TYPE_INVOICE_IS_USED = "INVOICE_IS_USED";
}
