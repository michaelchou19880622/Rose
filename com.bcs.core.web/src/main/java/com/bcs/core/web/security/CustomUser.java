package com.bcs.core.web.security;

import com.bcs.core.db.entity.AdminUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 使用者物件
 *
 * @author Kevin
 */
@Slf4j
public class CustomUser implements UserDetails {

    private static final long serialVersionUID = 1L;
    private AdminUser adminUser;
    private List<GrantedAuthority> authorities = new ArrayList<>();

    public CustomUser(AdminUser adminUser) {
        this.adminUser = adminUser;
        authorities.add(new SimpleGrantedAuthority(adminUser.getRole()));
        log.info("User role   : " + adminUser.getRole());
        log.info("Authorities : " + authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return adminUser.getPassword();
    }

    @Override
    public String getUsername() {
        return adminUser.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isCancopy() {
        return adminUser.getCanCopy();
    }

    public boolean isCanprinting() {
        return adminUser.getCanPrinting();
    }

    public String getDay() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(now);
    }

    public boolean isAdmin() {
        return AdminUser.RoleCode.ROLE_ADMIN.toString().equals(adminUser.getRole());
    }

    public String getAccount() {
        return adminUser.getAccount();
    }

    public String getMid() {
        return adminUser.getMid();
    }

    public String getRole() {
        return adminUser.getRole();
    }

    public void setMid(String mid) {
        adminUser.setMid(mid);
    }
}
