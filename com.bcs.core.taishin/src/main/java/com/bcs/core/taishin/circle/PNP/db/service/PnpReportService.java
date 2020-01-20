package com.bcs.core.taishin.circle.PNP.db.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.bcs.core.db.service.EntityManagerProviderService;
import com.bcs.core.taishin.circle.PNP.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpProcessFlowEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailReport;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailReportParam;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataUtils;

import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Pnp Report Service
 *
 * @author Alan Chen
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpReportService {

    private OracleService oracleService;
//    @PersistenceContext
//    private EntityManager entityManager;

    @Resource
    private EntityManagerProviderService entityManagerProvider;

    @Autowired
    public PnpReportService(OracleService oracleService) {
        this.oracleService = oracleService;
    }

    /**
     * Pnp Detail Report
     *
     * @param pnpDetailReportParam pnpDetailReportParam
     * @return PnpDetailReport List
     */
    @SuppressWarnings("unchecked")
    public List<PnpDetailReport> getPnpDetailReportList(@CurrentUser CustomUser customUser, final PnpDetailReportParam pnpDetailReportParam) {
        pnpDetailReportParam.setRole(customUser.getRole());
        EntityManager entityManager = entityManagerProvider.getEntityManager();
        Query query = entityManager.createNativeQuery(getDetailReportSql(pnpDetailReportParam).toString(), PnpDetailReport.class);
        List<PnpDetailReport> pnpDetailReportList = query.getResultList();
        log.info(DataUtils.toPrettyJsonUseJackson(pnpDetailReportList));
        if (pnpDetailReportList.isEmpty()) {
            log.info("List is Empty!!");
            return Collections.emptyList();
        }


        final boolean isCreateTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.CREATE_TIME);
        final boolean isOrderTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.ORDER_TIME);
        List<PnpDetailReport> reportFilterList;
        if (isOrderTime) {
            reportFilterList = pnpDetailReportList.stream()
                    .filter(report -> DataUtils.isBetween(
                            DataUtils.convStrToDate(report.getScheduleTime(), "yyyyMMddHHmmss"),
                            DataUtils.truncDate(pnpDetailReportParam.getStartDate()),
                            DataUtils.truncEndDate(pnpDetailReportParam.getEndDate()))
                    ).collect(Collectors.toList());
        } else if (isCreateTime){
            reportFilterList = pnpDetailReportList.stream()
                    .filter(report -> DataUtils.isBetween(
                            DataUtils.convStrToDate(report.getScheduleTime(), "yyyyMMddHHmmss"),
                            DataUtils.truncDate(pnpDetailReportParam.getStartDate()),
                            DataUtils.truncEndDate(pnpDetailReportParam.getEndDate()))
                    ).collect(Collectors.toList());
        } else {
             reportFilterList = new ArrayList<>();
             Collections.copy(reportFilterList, pnpDetailReportList);
        }

        pnpDetailReportList.clear();

        List<PnpDetailReport> reportFilterList2 = reportFilterList.stream()
                .sorted(Comparator.comparing((PnpDetailReport report) ->
                        DataUtils.convStrToDate(report.getCreateTime(), "yyyy-MM-dd HH:mm:ss")
                ).reversed()).collect(Collectors.toList());

        reportFilterList.clear();

        reportFilterList2.forEach(report -> {
            report.setProcessFlow(englishProcFlowToChinese(report.getProcessFlow()));
            report.setFtpSource(englishSourceToChinese(report.getFtpSource()));
            report.setBcStatus(englishStatusToChinese(report.getBcStatus()));
            report.setPnpStatus(englishStatusToChinese(report.getPnpStatus()));
            report.setSmsStatus(englishStatusToChinese(report.getSmsStatus()));
        });
        log.info("Sorted Report List", DataUtils.toPrettyJsonUseJackson(reportFilterList2));
        return reportFilterList2;
    }

    private StringBuilder getDetailReportSql(final PnpDetailReportParam pnpDetailReportParam) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT" +
                " ID," +
                " PROCESS_FLOW," +
                " PROCESS_STAGE," +
                " FTP_SOURCE," +
                " MESSAGE," +
                " SCHEDULE_TIME," +
                " BC_TIME," +
                " PNP_TIME," +
                " SMS_TIME," +
                " BC_STATUS," +
                " PNP_STATUS," +
                " SMS_STATUS," +
                " BC_HTTP_STATUS_CODE," +
                " PNP_HTTP_STATUS_CODE," +
                " SN," +
                " TEMPLATE," +
                " MAIN_ID," +
                " DETAIL_ID," +
                " MESSAGE_POINT," +
                " CAMPAIGN_ID," +
                " SEGMENT_ID," +
                " PROGRAM_ID," +
                " PID," +
                " IS_INTERNATIONAL," +
                " UID," +
                " PHONE," +
                " DIVISION_NAME," +
                " DEPARTMENT_NAME," +
                " GROUP_NAME," +
                " PCC_CODE," +
                " ACCOUNT," +
                " SOURCE_SYSTEM," +
                " EMPLOYEE_ID," +
                " CREATE_TIME," +
                " MODIFY_TIME" +
                " FROM (" +
                "  (" +
                "    SELECT" +
                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      D.DETAIL_SCHEDULE_TIME AS SCHEDULE_TIME," +
                "      CONVERT(VARCHAR, D.LINE_PUSH_TIME, 120) AS BC_TIME," +
                "      CONVERT(VARCHAR, D.PNP_TIME, 120) AS PNP_TIME," +
                "      CONVERT(VARCHAR, D.SMS_TIME, 120) AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.DEST_NAME AS SN," +
                "      D.FLEX_TEMPLATE_ID AS TEMPLATE," +
                "      CONVERT(VARCHAR, D.PNP_MAIN_ID) AS MAIN_ID," +
                "      CONVERT(VARCHAR, D.PNP_DETAIL_ID) AS DETAIL_ID," +
                "      1 AS MESSAGE_POINT," +
                "      NULL AS CAMPAIGN_ID," +
                "      NULL AS SEGMENT_ID," +
                "      NULL AS PROGRAM_ID," +
                "      NULL AS PID," +
                "      NULL AS IS_INTERNATIONAL," +
                "      D.UID AS UID," +
                "      D.PHONE AS PHONE," +
                "      D.DIVISION_NAME AS DIVISION_NAME," +
                "      D.DEPARTMENT_NAME AS DEPARTMENT_NAME," +
                "      D.GROUP_NAME AS GROUP_NAME," +
                "      A.PCC_CODE AS PCC_CODE," +
                "      A.ACCOUNT AS ACCOUNT," +
                "      A.SOURCE_SYSTEM AS SOURCE_SYSTEM," +
                "      A.EMPLOYEE_ID AS EMPLOYEE_ID," +
                "      CONVERT(VARCHAR, D.CREAT_TIME, 120) AS CREATE_TIME," +
                "      CONVERT(VARCHAR, D.MODIFY_TIME, 120) AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_MITAKE D" +
                "    INNER JOIN BCS_PNP_MAIN_MITAKE AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      D.DETAIL_SCHEDULE_TIME AS SCHEDULE_TIME," +
                "      CONVERT(VARCHAR, D.LINE_PUSH_TIME, 120) AS BC_TIME," +
                "      CONVERT(VARCHAR, D.PNP_TIME, 120) AS PNP_TIME," +
                "      CONVERT(VARCHAR, D.SMS_TIME, 120) AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID AS TEMPLATE," +
                "      CONVERT(VARCHAR, D.PNP_MAIN_ID) AS MAIN_ID," +
                "      CONVERT(VARCHAR, D.PNP_DETAIL_ID) AS DETAIL_ID," +
                "      1 AS MESSAGE_POINT," +
                "      NULL AS CAMPAIGN_ID," +
                "      NULL AS SEGMENT_ID," +
                "      NULL AS PROGRAM_ID," +
                "      NULL AS PID," +
                "      NULL AS IS_INTERNATIONAL," +
                "      D.UID AS UID," +
                "      D.PHONE AS PHONE," +
                "      D.DIVISION_NAME AS DIVISION_NAME," +
                "      D.DEPARTMENT_NAME AS DEPARTMENT_NAME," +
                "      D.GROUP_NAME AS GROUP_NAME," +
                "      A.PCC_CODE AS PCC_CODE," +
                "      A.ACCOUNT AS ACCOUNT," +
                "      A.SOURCE_SYSTEM AS SOURCE_SYSTEM," +
                "      A.EMPLOYEE_ID AS EMPLOYEE_ID," +
                "      CONVERT(VARCHAR, D.CREAT_TIME, 120) AS CREATE_TIME," +
                "      CONVERT(VARCHAR, D.MODIFY_TIME, 120) AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_MING D" +
                "    INNER JOIN BCS_PNP_MAIN_MING AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      D.DETAIL_SCHEDULE_TIME AS SCHEDULE_TIME," +
                "      CONVERT(VARCHAR, D.LINE_PUSH_TIME, 120) AS BC_TIME," +
                "      CONVERT(VARCHAR, D.PNP_TIME, 120) AS PNP_TIME," +
                "      CONVERT(VARCHAR, D.SMS_TIME, 120) AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID  AS TEMPLATE," +
                "      CONVERT(VARCHAR, D.PNP_MAIN_ID) AS MAIN_ID," +
                "      CONVERT(VARCHAR, D.PNP_DETAIL_ID) AS DETAIL_ID," +
                "      1 AS MESSAGE_POINT," +
                "      D.CAMPAIGN_ID AS CAMPAIGN_ID," +
                "      D.SEGMENT_ID AS SEGMENT_ID," +
                "      D.PROGRAM_ID AS PROGRAM_ID," +
                "      D.PID AS PID," +
                "      NULL AS IS_INTERNATIONAL," +
                "      D.UID AS UID," +
                "      D.PHONE AS PHONE," +
                "      D.DIVISION_NAME AS DIVISION_NAME," +
                "      D.DEPARTMENT_NAME AS DEPARTMENT_NAME," +
                "      D.GROUP_NAME AS GROUP_NAME," +
                "      A.PCC_CODE AS PCC_CODE," +
                "      A.ACCOUNT AS ACCOUNT," +
                "      A.SOURCE_SYSTEM AS SOURCE_SYSTEM," +
                "      A.EMPLOYEE_ID AS EMPLOYEE_ID," +
                "      CONVERT(VARCHAR, D.CREAT_TIME, 120) AS CREATE_TIME," +
                "      CONVERT(VARCHAR, D.MODIFY_TIME, 120) AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_UNICA D" +
                "    INNER JOIN BCS_PNP_MAIN_UNICA AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      D.DETAIL_SCHEDULE_TIME AS SCHEDULE_TIME," +
                "      CONVERT(VARCHAR, D.LINE_PUSH_TIME, 120) AS BC_TIME," +
                "      CONVERT(VARCHAR, D.PNP_TIME, 120) AS PNP_TIME," +
                "      CONVERT(VARCHAR, D.SMS_TIME, 120) AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID  AS TEMPLATE," +
                "      CONVERT(VARCHAR, D.PNP_MAIN_ID) AS MAIN_ID," +
                "      CONVERT(VARCHAR, D.PNP_DETAIL_ID) AS DETAIL_ID," +
                "      1 AS MESSAGE_POINT," +
                "      D.CAMPAIGN_ID AS CAMPAIGN_ID," +
                "      D.SEGMENT_ID AS SEGMENT_ID," +
                "      D.PROGRAM_ID AS PROGRAM_ID," +
                "      D.PID AS PID," +
                "      NULL AS IS_INTERNATIONAL," +
                "      D.UID AS UID," +
                "      D.PHONE AS PHONE," +
                "      D.DIVISION_NAME AS DIVISION_NAME," +
                "      D.DEPARTMENT_NAME AS DEPARTMENT_NAME," +
                "      D.GROUP_NAME AS GROUP_NAME," +
                "      A.PCC_CODE AS PCC_CODE," +
                "      A.ACCOUNT AS ACCOUNT," +
                "      A.SOURCE_SYSTEM AS SOURCE_SYSTEM," +
                "      A.EMPLOYEE_ID AS EMPLOYEE_ID," +
                "      CONVERT(VARCHAR, D.CREAT_TIME, 120) AS CREATE_TIME," +
                "      CONVERT(VARCHAR, D.MODIFY_TIME, 120) AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_EVERY8D D" +
                "    INNER JOIN BCS_PNP_MAIN_EVERY8D AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                ") AS R1 WHERE 1=1 ");

        final String account = pnpDetailReportParam.getAccount().trim();
        final String pccCode = pnpDetailReportParam.getPccCode().trim();
        final String sourceSystem = pnpDetailReportParam.getSourceSystem().trim();
        final String phoneNumber = pnpDetailReportParam.getPhone().trim();
        final String employeeId = pnpDetailReportParam.getEmployeeId().trim();
//        final Date startDate = pnpDetailReportParam.getStartDate();
//        final Date endDate = pnpDetailReportParam.getEndDate();
        final boolean isCreateTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.CREATE_TIME);
        final boolean isOrderTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.ORDER_TIME);

        /* 依照篩選條件過濾 */
//        if (startDate != null && isCreateTime) {
//            sb.append(String.format(" AND R1.CREATE_TIME >= '%s'", DataUtils.convDateToStr(DataUtils.truncDate(startDate), "yyyy-MM-dd HH:mm:ss")));
//        }
//        if (endDate != null && isCreateTime) {
//            sb.append(String.format(" AND R1.CREATE_TIME <= '%s'", DataUtils.convDateToStr(DataUtils.truncEndDate(endDate), "yyyy-MM-dd HH:mm:ss")));
//        }
//        if (startDate != null && isOrderTime) {
//            sb.append(String.format(" AND R1.SCHEDULE_TIME >= '%s'", DataUtils.convDateToStr(DataUtils.truncDate(startDate), "yyyy-MM-dd HH:mm:ss")));
//        }
//        if (endDate != null && isOrderTime) {
//            sb.append(String.format(" AND R1.SCHEDULE_TIME <= '%s'", DataUtils.convDateToStr(DataUtils.truncEndDate(endDate), "yyyy-MM-dd HH:mm:ss")));
//        }
        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" AND R1.ACCOUNT = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" AND R1.PCC_CODE = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" AND R1.SOURCE_SYSTEM = '%s' ", sourceSystem));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            sb.append(String.format(" AND R1.PHONE = '%s'", phoneNumber));
        }

        /* 檢查權限 */
        sb.append(oracleService.getAvailableEmpIdsByEmpId(employeeId, pnpDetailReportParam.getRole()));

        /* 依照建立時間反序 */
        if (isCreateTime) {
            sb.append(" ORDER BY R1.CREATE_TIME DESC ");
        }
        if (isOrderTime) {
            sb.append(" ORDER BY R1.SCHEDULE_TIME DESC ");
        }

        /* 是否分頁 */
        if (pnpDetailReportParam.isPageable()) {
            final Integer page = pnpDetailReportParam.getPage();
            final int[] pageRowArray = DataUtils.pageRowCalculate(page, 10);
            sb.append(String.format(" OFFSET %s ROWS ", pageRowArray[0] - 1));
            sb.append(String.format(" FETCH NEXT %s ROWS ONLY", 10));
        }

        log.info("str1: " + DataUtils.replaceUnnecessarySpace(sb.toString()));
        return sb;
    }

    @Cacheable
    private String englishStatusToChinese(final String status) {
        PnpStatusEnum statusEnum = PnpStatusEnum.findEnumByName(status);
        if (statusEnum == null) {
            return status;
        }
        return statusEnum.chinese;
    }

    /**
     * Transfer Source Code To Chinese Name
     *
     * @param sourceCode Source Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    @Cacheable
    private String englishSourceToChinese(final String sourceCode) {
        PnpFtpSourceEnum ftpSourceEnum = PnpFtpSourceEnum.findEnumByCode(sourceCode);
        if (ftpSourceEnum == null) {
            return sourceCode;
        }
        return ftpSourceEnum.chinese;
    }


    /**
     * Transfer procFlow Code To Chinese Name
     *
     * @param procFlowCode procFlow Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    @Cacheable
    private String englishProcFlowToChinese(final String procFlowCode) {
        PnpProcessFlowEnum processFlowEnum = PnpProcessFlowEnum.findEnumByCode(procFlowCode);
        if (processFlowEnum == null) {
            return procFlowCode;
        }
        return processFlowEnum.uiText;
    }
}
