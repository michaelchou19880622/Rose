package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @ClassName PNPUpdateBlockSend
 * @Description 異動啟用中名單並寫入歷史紀錄當中
 * @Author ean
 * @Date 2020/6/11 下午 01:31
 * @Version 1.0
 **/

@Getter
@Setter
@Entity
@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "updatePNPBlockSend",
                        procedureName = "usp_updatePNPBlock",
                        resultClasses = {PNPUpdateBlockSend.class},
                        parameters = {
                                // 手機門號，不指定填空白字串，"0912345678"
                                @StoredProcedureParameter(
                                        name = "mobile",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 設定狀態 0:移除 1:啟用
                                @StoredProcedureParameter(
                                        name = "block_enable",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                ),
                                // 客群標籤，不指定填空白字串，"24"
                                @StoredProcedureParameter(
                                        name = "group_tag",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 異動操作來源，不指定填空白字串,"oam"
                                @StoredProcedureParameter(
                                        name = "insert_user",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 寫入日期，YYYYMMDD
                                @StoredProcedureParameter(
                                        name = "insert_date",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 寫入時間，HH:mm:ss.SSS
                                @StoredProcedureParameter(
                                        name = "insert_time",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                ),
                                // 異動原因，
                                @StoredProcedureParameter(
                                        name = "modify_reason",
                                        type = String.class,
                                        mode = ParameterMode.IN
                                )

                        }
                )
        }
)
public class PNPUpdateBlockSend {

    @Id
    @Column(name = "HISTORY_REF_ID")
    private long historyRefId;
}
