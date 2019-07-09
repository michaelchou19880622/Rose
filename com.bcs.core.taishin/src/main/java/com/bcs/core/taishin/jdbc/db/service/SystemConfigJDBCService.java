package com.bcs.core.taishin.jdbc.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.jdbc.db.repository.SystemConfigJDBCDAO;
import com.bcs.core.taishin.jdbc.db.component.SystemConfigJDBC;

@Service
public class SystemConfigJDBCService {
	@Autowired
	SystemConfigJDBCDAO systemConfigJDBCDAO;

	public void addMember(SystemConfigJDBC memberAccount12){
		systemConfigJDBCDAO.addMember(memberAccount12);
	}
}