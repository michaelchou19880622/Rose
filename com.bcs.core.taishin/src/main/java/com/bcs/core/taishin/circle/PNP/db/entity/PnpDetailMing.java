package com.bcs.core.taishin.circle.PNP.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;


/**
 * 分隔符號       ;;   char	  1	   N	 分隔符號
 * <p>
 * 明宣 body
 * 分隔符號為兩個分號";;"
 * 流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
 *
 * @author Kenneth
 */
@Slf4j
@Getter
@Setter
@Entity
@Table(name = "BCS_PNP_DETAIL_MING",
        indexes = {
                @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
                @Index(name = "INDEX_1", columnList = "STATUS"),
                @Index(name = "INDEX_2", columnList = "PROC_STAGE"),
                @Index(name = "INDEX_3", columnList = "PNP_DELIVERY_EXPIRE_TIME"),
        })
public class PnpDetailMing extends PnpDetail {
    private static final long serialVersionUID = 1L;

    /* ------------------------- 來源資料原生欄位 --------------------------*/
    /**
     * 流水號
     */
    @Column(name = "SN", columnDefinition = "nvarchar(15)")
    private String sn;
    /**
     * 批次帳號1
     */
    @Column(name = "ACCOUNT1", columnDefinition = "nvarchar(20)")
    private String account1;
    /**
     * 批次帳號2
     */
    @Column(name = "ACCOUNT2", columnDefinition = "nvarchar(20)")
    private String account2;
    /**
     * 保留欄位1
     */
    @Column(name = "VARIABLE1", columnDefinition = "nvarchar(15)")
    private String variable1;
    /**
     * 保留欄位2
     */
    @Column(name = "VARIABLE2", columnDefinition = "nvarchar(15)")
    private String variable2;
    /**
     * 有效秒數
     */
    @Column(name = "KEEP_SECOND", columnDefinition = "nvarchar(15)")
    private String keepSecond;
    /* ------------------------- 來源資料原生欄位 --------------------------*/
}
