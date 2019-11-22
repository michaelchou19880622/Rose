package com.bcs.core.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author ???
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "BCS_LINE_USER",
        indexes = {
                @Index(name = "INDEX_0", columnList = "STATUS"),
                @Index(name = "INDEX_1", columnList = "PHONE"),
                @Index(name = "INDEX_2", columnList = "MOBILE"),
                @Index(name = "INDEX_3", columnList = "MID"),
        })
public class LineUser extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_BINDED = "BINDED";
    public static final String STATUS_BLOCK = "BLOCK";
    public static final String STATUS_UNBIND = "UNBIND";
    public static final String STATUS_UNFRIEND = "UNFRIEND";

    @Id
    @Column(name = "MID", columnDefinition = "nvarchar(50)")
    private String mid;

    @Column(name = "SOURCE_TYPE", columnDefinition = "nvarchar(50)")
    private String soureType;

    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "CUSTID", columnDefinition = "nvarchar(50)")
    private String custId;

    @Column(name = "PHONE", columnDefinition = "nvarchar(50)")
    private String phone;

    @Column(name = "GENDER", columnDefinition = "nvarchar(5)")
    private String gender;

    @Column(name = "ADDRESS", columnDefinition = "nvarchar(100)")
    private String address;

    @Column(name = "CITYDISTRICT", columnDefinition = "nvarchar(10)")
    private String cityDistrict;

    @Column(name = "HASINV", columnDefinition = "nvarchar(10)")
    private String hasInv;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "NAME", columnDefinition = "NVARCHAR(50)")
    private String name = null;

    @Column(name = "MOBILE", columnDefinition = "NVARCHAR(15)")
    private String mobile = null;

    @Column(name = "EMAIL", columnDefinition = "NVARCHAR(100)")
    private String email = null;

    @Column(name = "BIRTHDAY", columnDefinition = "NVARCHAR(20)")
    private String birthday = null;

    @Column(name = "ISBINDED", columnDefinition = "nvarchar(50)")
    private String isBinded;
}
