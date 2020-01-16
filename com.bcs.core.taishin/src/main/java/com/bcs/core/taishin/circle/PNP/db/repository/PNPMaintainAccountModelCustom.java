package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;

import java.util.List;

public interface PNPMaintainAccountModelCustom {

    public List<PNPMaintainAccountModel> queryUseConditions(
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType, Boolean status);

}
