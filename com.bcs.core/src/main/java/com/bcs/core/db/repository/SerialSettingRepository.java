package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SerialSetting;
import com.bcs.core.db.persistence.EntityRepository;

public interface SerialSettingRepository extends EntityRepository<SerialSetting, String> {

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x from SerialSetting x where x.serialLevel = ?1 order by MODIFY_TIME desc")
	public List<SerialSetting> findByLevel(String serialLevel);
}
