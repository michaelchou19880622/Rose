package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.UserUnbind;
import com.bcs.core.db.repository.ActionUserCouponRepository;
import com.bcs.core.db.repository.CampaignFlowRepository;
import com.bcs.core.db.repository.UserUnbindRepository;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.utils.ErrorRecord;

@Service
public class UserUnbindService {
	@Autowired
	private UserUnbindRepository userUnbindRepository;    
	
	private static Logger logger = Logger.getLogger(UserUnbindService.class);
	
	public UserUnbind save(UserUnbind userUnbind) {
		return userUnbindRepository.save(userUnbind);
	}
}
