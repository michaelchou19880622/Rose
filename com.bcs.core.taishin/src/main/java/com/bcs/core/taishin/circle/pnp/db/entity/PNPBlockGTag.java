package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @ClassName PNPBlockGTag
 * @Description 查詢黑名單歷史紀錄中曾經寫入的標籤名稱
 * @Author ean
 * @Date 2020/6/23 上午 10:11
 * @Version 1.0
 **/


@Getter
@Setter
@Entity
@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "qryPNPBlockGTag",
                        procedureName = "usp_qryPNPBlockGTag",
                        resultClasses = {PNPBlockGTag.class},
                        parameters = {
                        		// 篩選的客群標籤來源 (0 = 歷程紀錄, 1 = 作用中)
                                @StoredProcedureParameter(
                                        name = "in_active",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                )
                        }
                )
        }
)

public class PNPBlockGTag {

    @Id
    @Column(name = "GROUP_TAG")
    private String groupTag;
}
