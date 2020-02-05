package com.bcs.core.taishin.circle.pnp.db.entity;

import com.bcs.core.utils.ObjectUtil;

import java.io.Serializable;

public abstract class AbstractPnpMainEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /* ================================ FTP ==================================== */

    public static final String FTP_DETAIL_SAVE = "DRAFT";
    public static final String FTP_MAIN_SAVE = "WAIT";

    public static final String SEND_TYPE_IMMEDIATE = "IMMEDIATE";
    public static final String SEND_TYPE_SCHEDULE_TIME_EXPIRED = "SCH_EXPRED";
    public static final String SEND_TYPE_DELAY = "DELAY";
    public static final String DATA_CONVERTER_STATUS_SCHEDULED = "SCHEDULED";



    /* ========================================BC================================================*/

    public static final String BC_PROCESS = "BC_PROCESS";
    public static final String BC_SENDING = "BC_SENDING";
    public static final String BC_SENT_COMPLETE = "BC_COMPLETE";
    public static final String BC_SENT_FAIL = "BC_FAIL";
    public static final String BC_SENT_FAIL_PNP_PROCESS = "BC_FAIL_PNP_PROCESS";
    public static final String BC_SENT_FAIL_SMS_PROCESS = "BC_FAIL_SMS_PROCESS";
    public static final String BC_USER_BLOCKED_SMS_PROCESS = "USER_BLOCK_SMS_PROCESS";

    /* ========================================PNP================================================*/

    public static final String PNP_SENDING = "PNP_SENDING";
    public static final String PNP_SENT_CHECK_DELIVERY = "CHECK_DELIVERY";
    public static final String PNP_SENT_COMPLETE = "PNP_COMPLETE";
    public static final String PNP_SENT_FAIL_SMS_PROCESS = "PNP_FAIL_SMS_PROCESS";


    /* ========================================SMS================================================*/

    public static final String SMS_SENDING = "SMS_SENDING";
    public static final String SMS_SENT_CHECK_DELIVERY = "SMS_CHECK_DELIVERY";
    public static final String SMS_SENT_COMPLETE = "SMS_COMPLETE";
    public static final String SMS_SENT_FAIL = "SMS_FAIL";

    @Deprecated
    public static final String PROCESS = "PROCESS";
    @Deprecated
    public static final String SENDING = "SENDING";
    @Deprecated
    public static final String FINISH = "FINISH";
    @Deprecated
    public static final String FAIL = "FAIL";
    @Deprecated
    public static final String DELETE = "DELETE";
    @Deprecated
    public static final String COMPLETE = "COMPLETE";


    /**
     * 三竹來源
     */
    public static final String SOURCE_MITAKE = "1";
    /**
     * 互動來源
     */
    public static final String SOURCE_EVERY8D = "2";
    /**
     * 明宣來源
     */
    public static final String SOURCE_MING = "3";

    /**
     * UNICA來源
     */
    public static final String SOURCE_UNICA = "4";

    /**
     * 通路參數 : 寄BC 失敗直接結束
     */
    public static final String PROC_FLOW_BC = "1";
    /**
     * 通路參數 : 寄BC 失敗轉發SMS後結束
     */
    public static final String PROC_FLOW_BC_SMS = "2";
    /**
     * 通路參數 : 寄BC 失敗後寄PNP失敗後寄SMS結束
     */
    public static final String PROC_FLOW_BC_PNP_SMS = "3";


    /**
     * Stage : BC
     */
    public static final String STAGE_BC = "BC";
    /**
     * Stage : PNP
     */
    public static final String STAGE_PNP = "PNP";
    /**
     * Stage : SMS
     */
    public static final String STAGE_SMS = "SMS";

    @Override
    public String toString() {
        return ObjectUtil.objectToJsonStr(this);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
