package com.bcs.core.taishin.circle.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.db.repository.OdsSystemConfigRepository;
import com.bcs.core.taishin.circle.db.entity.OdsSystemConfig;

@Service
public class OdsSystemConfigService {
	@Autowired
	OdsSystemConfigRepository odsSystemConfigRepository;

	public void save(OdsSystemConfig odsSystemConfig){
		odsSystemConfigRepository.save(odsSystemConfig);
	}
}