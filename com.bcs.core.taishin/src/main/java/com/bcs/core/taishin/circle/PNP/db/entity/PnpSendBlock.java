package com.bcs.core.taishin.circle.PNP.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * Pnp Send Block Entity
 * @author Alan Chen
 */
@Getter
@Setter
@Entity
@Table(name = "BCS_PNP_SEND_BLOCK", indexes = { @Index(name = "IDX_UID", columnList = "UID"),
    @Index(name = "IDX_CREATE_TIME", columnList = "CREATE_TIME") })
public class PnpSendBlock {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotBlank
  @Column(name = "PHONE", columnDefinition = "varchar(15)")
  private String phone;
  @Column(name = "UID", columnDefinition = "varchar(50)")
  private String uid;
  @NotBlank
  @Column(name = "CREATE_TIME")
  private Date createTime;
  @NotBlank
  @Column(name = "MODIFY_TIME")
  private Date modifyTime;
}