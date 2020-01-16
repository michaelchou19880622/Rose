package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author ???
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BCS_PNP_CONTENT_LINK")
public class PnpContentLink extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "LINK_ID", columnDefinition = "nvarchar(50)")
    private String linkId;

    @Column(name = "LINK_TITLE", columnDefinition = "nvarchar(1000)")
    private String linkTitle;

    @Column(name = "LINK_URL", columnDefinition = "nvarchar(1000)")
    private String linkUrl;

    @Column(name = "LINK_PREVIEW_IMAGE", columnDefinition = "nvarchar(50)")
    private String linkPreviewImage;

    @Column(name = "LINK_TAG", columnDefinition = "nvarchar(50)")
    private String linkTag;

    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
    private String modifyUser;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;


    public PnpContentLink(String linkId) {
        this.linkId = linkId;
    }
}
