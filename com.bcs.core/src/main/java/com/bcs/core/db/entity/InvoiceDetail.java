package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_INVOICE_DETAIL", indexes = {})
public class InvoiceDetail extends AbstractBcsEntity{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "DETAIL_ID")
    private Long detailId = null;
    
    @Column(name = "AMOUNT")
    private Double amount = null;
    
    @Column(name = "DESCRIPTION", columnDefinition="NVARCHAR(256)")
    private String description = null;
    
    @Column(name = "UnitPrice")
    private Double unitPrice = null;
    
    @Column(name = "QUANTITY")
    private Integer quantity = null;

    @Column(name = "INVOICE_ID", columnDefinition="NVARCHAR(50)")
    private String invoiceId = null;
    
    @Column(name = "IS_MATCH")
    private Boolean isMatch = Boolean.FALSE;

    public InvoiceDetail() {}

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    
}
