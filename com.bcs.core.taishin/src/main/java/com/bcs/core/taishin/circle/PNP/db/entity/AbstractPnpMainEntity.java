package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.utils.ObjectUtil;

import java.io.Serializable;

public abstract class AbstractPnpMainEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /* ================================ FTP ==================================== */
    /**
     * 草稿 - 儲存至Detail Table後
     */
    public static final String DATA_CONVERTER_STATUS_DRAFT = "DRAFT";

    /**
     * 等待 - 草稿後建立Main及更新Detail後
     */
    public static final String DATA_CONVERTER_STATUS_WAIT = "WAIT";

    /**
     * 即時發送(Default)
     */
    public static final String SEND_TYPE_IMMEDIATE = "IMMEDIATE";
    /**
     * 排程過期 - 預約時間 < 現在時間過期(即時發送)
     */
    public static final String SEND_TYPE_SCHEDULE_TIME_EXPIRED = "SCH_EXPRED";
    /**
     * 排程未過期 - 預約時間 > 現在時間過期(進入排程)
     */
    public static final String SEND_TYPE_DELAY = "DELAY";
    //預約
    public static final String DATA_CONVERTER_STATUS_SCHEDULED = "SCHEDULED";

    public static final String MSG_SENDER_STATUS_BC_PROCESS = "BC_PROCESS";



    /* ========================================BC================================================*/

    public static final String MSG_SENDER_STATUS_BC_SENDING = "BC_SENDING";
    public static final String DATA_CONVERTER_STATUS_BC_COMPLETE = "BC_COMPLETE";
    public static final String MSG_SENDER_STATUS_BC_FAIL = "BC_FAIL";
    public static final String MSG_SENDER_STATUS_BC_FAIL_PNP_PROCESS = "BC_FAIL_PNP_PROCESS";
    public static final String MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS = "BC_FAIL_SMS_PROCESS";
    public static final String MSG_SENDER_STATUS_USER_BLOCK_SMS_PROCESS = "USER_BLOCK_SMS_PROCESS";

    /* ========================================BC================================================*/
    /* ========================================PNP================================================*/
    public static final String MSG_SENDER_STATUS_PNP_SENDING = "PNP_SENDING";
    public static final String MSG_SENDER_STATUS_CHECK_DELIVERY = "CHECK_DELIVERY";
    public static final String DATA_CONVERTER_STATUS_PNP_COMPLETE = "PNP_COMPLETE";
    public static final String MSG_SENDER_STATUS_PNP_FAIL_SMS_PROCESS = "PNP_FAIL_SMS_PROCESS";


    /* ========================================PNP================================================*/
    /* ========================================SMS================================================*/
    public static final String MSG_SENDER_STATUS_SMS_SENDING = "SMS_SENDING";
    public static final String MSG_SENDER_STATUS_SMS_CHECK_DELIVERY = "SMS_CHECK_DELIVERY";
    public static final String DATA_CONVERTER_STATUS_SMS_COMPLETE = "SMS_COMPLETE";
    public static final String MSG_SENDER_STATUS_SMS_FAIL = "SMS_FAIL";
    /* ========================================SMS================================================*/

    @Deprecated
    public static final String MSG_SENDER_STATUS_PROCESS = "PROCESS";
    @Deprecated
    public static final String MSG_SENDER_STATUS_SENDING = "SENDING";
    @Deprecated
    public static final String MSG_SENDER_STATUS_FINISH = "FINISH";
    @Deprecated
    public static final String MSG_SENDER_STATUS_FAIL = "FAIL";
    @Deprecated
    public static final String MSG_SENDER_STATUS_DELETE = "DELETE";
    @Deprecated
    public static final String DATA_CONVERTER_STATUS_COMPLETE = "COMPLETE";


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
