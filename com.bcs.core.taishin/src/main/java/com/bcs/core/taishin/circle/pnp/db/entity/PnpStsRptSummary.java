package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName PnpStsRptSummary
 * @Description TODO
 * @Author ean
 * @Date 2020/5/6 上午 10:51
 * @Version 1.0
 **/
@Getter
@Setter
@Entity


//@start_date AS NVARCHAR(20) = NULL, -- 預搜尋的開始日期, NULL 表示不設限
//@end_date AS NVARCHAR(20) = NULL, -- 預搜尋的結束日期, NULL 表示不設限
//@account AS NVARCHAR(50) = '', -- 預搜尋的帳號, 空字串 表示不設限
//@pcccode AS NVARCHAR(50) = '' -- 預搜尋的PCC, 空字串 表示不設限

@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "getPNPStsRptSummary",
                        procedureName = "usp_getPNPStsRptSummary",
                        resultClasses = { PnpStsRptSummary.class },
                        parameters = {
                                @StoredProcedureParameter(
                                        name = "start_date",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                @StoredProcedureParameter(
                                        name = "end_date",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                @StoredProcedureParameter(
                                        name = "account",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                @StoredProcedureParameter(
                                        name = "pcccode",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                )
                        }

                )
        }
)

public class PnpStsRptSummary implements Serializable  {

    @Column(name = "DATE_COUNT")
    private long date_count;

    @Id
    @Column(name = "SEND_DATE")
    private String send_date;

    @Column(name = "TOTAL")
    private long total;

    @Column(name = "BC_TOTAL")
    private long bc_total;

    @Column(name = "BC_OK")
    private long bc_ok;

    @Column(name = "BC_NO")
    private long bc_no;

    @Column(name = "BC_RATE")
    private String bc_rate;

    @Column(name = "PNP_TOTAL")
    private long pnp_total;

    @Column(name = "PNP_OK")
    private long pnp_ok;

    @Column(name = "PNP_NO")
    private long pnp_no;

    @Column(name = "PNP_RATE")
    private String pnp_rate;

    @Column(name = "SMS_TOTAL")
    private long sms_total;

    @Column(name = "SMS_OK")
    private long sms_ok;

    @Column(name = "SMS_NO")
    private long sms_no;

    @Column(name = "SMS_RATE")
    private String sms_rate;

}
