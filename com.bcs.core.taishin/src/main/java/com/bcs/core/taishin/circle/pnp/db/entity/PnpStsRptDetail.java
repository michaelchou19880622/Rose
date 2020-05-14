package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName PnpStsRptDetail
 * @Description TODO
 * @Author ean
 * @Date 2020/5/6 下午 02:01
 * @Version 1.0
 **/
@Getter
@Setter
@Entity


//@show_page AS int = 1, -- 預顯示的頁面,從1開始
//@page_count AS int = 10, -- 每頁顯示的筆數
//@start_date AS NVARCHAR(20) = NULL, -- 預搜尋的開始日期, NULL 表示不設限
//@end_date AS NVARCHAR(20) = NULL, -- 預搜尋的結束日期, NULL 表示不設限
//@account AS NVARCHAR(50) = '', -- 預搜尋的帳號, 空字串 表示不設限
//@pcccode AS NVARCHAR(50) = '', -- 預搜尋的PCC, 空字串 表示不設限
//@total_page AS int = 0 OUTPUT -- 回傳資料列以@page_count 分頁時的總頁數

@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "getPNPStsRptDetail",
                        procedureName = "usp_getPNPStsRptDetail",
                        resultClasses = { PnpStsRptDetail.class },
                        parameters = {
                                @StoredProcedureParameter(
                                        name = "show_page",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                ),
                                @StoredProcedureParameter(
                                        name = "page_count",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                ),
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
                                ),
                                @StoredProcedureParameter(
                                        name = "total_page",
                                        type = Integer.class,
                                        mode = ParameterMode.OUT
                                )
                        }

                )
        }
)
public class PnpStsRptDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6292751164343105258L;

	// 發送日期
    @Id
    @Column(name = "SEND_DATE")
    private String send_date;

    // 總通數
    @Column(name = "TOTAL")
    private long total;
    
    // BC總通數
    @Column(name = "BC_TOTAL")
    private long bc_total;

    // BC成功通數
    @Column(name = "BC_OK")
    private long bc_ok;

    // BC失敗通數
    @Column(name = "BC_NO")
    private long bc_no;

    // BC成功率
    @Column(name = "BC_RATE")
    private String bc_rate;

    // PNP總通數
    @Column(name = "PNP_TOTAL")
    private long pnp_total;
  
    // PNP成功通數
    @Column(name = "PNP_OK")
    private long pnp_ok;
 
    // PNP失敗通數
    @Column(name = "PNP_NO")
    private long pnp_no;
  
    // PNP成功率
    @Column(name = "PNP_RATE")
    private String pnp_rate;

    // SMS通路總通數
    @Column(name = "SMS_TOTAL")
    private long sms_total;
    
    // SMS成功總通數
    @Column(name = "SMS_OK")
    private long sms_ok;

    // SMS失敗通數
    @Column(name = "SMS_NO")
    private long sms_no;

    // SMS成功點數
    @Column(name = "SMS_POINT")
    private long sms_point;

    // SMS成功率
    @Column(name = "SMS_RATE")
    private String sms_rate;
}
