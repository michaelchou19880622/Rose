package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author ???
 */
@Setter
@Getter
@Entity
@Table(name = "BCS_PNP_CONTENT_TEMPLATE_ACTION",
        indexes = {
                @Index(name = "INDEX_0", columnList = "TEMPLATE_ID"),
                @Index(name = "INDEX_1", columnList = "LINK_ID"),
        })
public class PnpContentTemplateMsgAction extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DELETE = "DELETE";
    public static final String STATUS_AUTOREPLY = "AUTOREPLY"; // 暫時用不上

    public static final String ACTION_TYPE_POSTBACK = "postback";
    public static final String ACTION_TYPE_MESSAGE = "message";
    public static final String ACTION_TYPE_URI = "uri";

    @Id
    @Column(name = "TEMPLATE_ID_ACTION", columnDefinition = "nvarchar(50)")
    private String templateIdAction;

    @Column(name = "TEMPLATE_ID", columnDefinition = "nvarchar(50)")
    private String templateId;

    // 暫時用不上
    @Column(name = "ACTION_LETTER", columnDefinition = "nvarchar(5)")
    private String actionLetter;

    @Column(name = "ACTION_TYPE", columnDefinition = "nvarchar(50)")
    private String actionType;

    @Column(name = "ACTION_LABEL", columnDefinition = "nvarchar(50)")
    private String actionLabel;

    @Column(name = "ACTION_DATA", columnDefinition = "nvarchar(500)")
    private String actionData;

    @Column(name = "ACTION_TEXT", columnDefinition = "nvarchar(500)")
    private String actionText;

    @Column(name = "LINK_ID", columnDefinition = "nvarchar(50)")
    private String linkId;

    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;
}
