package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @ClassName PNPBlockHistoryCount
 * @Description TODO
 * @Author ean
 * @Date 2020/6/10 下午 02:51
 * @Version 1.0
 **/

@Getter
@Setter
@Entity
@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "getPNPBlockHistoryCount",
                        procedureName = "usp_getPNPBlockHistoryCount",
                        resultClasses = {PNPBlockHistoryCount.class},
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
                                ),
                                // 設定狀態 0:移除 1:啟用
                                @StoredProcedureParameter(
                                        name = "block_enable",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                )
                        }
                )
        }
)
public class PNPBlockHistoryCount {

    @Id
    // 明細資料筆數
    @Column(name = "CNT")
    private Long count;
}
