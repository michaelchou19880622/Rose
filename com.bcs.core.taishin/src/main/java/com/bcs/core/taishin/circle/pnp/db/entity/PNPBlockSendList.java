package com.bcs.core.taishin.circle.pnp.db.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @ClassName PNPBlockSendList
 * @Description 取得啟用中的黑名單列表
 * @Author ean
 * @Date 2020/6/4 下午 01:33
 * @Version 1.0
 **/

@Getter
@Setter
@Entity
@NamedStoredProcedureQueries(
        {
                @NamedStoredProcedureQuery(
                        name = "qryPNPBlockSendList",
                        procedureName = "usp_qryPNPBlockSendList",
                        resultClasses = {PNPBlockSendList.class},
                        parameters = {
                                // 指定查詢頁數，"1"
                                @StoredProcedureParameter(
                                        name = "show_page",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                ),
                                // 指定查詢每頁詳細筆數，"10"
                                @StoredProcedureParameter(
                                        name = "page_count",
                                        type = Integer.class,
                                        mode = ParameterMode.IN
                                ),
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
public class PNPBlockSendList {

    // MOBILE, S.UID, MODIFY_REASON, S.MODIFY_TIME, BLOCK_ENABLE, GROUP_TAG, INSERT_USER
    @Id

    @Column(name = "PHONE", columnDefinition = "VARCHAR(15)")
    private String phone;

    @Column(name = "UID", columnDefinition = "CHAR(35)")
    private String uid;

    @Column(name = "MODIFY_REASON", columnDefinition = "NVARCHAR(64)")
    private String modifyReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "BLOCK_ENABLE", columnDefinition = "TINYINT")
    private Short blockEnable;

    @Column(name = "GROUP_TAG", columnDefinition = "NCHAR(8)")
    private String groupTag;

    @Column(name = "INSERT_USER", columnDefinition = "NVARCHAR(32)")
    private String insertUser;

}
