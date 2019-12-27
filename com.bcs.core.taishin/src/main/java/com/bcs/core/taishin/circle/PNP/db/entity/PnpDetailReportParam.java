package com.bcs.core.taishin.circle.PNP.db.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnpDetailReportParam {
    public static final String CREATE_TIME = "createTime";
    public static final String ORDER_TIME = "orderTime";
    
    private String dateType = CREATE_TIME;
    private Date startDate;
    private Date endDate;
    private boolean isPageable = false;
    private Integer page = 1;
    private String account;
    private String pccCode;
    private String sourceSystem;
    private String employeeId;
    private String phone;
}
