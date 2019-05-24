package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;

public interface PnpDetailUnicaRepositoryCustom {
	public void batchInsertPnpDetailUnica(final List<PnpDetailUnica> list);
}
