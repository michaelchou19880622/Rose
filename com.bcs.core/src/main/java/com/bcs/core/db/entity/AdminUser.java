package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "BCS_ADMIN_USER")
public class AdminUser extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    @Getter
    public enum RoleCode {
        /**
         * Role Code
         */
        ROLE_ADMIN("ROLE_ADMIN", "管理者", "manager"),
        ROLE_EDIT("ROLE_EDIT", "編輯人員", "editor"),
        ROLE_MARKET("ROLE_MARKET", "行銷人員", "marketing"),
        ROLE_PNP_ADMIN("ROLE_PNP_ADMIN", "PNP管理者", "pnp_manager"),
        ROLE_PNP_SEND_LINE_SEND("ROLE_PNP_SEND_LINE_SEND", "PNP發送暨LINE點發送者", "pnp_send_line_send"),
        ROLE_PNP_SEND_LINE_VERIFY("ROLE_PNP_SEND_LINE_VERIFY", "PNP發送暨LINE點審核者", "pnp_send_line_verify"),
        ROLE_REPORT("ROLE_REPORT", "報表查詢人員", "report"),
        ROLE_LINE_SEND("ROLE_LINE_SEND", "LINE點發送者", "line_send"),
        ROLE_LINE_VERIFY("ROLE_LINE_VERIFY", "LINE點審核者", "line_verify"),
        ROLE_CUSTOMER_SERVICE("ROLE_CUSTOMER_SERVICE", "客服人員", "customer_service"),
        ;

        private String roleId;
        private String roleName;
        private String roleNameEn;

        RoleCode(String roleId, String roleName, String roleNameEn) {
            this.roleId = roleId;
            this.roleName = roleName;
            this.roleNameEn = roleNameEn;
        }

        @Override
        public String toString() {
            return roleId;
        }
    }

    @Id
    @Column(name = "ACCOUNT", columnDefinition = "nvarchar(50)")
    private String account;

    @Column(name = "USER_NAME", columnDefinition = "nvarchar(50)")
    private String userName;

    @Column(name = "DEPARTMENT", columnDefinition = "nvarchar(50)")
    private String department;

    @Column(name = "EMAIL", columnDefinition = "nvarchar(50)")
    private String email;

    @Column(name = "TELEPHONE", columnDefinition = "nvarchar(50)")
    private String telephone;

    @Column(name = "ROLES", columnDefinition = "nvarchar(50)")
    private String roles;

    @Column(name = "LINEBC", columnDefinition = "nvarchar(50)")
    private String linebc;

    @Column(name = "ROSELINE", columnDefinition = "nvarchar(50)")
    private String roseline;

    @Column(name = "PEPPER", columnDefinition = "nvarchar(50)")
    private String pepper;

    @Column(name = "ROBOT", columnDefinition = "nvarchar(50)")
    private String robot;

    @Column(name = "DESK", columnDefinition = "nvarchar(50)")
    private String desk;

    @Column(name = "PASSWORD", columnDefinition = "nvarchar(200)")
    private String password;

    @Column(name = "ROLE", columnDefinition = "nvarchar(50)")
    private String role;

    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
    private String modifyUser;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    @Column(name = "MID", columnDefinition = "nvarchar(50)")
    private String mid;

    @Column(name = "VIEW_LIMIT")
    private Boolean viewLimit;

    @Column(name = "CAN_COPY")
    private Boolean canCopy;

    @Column(name = "CAN_SAVE")
    private Boolean canSave;

    @Column(name = "CAN_PRINTING")
    private Boolean canPrinting;
}
