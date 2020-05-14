package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.*;

import java.util.Date;

/**
 * @ClassName PnpStsRptParam
 * @Description TODO
 * @Author ean
 * @Date 2020/5/6 上午 10:31
 * @Version 1.0
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnpStsRptParam {
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
    private String role;
    private String pageCount;
}
