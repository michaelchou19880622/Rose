package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.LineUser;

public interface LineUserRepositoryCustom {

	public void bulkPersist(List<LineUser> lineUsers);
}
