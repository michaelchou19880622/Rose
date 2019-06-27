package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;

@Repository
public class PNPMaintainAccountModelCustomImpl implements PNPMaintainAccountModelCustom{	
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<PNPMaintainAccountModel> queryUseConditions(String divisionName, String departmentName, String groupName, 
			String pccCode, String account, String employeeId, String accountType, Boolean status) {
		Date  now = Calendar.getInstance().getTime();
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("select * from BCS_PNP_MAINTAIN_ACCOUNT where 1=1 ");
		if(StringUtils.isNotBlank(divisionName)){
			sqlString.append("AND DIVISION_NAME = :divisionName ");
		}
		if(StringUtils.isNotBlank(departmentName)){
			sqlString.append("AND DEPARTMENT_NAME = :departmentName ");
		}
		if(StringUtils.isNotBlank(groupName)){
			sqlString.append("AND GROUP_NAME = :groupName ");
		}
		if(StringUtils.isNotBlank(pccCode)){
			sqlString.append("AND PCC_CODE = :pccCode ");
		}
		if(StringUtils.isNotBlank(account)){
			sqlString.append("AND ACCOUNT = :account ");
		}
		if(StringUtils.isNotBlank(employeeId)){
			sqlString.append("AND EMPLOYEE_ID = :employeeId ");
		}
		if(StringUtils.isNotBlank(accountType)){
			sqlString.append("AND ACCOUNT_TYPE = :accountType ");
		}
		if(status != null){
			sqlString.append("AND STATUS = :status ");
		}
		
		Query query = (Query) entityManager.createNativeQuery(sqlString.toString(),PNPMaintainAccountModel.class);
		if(StringUtils.isNotBlank(divisionName)){
			((javax.persistence.Query) query).setParameter("divisionName", divisionName);
		}
		if(StringUtils.isNotBlank(departmentName)){
			((javax.persistence.Query) query).setParameter("departmentName", departmentName);
		}
		if(StringUtils.isNotBlank(groupName)){
			((javax.persistence.Query) query).setParameter("groupName", groupName);
		}
		if(StringUtils.isNotBlank(pccCode)){
			((javax.persistence.Query) query).setParameter("pccCode", pccCode);
		}
		if(StringUtils.isNotBlank(account)){
			((javax.persistence.Query) query).setParameter("account", account);
		}
		if(StringUtils.isNotBlank(employeeId)){
			((javax.persistence.Query) query).setParameter("employeeId", employeeId);
		}
		if(StringUtils.isNotBlank(accountType)){
			((javax.persistence.Query) query).setParameter("accountType", accountType);
		}
		if(status != null){
			((javax.persistence.Query) query).setParameter("status", status);
		}
		List<PNPMaintainAccountModel> ResultList = ((javax.persistence.Query) query).getResultList();
		
		return ResultList;
	}

}
