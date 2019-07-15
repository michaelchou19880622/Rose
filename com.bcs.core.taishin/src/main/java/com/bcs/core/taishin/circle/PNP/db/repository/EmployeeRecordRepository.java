package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.EmployeeRecord;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;

@Repository
public interface EmployeeRecordRepository extends EntityRepository<EmployeeRecord, Long> {	
}
