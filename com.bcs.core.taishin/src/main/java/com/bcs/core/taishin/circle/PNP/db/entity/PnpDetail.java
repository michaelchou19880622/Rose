package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

/**
 * 通用Detail物件
 *
 * @author ???
 */
@Slf4j
@Getter
@Setter
@MappedSuperclass
public class PnpDetail extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    /**
     * Detail ID - Content table Id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PNP_DETAIL_ID")
    private Long pnpDetailId;

    /**
     * Main ID - Header table Id
     */
    @Column(name = "PNP_MAIN_ID")
    private Long pnpMainId;

    /**
     * Line UID
     */
    @Column(name = "UID", columnDefinition = "nvarchar(50)")
    private String uid;

    /**
     * 資料來源
     * 1. 三竹 Mitake
     * 2. 互動來源 Every8D
     * 3. 明宣 Ming
     * 4. Unica
     */
    @Column(name = "SOURCE", columnDefinition = "nvarchar(3)")
    private String source;

    /**
     * 電話號碼
     * 對應原生欄位DestNo
     */
    @Column(name = "PHONE", columnDefinition = "nvarchar(15)")
    private String phone;
    /**
     * 加密後電話號碼
     * SHA256 e.164 hash for line pnp push
     */
    @Column(name = "PHONE_HASH", columnDefinition = "nvarchar(100)")
    private String phoneHash;

    /**
     * 簡訊內文
     * 對應原生欄位 MsgData
     */
    @Column(name = "MSG", columnDefinition = "nvarchar(1000)")
    private String msg;

    /**
     * 建立時間
     */
    @Column(name = "CREAT_TIME")
    private Date createTime;

    /**
     * 發送時間
     */
    @Column(name = "SEND_TIME")
    private Date sendTime;

    /**
     * 明細檔狀態；select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;

    /**
     * 明細檔狀態；select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "BC_STATUS", columnDefinition = "nvarchar(20)")
    private String bcStatus;

    /**
     * 明細檔狀態；select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "PNP_STATUS", columnDefinition = "nvarchar(20)")
    private String pnpStatus;

    /**
     * 明細檔狀態；select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "SMS_STATUS", columnDefinition = "nvarchar(20)")
    private String smsStatus;

    /**
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 通路參數
     */
    @Column(name = "PROC_FLOW", columnDefinition = "nvarchar(3)")
    private String procFlow;

    /**
     * 主檔通路執行階段；select for update wait時更新此欄位，防止重複發送
     * 對應procFlow的各種階段；以procFlow=3(PROC_FLOW_BC_PNP_SMS)為例，則分為BC、PNP、SMS三個procStage，stage之中分各種status
     */
    @Column(name = "PROC_STAGE", columnDefinition = "nvarchar(50)")
    private String procStage;

    /**
     * BC推播時間
     */
    @Column(name = "LINE_PUSH_TIME")
    private Date linePushTime;

    /**
     * PNP推播時間
     */
    @Column(name = "PNP_TIME")
    private Date pnpTime;

    /**
     * PNP 接收Line回傳時間
     */
    @Column(name = "PNP_DELIVERY_TIME")
    private Date pnpDeliveryTime;

    /**
     * web hook 送來 PNP DELIVERY的到期時間
     */
    @Column(name = "PNP_DELIVERY_EXPIRE_TIME")
    private Date pnpDeliveryExpireTime;

    /**
     * SMS 發送時間
     */
    @Column(name = "SMS_TIME")
    private Date smsTime;

    /**
     * 轉SMS使用的檔名 ；因為轉SMS時，是抓當下資料庫發送失敗的資料，
     * 組成一個檔案來轉送SMS，有可能一個原檔轉到SMS時變成多個檔案(發送時間問題)，
     * 所以在detail紀錄對應的SMS檔案
     */
    @Column(name = "SMS_FILE_NAME", columnDefinition = "nvarchar(200)")
    private String smsFileName;

    /**
     * Flex Template id
     */
    @Column(name = "FLEX_TEMPLATE_ID")
    private String flexTemplateId;

    /**
     * 處
     */
    @Column(name = "DIVISION_NAME", columnDefinition = "nvarchar(50)")
    private String divisionName;

    /**
     * 部
     */
    @Column(name = "DEPARTMENT_NAME", columnDefinition = "nvarchar(50)")
    private String departmentName;

    /**
     * 組
     */
    @Column(name = "GROUP_NAME", columnDefinition = "nvarchar(50)")
    private String groupName;


    @PrePersist
    public void prePersist() {
        createTime = new Date();
        modifyTime = createTime;
        log.info("Create Time is Update to : {}", DataUtils.formatDateToString(createTime, "yyyy-MM-dd HH:mm:ss"));
        log.info("Modify Time is Update to : {}", DataUtils.formatDateToString(modifyTime, "yyyy-MM-dd HH:mm:ss"));
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = new Date();
        log.info("Modify Time is Update to : {}", DataUtils.formatDateToString(modifyTime, "yyyy-MM-dd HH:mm:ss"));
    }
}
