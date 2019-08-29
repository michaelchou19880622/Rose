package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

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

    public Long getPnpDeliveryId() {
        return pnpDeliveryId;
    }

    public void setPnpDeliveryId(Long pnpDeliveryId) {
        this.pnpDeliveryId = pnpDeliveryId;
    }

    public Long getPnpDetailId() {
        return pnpDetailId;
    }

    public void setPnpDetailId(Long pnpDetailId) {
        this.pnpDetailId = pnpDetailId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

}
