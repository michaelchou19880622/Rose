package com.bcs.core.taishin.circle.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;

/**
 * BCS_BILLING_NOTICE_DETAIL與BCS_BILLING_NOTICE_MAIN為1對多關係
 *
 * @author ???
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "BCS_BILLING_NOTICE_DETAIL",
        indexes = {
                @Index(name = "INDEX_0", columnList = "NOTICE_DETAIL_ID"),
                @Index(name = "INDEX_1", columnList = "PARENT_TYPE"),
                @Index(name = "INDEX_2", columnList = "MSG_TYPE"),
                @Index(name = "INDEX_3", columnList = "NOTICE_MAIN_ID"),
                @Index(name = "INDEX_4", columnList = "TITLE"),
                @Index(name = "INDEX_5", columnList = "STATUS"),
                @Index(name = "INDEX_6", columnList = "CREAT_TIME"),
                @Index(name = "INDEX_7", columnList = "SEND_TIME"),
                @Index(name = "INDEX_8", columnList = "MODIFY_TIME"),
        })
public class BillingNoticeDetail extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;
    public static final String MSG_TYPE_TEMPLATE = "template";

    /**
     * 帳務通知明細ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTICE_DETAIL_ID")
    private Long noticeDetailId;

    /**
     * 帳務通知主檔ID
     */
    @Column(name = "NOTICE_MAIN_ID")
    private Long noticeMainId;

    /**
     *
     */
    @Column(name = "PARENT_TYPE", columnDefinition = "nvarchar(50)")
    private String parentType;

    /**
     *
     */
    @Column(name = "MSG_TYPE", columnDefinition = "nvarchar(50)")
    private String msgType = MSG_TYPE_TEMPLATE;

    /**
     * Line uid
     */
    @Column(name = "UID", columnDefinition = "nvarchar(50)")
    private String uid;

    /**
     * 訊息title
     */
    @Column(name = "TITLE", columnDefinition = "nvarchar(500)")
    private String title;

    /**
     * 訊息內文
     */
    @Column(name = "TEXT", columnDefinition = "nvarchar(1000)")
    private String text;

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
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    @PrePersist
    public void prePersist() {
        createTime = new Date();
        modifyTime = createTime;
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = new Date();
    }
}
