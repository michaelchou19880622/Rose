package com.bcs.core.taishin.circle.pnp.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author ???
 */
@Setter
@Getter
@Entity
@Table(name = "BCS_PNP_DELIVERY_RECORD",
        indexes = {
                @Index(name = "INDEX_0", columnList = "PNP_DETAIL_ID")
        })
public class PnpDeliveryRecord extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static final String THIS_TYPE = "PNP_DELIVERY";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PNP_DELIVERY_ID")
    private Long pnpDeliveryId;

    @Column(name = "PNP_DETAIL_ID")
    private Long pnpDetailId;

    @Column(name = "UID", columnDefinition = "nvarchar(50)")
    private String uid;

    @Column(name = "DELIVERY_TIME")
    private Date deliveryTime = new Date();
}
