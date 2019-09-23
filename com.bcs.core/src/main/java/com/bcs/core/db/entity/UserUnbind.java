package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="BCS_USER_UNBIND",
indexes = {  
		   @Index(name = "INDEX_0", columnList = "ACCOUNT_ID"),
	       @Index(name = "INDEX_1", columnList = "MODIFY_TIME"),
	       @Index(name = "INDEX_2", columnList = "MODIFY_ID")
	}
)
public class UserUnbind extends AbstractBcsEntity{
	
    private static final long serialVersionUID = 1L;
    
    public static final String UNBIND = "UNBIND";
    public static final String CHECK = "CHECK";
    
    public static final String S001 = "S001"; //成功
    public static final String E001 = "E001"; //失敗
    
    @Id
    @Column(name="ID", nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="ACCOUNT_ID", columnDefinition="nvarchar(10)")
    private String accountId;
    
    @Column(name="MODIFY_TIME")
    private Date modifyTime;
    
    @Column(name="MODIFY_ID", columnDefinition="nvarchar(50)")
    private String modifyId;
    
    @Column(name="MODIFY_IP", columnDefinition="nvarchar(50)")
    private String modifyIp;
    
    @Column(name="ACCOUNT_UID", columnDefinition="nvarchar(50)")
    private String accountUid;
    
    @Column(name="ACTION", columnDefinition="nvarchar(50)")
    private String action;
    
    @Column(name="ACTION_RESULT", columnDefinition="nvarchar(50)")
    private String actionResult;



}
