package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.entity.UserUnbind;
import com.bcs.core.db.persistence.EntityRepository;

public interface UserUnbindRepository  extends EntityRepository<UserUnbind, Long>{

}
