package com.bcs.core.taishin.circle.pnp.db.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;

import com.bcs.core.taishin.circle.pnp.db.entity.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.EntityManagerProviderService;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpProcessFlowEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;

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
     * Pnp Statistical Report Summary List
     *
     * @return PnpStsRptSummary List
     */
    @SuppressWarnings("unchecked")
    public List<PnpStsRptSummary> getPnpStsRptSummaryList(@CurrentUser CustomUser customUser, final PnpStsRptParam pnpStsRptParam) {

        pnpStsRptParam.setRole(customUser.getRole());
        EntityManager entityManager = entityManagerProvider.getEntityManager();

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("getPNPStsRptSummary");

        Date startDate = pnpStsRptParam.getStartDate();
        String str_startDate = DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        Date endDate = pnpStsRptParam.getEndDate();
        String str_endDate = DataUtils.formatDateToString(endDate, "yyyy-MM-dd");

        query.setParameter("start_date",str_startDate);
        query.setParameter("end_date",str_endDate);
        query.setParameter("account", pnpStsRptParam.getAccount());
        query.setParameter("pcccode", pnpStsRptParam.getPccCode());

        log.info("start_date:" + str_startDate);
        log.info("end_date:" + str_endDate);
        log.info("account:" + pnpStsRptParam.getAccount());
        log.info("pcccode:" + pnpStsRptParam.getPccCode());

        List<PnpStsRptSummary> pnpStsRptSummaryList = query.getResultList();

        if (pnpStsRptSummaryList.isEmpty()) {
            log.info("pnpStsRptSummaryList List is Empty!!");
            return Collections.emptyList();
        } else
            log.info("pnpStsRptSummaryList is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpStsRptSummaryList));

        return pnpStsRptSummaryList;
    }

    /**
     * Pnp Statistical Report Detail List
     *
     * @return PnpStsRptDetail List
     */
    @SuppressWarnings("unchecked")
    public List<PnpStsRptDetail> getPnpStsRptDetailList(@CurrentUser CustomUser customUser, final PnpStsRptParam pnpStsRptParam) {

    	log.info("pnpStsRptParam = {}", pnpStsRptParam);
    	
        pnpStsRptParam.setRole(customUser.getRole());
        
        EntityManager entityManager = entityManagerProvider.getEntityManager();

        Date startDate = pnpStsRptParam.getStartDate();
        String str_startDate = DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        Date endDate = pnpStsRptParam.getEndDate();
        String str_endDate = DataUtils.formatDateToString(endDate, "yyyy-MM-dd");

        // === call sp way 2 ====
        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("getPNPStsRptDetail");
        query.setParameter("show_page", pnpStsRptParam.getPage());
        query.setParameter("page_count",Integer.valueOf(pnpStsRptParam.getPageCount()));
        query.setParameter("start_date",str_startDate);
        query.setParameter("end_date",str_endDate);
        query.setParameter("account", pnpStsRptParam.getAccount());
        query.setParameter("pcccode", pnpStsRptParam.getPccCode());

        log.info("show_page:" + pnpStsRptParam.getPage());
        log.info("page_count:" + pnpStsRptParam.getPageCount());
        log.info("start_date:" + str_startDate);
        log.info("end_date:" + str_endDate);
        log.info("account:" + pnpStsRptParam.getAccount());

/*
        // === call sp way 1 ====
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("usp_getPNPStsRptDetail");

        query.registerStoredProcedureParameter("show_page", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("page_count", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("start_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("end_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("account", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("pcccode", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("total_page", Integer.class, ParameterMode.OUT);
        query.setParameter("show_page", 1);
        query.setParameter("page_count",10);
        query.setParameter("start_date","null");
        query.setParameter("end_date","null");
        query.setParameter("account","null");
        query.setParameter("pcccode","null");
        query.execute();
*/
        query.execute();
        
//        int total_page = (Integer) query.getOutputParameterValue("total_page");
//        log.info("total_page: " + total_page);
        
        List<PnpStsRptDetail> pnpStsRptDetailList = query.getResultList();

        if (pnpStsRptDetailList.isEmpty()) {
            log.info("pnpStsRptDetailList List is Empty!!");
            return Collections.emptyList();
        } else
            log.info("pnpStsRptDetailList is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpStsRptDetailList));

        return pnpStsRptDetailList;
    }


    private StringBuilder getPnpStsRptSql(final PnpStsRptParam pnpStsRptParam) {
        StringBuilder sb = new StringBuilder();

        sb.append("EXEC usp_getPNPStsRptSummary");
        return sb;
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

        List<PnpDetailReport> reportFilterList = pnpDetailReportList.stream()
                .sorted(Comparator.comparing(PnpDetailReport::getCreateTime).reversed()).collect(Collectors.toList());

        pnpDetailReportList.clear();

        reportFilterList.forEach(report -> {
            report.setProcessFlow(englishProcFlowToChinese(report.getProcessFlow()));
            report.setFtpSource(englishSourceToChinese(report.getFtpSource()));
            report.setBcStatus(englishStatusToChinese(report.getBcStatus()));
            report.setPnpStatus(englishStatusToChinese(report.getPnpStatus()));
            report.setSmsStatus(englishStatusToChinese(report.getSmsStatus()));
        });
        log.info("Sorted Report List", DataUtils.toPrettyJsonUseJackson(reportFilterList));
        return reportFilterList;
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
                " TEMPLATE_ID," +
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
//                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PNP_DETAIL_ID as ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      CAST(FORMAT(CAST(REPLACE(REPLACE(REPLACE(REPLACE(D.DETAIL_SCHEDULE_TIME, '-', ''), '/' , ''), ' ', ''), ':', '') AS BIGINT),'####-##-## ##:##:##') AS DATETIME2(0)) AS SCHEDULE_TIME," +
                "      D.LINE_PUSH_TIME AS BC_TIME," +
                "      D.PNP_TIME AS PNP_TIME," +
                "      D.SMS_TIME AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.DEST_NAME AS SN," +
                "      D.FLEX_TEMPLATE_ID AS TEMPLATE_ID," +
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
                "      D.CREAT_TIME AS CREATE_TIME," +
                "      D.MODIFY_TIME AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_MITAKE D" +
                "    INNER JOIN BCS_PNP_MAIN_MITAKE AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
//                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PNP_DETAIL_ID as ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      CAST(FORMAT(CAST(REPLACE(REPLACE(REPLACE(REPLACE(D.DETAIL_SCHEDULE_TIME, '-', ''), '/' , ''), ' ', ''), ':', '') AS BIGINT),'####-##-## ##:##:##') AS DATETIME2(0)) AS SCHEDULE_TIME," +
                "      D.LINE_PUSH_TIME AS BC_TIME," +
                "      D.PNP_TIME AS PNP_TIME," +
                "      D.SMS_TIME AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID AS TEMPLATE_ID," +
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
                "      D.CREAT_TIME AS CREATE_TIME," +
                "      D.MODIFY_TIME AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_MING D" +
                "    INNER JOIN BCS_PNP_MAIN_MING AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
//                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PNP_DETAIL_ID as ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      CAST(FORMAT(CAST(REPLACE(REPLACE(REPLACE(REPLACE(D.DETAIL_SCHEDULE_TIME, '-', ''), '/' , ''), ' ', ''), ':', '') AS BIGINT),'####-##-## ##:##:##') AS DATETIME2(0)) AS SCHEDULE_TIME," +
                "      D.LINE_PUSH_TIME AS BC_TIME," +
                "      D.PNP_TIME AS PNP_TIME," +
                "      D.SMS_TIME AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID  AS TEMPLATE_ID," +
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
                "      D.CREAT_TIME AS CREATE_TIME," +
                "      D.MODIFY_TIME AS MODIFY_TIME" +
                "    FROM BCS_PNP_DETAIL_UNICA D" +
                "    INNER JOIN BCS_PNP_MAIN_UNICA AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID" +
                "    INNER JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID" +
                "  )" +
                "  UNION ALL" +
                "  (" +
                "    SELECT" +
//                "      CONCAT(D.PNP_MAIN_ID, '.', D.PNP_DETAIL_ID) AS ID," +
                "      D.PNP_DETAIL_ID as ID," +
                "      D.PROC_FLOW AS PROCESS_FLOW," +
                "      D.PROC_STAGE AS PROCESS_STAGE," +
                "      D.SOURCE AS FTP_SOURCE," +
                "      D.MSG AS MESSAGE," +
                "      CAST(FORMAT(CAST(REPLACE(REPLACE(REPLACE(REPLACE(D.DETAIL_SCHEDULE_TIME, '-', ''), '/' , ''), ' ', ''), ':', '') AS BIGINT),'####-##-## ##:##:##') AS DATETIME2(0)) AS SCHEDULE_TIME," +
                "      D.LINE_PUSH_TIME AS BC_TIME," +
                "      D.PNP_TIME AS PNP_TIME," +
                "      D.SMS_TIME AS SMS_TIME," +
                "      D.BC_STATUS AS BC_STATUS," +
                "      D.PNP_STATUS AS PNP_STATUS," +
                "      D.SMS_STATUS AS SMS_STATUS," +
                "      D.BC_HTTP_STATUS_CODE AS BC_HTTP_STATUS_CODE," +
                "      D.PNP_HTTP_STATUS_CODE AS PNP_HTTP_STATUS_CODE," +
                "      D.SN AS SN," +
                "      D.FLEX_TEMPLATE_ID AS TEMPLATE_ID," +
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
                "      D.CREAT_TIME AS CREATE_TIME," +
                "      D.MODIFY_TIME AS MODIFY_TIME" +
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
        final Date startDate = pnpDetailReportParam.getStartDate();
        final Date endDate = pnpDetailReportParam.getEndDate();
        final boolean isCreateTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.CREATE_TIME);
        final boolean isOrderTime = pnpDetailReportParam.getDateType().equals(PnpDetailReportParam.ORDER_TIME);

        /* 依照篩選條件過濾 */
        if (startDate != null && isCreateTime) {
            sb.append(String.format(" AND R1.CREATE_TIME >= '%s'", DataUtils.convDateToStr(DataUtils.truncDate(startDate), "yyyy-MM-dd HH:mm:ss")));
        }
        if (endDate != null && isCreateTime) {
            sb.append(String.format(" AND R1.CREATE_TIME <= '%s'", DataUtils.convDateToStr(DataUtils.truncEndDate(endDate), "yyyy-MM-dd HH:mm:ss")));
        }
        if (startDate != null && isOrderTime) {
            sb.append(String.format(" AND R1.SCHEDULE_TIME >= '%s'", DataUtils.convDateToStr(DataUtils.truncDate(startDate), "yyyy-MM-dd HH:mm:ss")));
        }
        if (endDate != null && isOrderTime) {
            sb.append(String.format(" AND R1.SCHEDULE_TIME <= '%s'", DataUtils.convDateToStr(DataUtils.truncEndDate(endDate), "yyyy-MM-dd HH:mm:ss")));
        }
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

    /**
     * Pnp Block Send Detail List
     *
     * @return PnpBlockSend List
     */
    @SuppressWarnings("unchecked")
    public List<PNPBlockSendList> qryPnpBlockSendList(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {

		log.info("2-1 pnpSendBlockParam.getPage() = {}", pnpSendBlockParam.getPage());
        log.info("2-2 pnpSendBlockParam.getPageCount() = {}", pnpSendBlockParam.getPageCount());
        log.info("2-3 pnpSendBlockParam.getStartDate() = {}", pnpSendBlockParam.getStartDate());
        log.info("2-4 pnpSendBlockParam.getEndDate() = {}", pnpSendBlockParam.getEndDate());
        log.info("2-5 pnpSendBlockParam.getMobile() = {}", pnpSendBlockParam.getMobile());
        log.info("2-6 pnpSendBlockParam.getInsertUser() = {}", pnpSendBlockParam.getInsertUser());
        log.info("2-7 pnpSendBlockParam.getGroupTag() = {}", pnpSendBlockParam.getGroupTag());
        log.info("2-8 pnpSendBlockParam.getModify_reason() = {}", pnpSendBlockParam.getModify_reason());

        pnpSendBlockParam.setRole(customUser.getRole());

        EntityManager entityManager = entityManagerProvider.getEntityManager();

        Date startDate = pnpSendBlockParam.getStartDate();
        String str_startDate = (startDate == null)? "" : DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        
        Date endDate = pnpSendBlockParam.getEndDate();
        String str_endDate = (startDate == null)? "" : DataUtils.formatDateToString(endDate, "yyyy-MM-dd");

        log.info("startDate = {}", startDate);
        log.info("endDate = {}", endDate);

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("qryPNPBlockSendList");
        query.setParameter("show_page", pnpSendBlockParam.getPage());
        query.setParameter("page_count",pnpSendBlockParam.getPageCount());
        query.setParameter("start_date",str_startDate);
        query.setParameter("end_date",str_endDate);
        query.setParameter("mobile",pnpSendBlockParam.getMobile());
        query.setParameter("insert_user",pnpSendBlockParam.getInsertUser());
        query.setParameter("group_tag",pnpSendBlockParam.getGroupTag());

        List<PNPBlockSendList> pnpBlockSendList = query.getResultList();

        if (pnpBlockSendList.isEmpty()) {
            log.info("pnpBlockSendList List is Empty!!");
            return Collections.emptyList();
        } else
            log.info("pnpBlockSendList is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpBlockSendList));

        return pnpBlockSendList;
    }

    /**
     * Pnp Block Send Count
     *
     * @return PnpBlockSendCount
     */
    @SuppressWarnings("unchecked")
    public long getPnpBlockSendCount(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {
        log.info("pnpSendBlockParam = {}", pnpSendBlockParam);
        
		log.debug("2-1 pnpSendBlockParam.getPage() = {}", pnpSendBlockParam.getPage());
        log.debug("2-2 pnpSendBlockParam.getPageCount() = {}", pnpSendBlockParam.getPageCount());
        log.debug("2-3 pnpSendBlockParam.getStartDate() = {}", pnpSendBlockParam.getStartDate());
        log.debug("2-4 pnpSendBlockParam.getEndDate() = {}", pnpSendBlockParam.getEndDate());
        log.debug("2-5 pnpSendBlockParam.getMobile() = {}", pnpSendBlockParam.getMobile());
        log.debug("2-6 pnpSendBlockParam.getInsertUser() = {}", pnpSendBlockParam.getInsertUser());
        log.debug("2-7 pnpSendBlockParam.getGroupTag() = {}", pnpSendBlockParam.getGroupTag());
        log.debug("2-8 pnpSendBlockParam.getModify_reason() = {}", pnpSendBlockParam.getModify_reason());

        log.debug("customUser.getRole() = {}", customUser.getRole());
        pnpSendBlockParam.setRole(customUser.getRole());

        Date startDate = pnpSendBlockParam.getStartDate();
        String str_startDate = (startDate == null)? "" : DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        
        Date endDate = pnpSendBlockParam.getEndDate();
        String str_endDate = (startDate == null)? "" : DataUtils.formatDateToString(endDate, "yyyy-MM-dd");
        
        log.debug("startDate = {}", startDate);
        log.debug("endDate = {}", endDate);

        EntityManager entityManager = entityManagerProvider.getEntityManager();

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("getPNPBlockSendCount");
        query.setParameter("start_date", str_startDate);
        query.setParameter("end_date", str_endDate);
        query.setParameter("mobile", pnpSendBlockParam.getMobile());
        query.setParameter("insert_user", pnpSendBlockParam.getInsertUser());
        query.setParameter("group_tag", pnpSendBlockParam.getGroupTag());

        List<PNPBlockSendCount> pnpBlockSendCount = query.getResultList();
        log.info("pnpBlockSendCount = {}", pnpBlockSendCount);

        if (pnpBlockSendCount.isEmpty()) {
            log.info("getPNPBlockSendCount List is Empty!!");
            return 0;
        } else
            log.info("getPNPBlockSendCount List is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpBlockSendCount));

        return pnpBlockSendCount.get(0).getCount();
    }

    /**
     * Pnp Block History Detail List
     *
     * @return PnpBlockHistory List
     */
    @SuppressWarnings("unchecked")
    public List<PNPBlockSendList> qryPnpBlockHistoryList(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {

        log.info("pnpSendBlockParam = {}", pnpSendBlockParam);

        pnpSendBlockParam.setRole(customUser.getRole());

        EntityManager entityManager = entityManagerProvider.getEntityManager();

        Date startDate = pnpSendBlockParam.getStartDate();
        String str_startDate = DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        Date endDate = pnpSendBlockParam.getEndDate();
        String str_endDate = DataUtils.formatDateToString(endDate, "yyyy-MM-dd");

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("qryPNPBlockSendList");
        query.setParameter("show_page", pnpSendBlockParam.getPage());
        query.setParameter("page_count",pnpSendBlockParam.getPageCount());
        query.setParameter("start_date",str_startDate);
        query.setParameter("end_date",str_endDate);
        query.setParameter("mobile",pnpSendBlockParam.getMobile());
        query.setParameter("insert_user",pnpSendBlockParam.getInsertUser());
        query.setParameter("group_tag",pnpSendBlockParam.getGroupTag());
        query.setParameter("block_enable",pnpSendBlockParam.getBlockEnable());

        List<PNPBlockSendList> pnpBlockSendList = query.getResultList();

        if (pnpBlockSendList.isEmpty()) {
            log.info("pnpBlockHistoryList List is Empty!!");
            return Collections.emptyList();
        } else
            log.info("pnpBlockHistoryList is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpBlockSendList));

        return pnpBlockSendList;
    }

    /**
     * Pnp Bolck History Detail List
     *
     * @return PnpBlockHistoryCount
     */
    @SuppressWarnings("unchecked")
    public long getPnpBlockHistoryCount(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {

        log.info("pnpSendBlockParam = {}", pnpSendBlockParam);

        pnpSendBlockParam.setRole(customUser.getRole());

        EntityManager entityManager = entityManagerProvider.getEntityManager();

        Date startDate = pnpSendBlockParam.getStartDate();
        String str_startDate = DataUtils.formatDateToString(startDate, "yyyy-MM-dd");
        Date endDate = pnpSendBlockParam.getEndDate();
        String str_endDate = DataUtils.formatDateToString(endDate, "yyyy-MM-dd");

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("getPNPBlockSendCount");
        query.setParameter("start_date",str_startDate);
        query.setParameter("end_date",str_endDate);
        query.setParameter("mobile",pnpSendBlockParam.getMobile());
        query.setParameter("insert_user",pnpSendBlockParam.getInsertUser());
        query.setParameter("group_tag",pnpSendBlockParam.getGroupTag());
        query.setParameter("block_enable",pnpSendBlockParam.getBlockEnable());

        List<PNPBlockSendCount> pnpBlockSendCount = query.getResultList();

        if (pnpBlockSendCount.isEmpty()) {
            log.info("getPNPBlockHistoryCount List is Empty!!");
            return 0;
        } else
            log.info("getPNPBlockHistoryCount List is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpBlockSendCount));

        return pnpBlockSendCount.get(0).getCount();
    }

    /**
     * Update Pnp BlockSend Table and History Table
     *
     * @return PnpBlockHistory Ref_Id
     */
    @SuppressWarnings("unchecked")
    public long updPnpBlockSend(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {

        log.info("pnpSendBlockParam = {}", pnpSendBlockParam);
        
        log.info("2-1 pnpSendBlockParam.getMobile() = {}", pnpSendBlockParam.getMobile());
        log.info("2-2 pnpSendBlockParam.getInsertUser() = {}", pnpSendBlockParam.getInsertUser());
        log.info("2-3 pnpSendBlockParam.getInsertDate() = {}", pnpSendBlockParam.getInsertDate());
        log.info("2-4 pnpSendBlockParam.getInsertTime() = {}", pnpSendBlockParam.getInsertTime());
        log.info("2-5 pnpSendBlockParam.getModify_reason() = {}", pnpSendBlockParam.getModify_reason());
        log.info("2-6 pnpSendBlockParam.getBlockEnable() = {}", pnpSendBlockParam.getBlockEnable());
        log.info("2-7 pnpSendBlockParam.getGroupTag() = {}", pnpSendBlockParam.getGroupTag());

        pnpSendBlockParam.setRole(customUser.getRole());

        EntityManager entityManager = entityManagerProvider.getEntityManager();

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("updatePNPBlockSend");
        query.setParameter("mobile", pnpSendBlockParam.getMobile());
        query.setParameter("block_enable",pnpSendBlockParam.getBlockEnable());
        query.setParameter("insert_user",pnpSendBlockParam.getInsertUser());
        query.setParameter("group_tag",pnpSendBlockParam.getGroupTag());
        query.setParameter("insert_date",pnpSendBlockParam.getInsertDate());
        query.setParameter("insert_time",pnpSendBlockParam.getInsertTime());
        query.setParameter("modify_reason",pnpSendBlockParam.getModify_reason());

        List<PNPUpdateBlockSend> pnpUpdateBlockSend = query.getResultList();

        if (pnpUpdateBlockSend.isEmpty()) {
            log.info("updPnpBlockSend List is Empty!!");
            return 0;
        } else
            log.info("updPnpBlockSend is not emtpy:" + DataUtils.toPrettyJsonUseJackson(pnpUpdateBlockSend));

        return pnpUpdateBlockSend.get(0).getHistoryRefId();
    }

    /**
    *
    * Query PNP BLOCK HISTORY*
    *
    * @return GROUP_TAG LIST
    * */

    @SuppressWarnings("unchecked")
    public List<PNPBlockGTag> qryPNPBlockGTagList(@CurrentUser CustomUser customUser, final PnpSendBlockParam pnpSendBlockParam) {
        log.info("pnpSendBlockParam = {}", pnpSendBlockParam);
        
        log.info("2-1 pnpSendBlockParam.getInActive() = {}", pnpSendBlockParam.getInActive());
    	
    	EntityManager entityManager = entityManagerProvider.getEntityManager();

        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("qryPNPBlockGTag");
        query.setParameter("in_active", pnpSendBlockParam.getInActive());

        List<PNPBlockGTag> pnpBlockGTagList = query.getResultList();

        return pnpBlockGTagList;

    }





}
