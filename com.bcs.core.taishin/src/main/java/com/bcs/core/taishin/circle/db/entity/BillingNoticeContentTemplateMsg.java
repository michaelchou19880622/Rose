package com.bcs.core.taishin.circle.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ???
 */
@Setter
@Getter
@Entity
@Table(name = "BCS_BN_CONTENT_TEMPLATE")
public class BillingNoticeContentTemplateMsg extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static final String TEMPLATE_LEVEL_MAIN = "MAIN"; // 暫時用不上
    // For Carousel Type
    public static final String TEMPLATE_LEVEL_COLUMN = "COLUMN";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DELETE = "DELETE";
    public static final String STATUS_AUTOREPLY = "AUTOREPLY"; // 暫時用不上

    public static final String TEMPLATE_TYPE_BUTTONS = "buttons";
    public static final String TEMPLATE_TYPE_CONFIRM = "confirm";
    public static final String TEMPLATE_TYPE_CAROUSEL = "carousel";

    @Id
    @Column(name = "TEMPLATE_ID", columnDefinition = "nvarchar(50)")
    private String templateId;

    //產品開關：當單一產品不要發送的時候調整此開關
    @Column(name = "PRODUCT_SWITCH", nullable = false)
    @ColumnDefault("1")
    private boolean productSwitch;

    @Column(name = "ALT_TEXT", columnDefinition = "nvarchar(500)")
    private String altText;

    @Column(name = "TEMPLATE_TYPE", columnDefinition = "nvarchar(50)")
    private String templateType;

    // 暫時用不上
    @Column(name = "TEMPLATE_LEVEL", columnDefinition = "nvarchar(50)")
    private String templateLevel;

    // 暫時用不上
    @Column(name = "TEMPLATE_PARENT_ID", columnDefinition = "nvarchar(50)")
    private String templateParentId;

    @Column(name = "TEMPLATE_IMAGE_ID", columnDefinition = "nvarchar(50)")
    private String templateImageId;

    //比對ftp檔案header 對應template用
    @Column(name = "TEMPLATE_TITLE", columnDefinition = "nvarchar(200)")
    private String templateTitle;
    // 暫時用不上 - BCS_BILLING_NOTICE_DETAIL 內有
    @Column(name = "TEMPLATE_TEXT", columnDefinition = "nvarchar(500)")
    private String templateText;

    //宵禁起始時間：24小時制 HHMMSS
    @Column(name = "CURFEW_START_TIME", columnDefinition = "nvarchar(10)")
    private String curfewStartTime;

    //宵禁結束時間：24小時制 HHMMSS
    @Column(name = "CURFEW_END_TIME", columnDefinition = "nvarchar(10)")
    private String curfewEndTime;

    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
    private String modifyUser;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;

    // 暫時用不上
    @Column(name = "TEMPLATE_LETTER", columnDefinition = "nvarchar(5)")
    private String templateLetter;

    @PrePersist
    public void prePersist() {
        modifyTime = Calendar.getInstance().getTime();
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = Calendar.getInstance().getTime();
    }
}
