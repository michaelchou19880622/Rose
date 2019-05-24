package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;

public interface PnpDetailEvery8dRepositoryCustom{
	public void batchInsertPnpDetailEvery8d(final List<PnpDetailEvery8d> list);
}
