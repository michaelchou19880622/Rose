package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Slf4j(topic = "PnpRecorder")
@Repository
public class PNPMaintainAccountModelCustomImpl implements PNPMaintainAccountModelCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public List<PNPMaintainAccountModel> queryUseConditions(String divisionName, String departmentName, String groupName,
                                                            String pccCode, String account, String employeeId, String accountType, Boolean status) {
        StringBuilder sb = new StringBuilder();

        // Original (All Blank = All Show)
        sb.append("SELECT * FROM BCS_PNP_MAINTAIN_ACCOUNT WHERE 1=1");

        if (StringUtils.isNotBlank(divisionName)) {
            sb.append(" AND DIVISION_NAME='");
            sb.append(divisionName);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(departmentName)) {
            sb.append(" AND DEPARTMENT_NAME='");
            sb.append(departmentName);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(groupName)) {
            sb.append(" AND GROUP_NAME='");
            sb.append(groupName);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(" AND PCC_CODE='");
            sb.append(pccCode);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(account)) {
            sb.append(" AND ACCOUNT='");
            sb.append(account);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(employeeId)) {
            sb.append(" AND EMPLOYEE_ID='");
            sb.append(employeeId);
            sb.append("'");
        }
        if (StringUtils.isNotBlank(accountType) && ObjectUtils.notEqual("All", accountType)) {
            sb.append(" AND ACCOUNT_TYPE='");
            sb.append(accountType);
            sb.append("'");
        }
        if (status != null) {
            sb.append(" AND STATUS='");
            sb.append(status);
            sb.append("'");
        }
        log.info("sqlString11:" + sb.toString());
        Query query = entityManager.createNativeQuery(sb.toString(), PNPMaintainAccountModel.class);
        List<PNPMaintainAccountModel> resultList = query.getResultList();
        log.info("Result List : " + DataUtils.toPrettyJsonUseJackson(resultList));
        return resultList;
    }

}
