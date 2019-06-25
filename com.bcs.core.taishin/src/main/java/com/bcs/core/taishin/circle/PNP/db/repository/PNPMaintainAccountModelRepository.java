package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;

@Repository
public interface PNPMaintainAccountModelRepository extends EntityRepository<PNPMaintainAccountModel, Long> {	
	public List<PNPMaintainAccountModel> findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
			String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType);
	public List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent);
	
	@Transactional(timeout = 30)
	@Query("select x from PNPMaintainAccountModel x where x.accountType = ?1 order by x.modifyTime desc")
	public List<PNPMaintainAccountModel> findByAccountType(String accountType);
}
