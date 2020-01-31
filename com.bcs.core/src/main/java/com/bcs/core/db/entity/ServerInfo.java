//package com.bcs.core.db.entity;
//
//import com.bcs.core.utils.DataUtils;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Index;
//import javax.persistence.PrePersist;
//import javax.persistence.PreUpdate;
//import javax.persistence.Table;
//import java.util.Date;
//
///**
// * Server Information
// *
// * @author Alan
// */
//@Slf4j
//@NoArgsConstructor
//@Getter
//@Setter
//@Entity
//@Table(name = "BCS_SERVER_INFO",
//        indexes = {
//                @Index(name = "INDEX_0", columnList = "SERVER_TYPE"),
//                @Index(name = "INDEX_1", columnList = "COMPUTER_NAME"),
//                @Index(name = "INDEX_2", columnList = "IP")
//        })
//public class ServerInfo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "SERVER_TYPE", columnDefinition = "char(10)")
//    private ServerType serverType;
//
//    @Column(name = "COMPUTER_NAME", columnDefinition = "nvarchar(30)")
//    private String computerName;
//
//    @Column(name = "IP", columnDefinition = "char(39)")
//    private String ip;
//
//    @Column(name = "MODIFY_TIME")
//    private Date modifyTime;
//
//    @Column(name = "CREATE_TIME")
//    private Date createTime;
//
//    @PrePersist
//    public void prePersist(){
//        log.info("Save Before: {}", DataUtils.toPrettyJsonUseJackson(this));
//    }
//
//    @PreUpdate
//    public void preUpdate(){
//        log.info("Update Before: {}", DataUtils.toPrettyJsonUseJackson(this));
//    }
//
//    public enum ServerType {
//        /**
//         * Server Type
//         */
//        AP("Application Server"),
//        BE("Backend Server"),
//        DB("Database Server"),
//        GW("Gateway Server"),
//        LB("Load Balance Server");
//
//        String name;
//
//        ServerType(String name) {
//            this.name = name;
//        }
//    }
//}
