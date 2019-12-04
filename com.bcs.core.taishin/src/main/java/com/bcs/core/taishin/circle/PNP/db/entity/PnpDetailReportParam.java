package com.bcs.core.taishin.circle.PNP.db.entity;

public class PnpDetailReportParam {
    private final String startDate;
    private final String endDate;
    private final String account;
    private final String pccCode;
    private final String sourceSystem;
    private final String empId;

    public PnpDetailReportParam(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.account = account;
        this.pccCode = pccCode;
        this.sourceSystem = sourceSystem;
        this.empId = empId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getAccount() {
        return account;
    }

    public String getPccCode() {
        return pccCode;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getEmpId() {
        return empId;
    }
}
