package com.bcs.core.taishin.circle.PNP.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Uinca 都是互動格式  【台新 Line PNP】相關問題
 * From: 洪志豪 <shaunhung@taishinbank.com.tw>
 * Sent: Friday, April 19, 2019 10:33 AM
 * <p>
 * <p>
 * 分隔符號       &	   char	  1	   N	 分隔符號
 * <p>
 * 互動 body
 * 名稱	                          屬性	                       長度	  Null?	          說明
 * SN	        char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
 * DestName   char        36	   Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
 * Mobile	    char        20	   N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
 * Content    nvarchar    756	   N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
 * PID        char        11	   Y     身份字號
 * CampaignID	varchar	    28     Y     行銷活動代碼(可為空值)
 * SegmentID	varchar	    10     Y     客群代號(可為空值)
 * ProgramID	varchar	    20     Y     階段代號(可為空值)
 * Variable1	varchar	    15     Y     擴充欄位1(可為空值)
 * Variable2	varchar	    15     Y     擴充欄位2(可為空值)
 *
 * @author Kenneth
 */
@Getter
@Setter
@Entity
@Table(name = "BCS_PNP_DETAIL_UNICA",
        indexes = {
                @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
                @Index(name = "INDEX_1", columnList = "STATUS"),
                @Index(name = "INDEX_2", columnList = "PROC_STAGE"),
                @Index(name = "INDEX_3", columnList = "PNP_DELIVERY_EXPIRE_TIME"),
        })
public class PnpDetailUnica extends PnpDetail {
    private static final long serialVersionUID = 1L;
    /* ------------------------- 來源資料原生欄位 --------------------------*/
    /**
     * 名單流水號
     */
    @Column(name = "SN", columnDefinition = "nvarchar(15)")
    private String sn;
    /**
     * 收件者名稱
     */
    @Column(name = "DEST_NAME", columnDefinition = "nvarchar(36)")
    private String destName;
    /**
     * 身份證字號
     */
    @Column(name = "PID", columnDefinition = "nvarchar(11)")
    private String pid;
    /**
     * 行銷活動代碼
     */
    @Column(name = "CAMPAIGN_ID", columnDefinition = "nvarchar(28)")
    private String campaignId;
    /**
     * 客群代號
     */
    @Column(name = "SEGMENT_ID", columnDefinition = "nvarchar(10)")
    private String segmentId;
    /**
     * 階段代號
     */
    @Column(name = "PROGRAM_ID", columnDefinition = "nvarchar(20)")
    private String programId;
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
    /* ------------------------- 來源資料原生欄位 --------------------------*/

}
