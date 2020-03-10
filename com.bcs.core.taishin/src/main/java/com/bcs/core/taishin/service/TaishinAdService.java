package com.bcs.core.taishin.service;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.AdminUser.RoleCode;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.AdUserSyncModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class RichartAdService {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(RichartAdService.class);

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void syncAdUser(AdUserSyncModel model) throws Exception {
        logger.debug("syncAdUser");

        String account = model.getUserId();
        if (StringUtils.isBlank(account)) {
            throw new Exception("AccountNull");
        }

        AdminUser adminUser = adminUserService.findOne(account);

        if (adminUser == null) {
            adminUser = new AdminUser();
            adminUser.setAccount(account);
//			adminUser.setModifyTime(new Date());
        }
        String prefix = CoreConfigReader.getString(CONFIG_STR.PASSWORD_PREFIX, true);
        String suffix = CoreConfigReader.getString(CONFIG_STR.PASSWORD_SUFFIX, true);
        account = account.toLowerCase();
        adminUser.setPassword(passwordEncoder.encode(prefix + account + suffix));

        adminUser.setModifyUser("SYSTEM");

        String name = model.getName();
        if (StringUtils.isBlank(name)) {
            throw new Exception("NameNull");
        }
        adminUser.setUserName(name);

        String roseline = model.getRoseline();
        logger.info("roseline = " + roseline);

        String role = "";
        boolean roleCheck = false;
        if (StringUtils.isBlank(roseline)) {
            throw new Exception("LinebcNull");
        }

        RoleCode[] roles = RoleCode.values();
        for (RoleCode code : roles) {
            logger.info("code.getRoleName() = " + code.getRoleName());

            if (code.getRoleName().equals(roseline)) {
                role = code.getRoleId();
                roleCheck = true;
            }
        }
        for (RoleCode code : roles) {
            if (code.getRoleNameEn().equals(roseline)) {
                role = code.getRoleId();
                roleCheck = true;
            }
        }
        if (roleCheck) {
            adminUser.setRole(role);
        } else {
            throw new Exception("RoleError");
        }

        adminUser.setEmail(model.getEmail());
        adminUser.setTelephone(model.getTelephone());
        adminUser.setDepartment(model.getDepartment());
        adminUser.setPepper(model.getPepper());
        adminUser.setRobot(model.getRobot());
        adminUser.setDesk(model.getDesk());
        adminUser.setLinebc(model.getLinebc());
        adminUser.setRoseline(model.getRoseline());
        adminUser.setModifyTime(new Date());

        adminUser.setViewLimit(model.getViewLimit());
        adminUser.setCanCopy(model.getCanCopy());
        adminUser.setCanSave(model.getCanSave());
        adminUser.setCanPrinting(model.getCanPrinting());

        adminUserService.save(adminUser);
    }
}
