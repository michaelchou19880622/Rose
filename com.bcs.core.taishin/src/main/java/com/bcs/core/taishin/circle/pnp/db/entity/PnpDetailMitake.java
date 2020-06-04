package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;


/**
 * 三竹  body
 * 欄位    型態 長度 説明 備註
 * DestCategory    Char      8  ”掛帳代碼” =>  PCC Code       此欄位新版簡訊無特別用處                       Dept_id(必填)
 * <p>
 * DestName        Varchar   20 請填入系統有意義之流水號(open端可辯示之唯一序號)                   Msg_idx
 * <p>
 * DestNo          Varchar   20 手機門號/請填入09帶頭的手機號碼。                                                                                          必填 Tel(必填)
 * <p>
 * MsgData         Varchar      請勿輸入 % $ '  字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。
 * 若有換行的需求，請以ASCII Code 6代表換行。必填。(333個字)         Content(必填)
 *
 * @author Kenneth
 */
@Slf4j
@Getter
@Setter
@Entity
@Table(name = "BCS_PNP_DETAIL_MITAKE",
        indexes = {
                @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
                @Index(name = "INDEX_1", columnList = "STATUS"),
                @Index(name = "INDEX_2", columnList = "PROC_STAGE"),
                @Index(name = "INDEX_3", columnList = "PNP_DELIVERY_EXPIRE_TIME"),
        })
public class PnpDetailMitake extends PnpDetail {
    private static final long serialVersionUID = 1L;

    /* ------------------------- 來源資料原生欄位 --------------------------*/
    /**
     * 請填入系統有意義之流水號
     */
    @Column(name = "DEST_CATEGORY", columnDefinition = "nvarchar(15)")
    private String destCategory;

    /**
     * 手機門號/請填入09帶頭的手機號碼
     */
    @Column(name = "DEST_NAME", columnDefinition = "nvarchar(36)")
    private String destName;

    /* ------------------------- 來源資料原生欄位 --------------------------*/

    /**
     * For Every8d sms extend column
     */
    @Column(name = "VARIABLE1", columnDefinition = "nvarchar(15)")
    private String variable1;
    /**
     * For Every8d sms extend column
     */
    @Column(name = "VARIABLE2", columnDefinition = "nvarchar(15)")
    private String variable2;


    /* SMS 輸出格式轉換為Every8d 用的暫存物件 */
    @Transient
    private PnpDetailEvery8d every8dObj;

}
