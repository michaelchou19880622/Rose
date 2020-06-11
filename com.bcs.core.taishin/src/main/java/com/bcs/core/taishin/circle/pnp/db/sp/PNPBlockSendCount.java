package com.bcs.core.taishin.circle.pnp.db.sp;

import com.bcs.core.taishin.circle.pnp.db.entity.PnpStsRptSummary;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @ClassName GetPNPBlockSendCount
 * @Description 取得啟用中黑名單筆數
 * @Author ean
 * @Date 2020/6/4 上午 11:39
 * @Version 1.0
 **/

@Getter
@Setter
@Entity
@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "getPNPBlockSendCount",
                        procedureName = "usp_getPNPBlockSendCount",
                        resultClasses = {PNPBlockSendCount.class},
                        parameters = {
                                // 日期條件區間開始，不指定填NULL，YYYYMMDD
                                @StoredProcedureParameter(
                                        name = "start_date",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 日期條件區間結束，不指定填NULL，YYYYMMDD
                                @StoredProcedureParameter(
                                        name = "end_date",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 手機門號，不指定填空白字串，"0912345678"
                                @StoredProcedureParameter(
                                        name = "mobile",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 異動操作來源，不指定填空白字串,"oam"
                                @StoredProcedureParameter(
                                        name = "insert_user",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 客群標籤，不指定填空白字串，"24"
                                @StoredProcedureParameter(
                                        name = "group_tag",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                )
                        }
                )
        }
)

public class PNPBlockSendCount {

    // 明細資料筆數
    @Column(name = "CNT")
    private Long count;

}
