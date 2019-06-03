package com.bcs.core.taishin.circle.PNP.ftp;

import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;

public enum PNPFTPType {
	
	MITAKE("mitake",AbstractPnpMainEntity.SOURCE_MITAKE, "BCS_PNP_MAIN_MITAKE", "BCS_PNP_DETAIL_MITAKE"),
	EVERY8D("every8d", AbstractPnpMainEntity.SOURCE_EVERY8D, "BCS_PNP_MAIN_EVERY8D", "BCS_PNP_DETAIL_EVERY8D"),
	MING("ming" , AbstractPnpMainEntity.SOURCE_MING, "BCS_PNP_MAIN_MING", "BCS_PNP_DETAIL_MING"),
	UNICA("unica", AbstractPnpMainEntity.SOURCE_UNICA, "BCS_PNP_MAIN_UNICA", "BCS_PNP_DETAIL_UNICA");
	
	private String source;
	private String value;
	private String mainTable;
	private String detailTable;
    
	PNPFTPType(String value, String source, String mainTable, String detailTable) {
        this.value = value;
        this.source = source;
        this.mainTable = mainTable;
        this.detailTable = detailTable;
    }
	
	public String toString() {
		return value;
	}
	
	public String getSource() {
		return source;
	}

	public String getDetailTable() {
		return detailTable;
	}
	
	public String getMainTable() {
		return mainTable;
	}
}
