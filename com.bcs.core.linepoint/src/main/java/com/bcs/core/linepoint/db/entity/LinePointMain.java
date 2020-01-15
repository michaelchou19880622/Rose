package com.bcs.core.linepoint.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author ???
 */
@SqlResultSetMapping(name = "LinePointMain", entities = {
        @EntityResult(entityClass = LinePointMain.class),
})

@NamedNativeQueries({
        @NamedNativeQuery(name = "queryGetStatisticsReportPage", query = "select * from BCS_LINE_POINT_MAIN x "
                + "where x.ID in (SELECT DISTINCT y.LINE_POINT_MAIN_ID from BCS_LINE_POINT_DETAIL y where y.SEND_TIME >=  ?3 "
                + "and y.SEND_TIME <= ?4 ) and "
                + "x.TITLE like ('%' + ?1 + '%') and x.MODIFY_USER like ('%' + ?2 + '%') "
                + "and (x.status = 'COMPLETE' or x.status is null ) "
                + "order by x.MODIFY_TIME desc ", resultSetMapping = "LinePointMain"),
})

@Getter
@Setter
@Entity
@Table(name = "BCS_LINE_POINT_MAIN")
public class LinePointMain extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    /**
     * BCS
     */
    public static final String SEND_TYPE_BCS = "BCS";
    /**
     * API
     */
    public static final String SEND_TYPE_API = "API";

    public static final String SEND_TIMING_TYPE_IMMEDIATE = "IMMEDIATE";
    public static final String SEND_TIMING_TYPE_SCHEDULE = "SCHEDULE";

    public static final String SEND_AMOUNT_TYPE_UNIVERSAL = "UNIVERSAL";
    public static final String SEND_AMOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    /**
     * weather immediate or schedule
     */
    public static final String STATUS_IDLE = "IDLE";
    public static final String STATUS_COMPLETE = "COMPLETE";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    /**
     * BCS/API
     */
    @Column(name = "SEND_TYPE", columnDefinition = "varchar(50)")
    private String sendType;
    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
    private String modifyUser;
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;


    @Column(name = "TITLE", columnDefinition = "nvarchar(50)")
    private String title = "";
    @Column(name = "SERIAL_ID", columnDefinition = "nvarchar(50)")
    private String serialId = "";
    @Column(name = "PCC_CODE", columnDefinition = "nvarchar(50)")
    private String pccCode = "";
    @Column(name = "DEPARTMENT_FULL_NAME", columnDefinition = "nvarchar(200)")
    private String departmentFullName = "";
    /**
     * IMMEDIATE/SCHEDULE
     */
    @Column(name = "SEND_TIMING_TYPE", columnDefinition = "varchar(50)")
    private String sendTimingType = "";
    /**
     * IMMEDIATE/SCHEDULE
     */
    @Column(name = "SEND_USER", columnDefinition = "varchar(50)")
    private String sendUser = "";

    /**
     * only for sendTimingType = SCHEDULE
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "SEND_TIMING_TIME")
    private Date sendTimingTime;
    /**
     * INDIVIDUAL/UNIVERSAL
     */
    @Column(name = "SEND_AMOUNT_TYPE", columnDefinition = "varchar(50)")
    private String sendAmountType = "";
    /**
     * only for sendAmountType = UNIVERSAL
     */
    @Column(name = "AMOUNT")
    private Long amount = 0L;
    @Column(name = "DO_CHECK_FOLLOWAGE")
    private Boolean doCheckFollowage;
    @Column(name = "DO_APPEND_MESSAGE")
    private Boolean doAppendMessage;
    @Column(name = "ALLOW_TO_SEND")
    private Boolean allowToSend;
    @Column(name = "APPEND_MESSAGE_ID")
    private Long appendMessageId;
    @Column(name = "LINE_POINT_SEND_GROUP_ID")
    private Long linePointSendGroupId;

    @Column(name = "TOTAL_COUNT")
    private Long totalCount = 0L;
    @Column(name = "TOTAL_AMOUNT")
    private Long totalAmount = 0L;
    @Column(name = "SUCCESSFUL_COUNT")
    private Long successfulCount = 0L;
    @Column(name = "SUCCESSFUL_AMOUNT")
    private Long successfulAmount = 0L;
    @Column(name = "FAILED_COUNT")
    private Long failedCount = 0L;
    /**
     * press [Start] at LinePointList
     */
    @Column(name = "SEND_START_TIME")
    private Date sendStartTime;
    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status = "";
}
