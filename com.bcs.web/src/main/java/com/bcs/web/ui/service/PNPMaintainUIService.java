package com.bcs.web.ui.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PNPMaintainUIService {

    private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository;
    private PNPMaintainAccountModelCustom pnpMaintainAccountModelCustom;
    private OracleService oraclePnpService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PNPMaintainUIService(PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository,
                                PNPMaintainAccountModelCustom pnpMaintainAccountModelCustom,
                                OracleService oraclePnpService) {
        this.pnpMaintainAccountModelRepository = pnpMaintainAccountModelRepository;
        this.pnpMaintainAccountModelCustom = pnpMaintainAccountModelCustom;
        this.oraclePnpService = oraclePnpService;

    }

    public void save(PNPMaintainAccountModel pnpMaintainAccountModel) {
        pnpMaintainAccountModelRepository.save(pnpMaintainAccountModel);
    }

    public void delete(PNPMaintainAccountModel pnpMaintainAccountModel) {
        pnpMaintainAccountModelRepository.delete(pnpMaintainAccountModel);
    }

    public PNPMaintainAccountModel findOne(Long id) {
        return pnpMaintainAccountModelRepository.findOne(id);
    }

    public List<PNPMaintainAccountModel> findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType) {
        return pnpMaintainAccountModelRepository.findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
                divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
    }

    public List<PNPMaintainAccountModel> queryUsePageCoditions(
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType, Boolean status) {
        return pnpMaintainAccountModelCustom.queryUseConditions(divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
    }

    public List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent) {
        return pnpMaintainAccountModelRepository.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
    }

    public List<PNPMaintainAccountModel> findByAccountType(String accountType) {
        return pnpMaintainAccountModelRepository.findByAccountType(accountType);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getPNPDetailReport(String startDate,
                                                        String endDate,
                                                        String account,
                                                        String pccCode,
                                                        String sourceSystem,
                                                        Integer page,
                                                        String empId,
                                                        String phoneNumber) {
        try {
            int rowStart;
            int rowEnd;
            if (page == null) {
                rowStart = 1;
                /* Equal Get all data */
                rowEnd = Integer.MAX_VALUE;
            } else {
                // 1~199 => 0~198
                page--;
                rowStart = page * 10 + 1;
                // 10 as Size
                rowEnd = rowStart + 10;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("select * from ( " +
                    "        select " +
                    "                orig_file_name, " +
                    "                proc_flow, " +
                    "                proc_stage, " +
                    "                msg, " +
                    "                phone, " +
                    "                detail_schedule_time1, " +
                    "                detail_schedule_time2, " +
                    "                bc_time, " +
                    "                pnp_time, " +
                    "                status1, " +
                    "                status2, " +
                    "                status3, " +
                    "                pcc_code, " +
                    "                account, " +
                    "                source_system, " +
                    "                employee_id, " +
                    "                creat_time, " +
                    "                row_number() over ( " +
                    "        order by " +
                    "                creat_time desc) as rownum " +
                    "        from ( " +
                    "        ( " +
                    "                select " +
                    "                        m.orig_file_name, " +
                    "                        d.proc_flow, " +
                    "                        d.proc_stage, " +
                    "                        msg, " +
                    "                        phone, " +
                    "                        d.detail_schedule_time as detail_schedule_time1, " +
                    "                        d.detail_schedule_time as detail_schedule_time2, " +
                    "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                    "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                    "                        d.bc_status as status1, " +
                    "                        d.pnp_status as status2, " +
                    "                        d.sms_status as status3, " +
                    "                        a.pcc_code, " +
                    "                        a.account, " +
                    "                        a.source_system, " +
                    "                        a.employee_id, " +
                    "                        d.creat_time " +
                    "                from " +
                    "                        bcs_pnp_detail_ming as d " +
                    "                left join bcs_pnp_main_ming as m on d.pnp_main_id = m.pnp_main_id " +
                    "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                    "                where d.creat_time >= ?1 and d.creat_time <  dateadd(day, 1, ?2) ");

            if (StringUtils.isNotBlank(account)) {
                sb.append(String.format(" and account = '%s'", account));
            }
            if (StringUtils.isNotBlank(pccCode)) {
                sb.append(String.format(" and pcc_code = '%s' ", pccCode));
            }
            if (StringUtils.isNotBlank(sourceSystem)) {
                sb.append(String.format(" and source_system = '%s' ", sourceSystem));
            }
            if (StringUtils.isNotBlank(phoneNumber)) {
                sb.append(String.format(" and phone = '%s'", phoneNumber));
            }

            sb.append(
                    "        ) " +
                            "        union all " +
                            "        ( " +
                            "               select " +
                            "                        m.orig_file_name, " +
                            "                        d.proc_flow, " +
                            "                        d.proc_stage, " +
                            "                        msg, " +
                            "                        phone, " +
                            "                        null as detail_schedule_time1, " +
                            "                        null as detail_schedule_time2, " +
                            "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                            "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                            "                        d.bc_status as status1, " +
                            "                        d.pnp_status as status2, " +
                            "                        d.sms_status as status3, " +
                            "                        a.pcc_code, " +
                            "                        a.account, " +
                            "                        a.source_system, " +
                            "                        a.employee_id, " +
                            "                        d.creat_time " +
                            "                from " +
                            "                        bcs_pnp_detail_mitake as d " +
                            "                left join bcs_pnp_main_mitake as m on d.pnp_main_id = m.pnp_main_id " +
                            "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                            "                where d.creat_time >= ?3 and d.creat_time <  dateadd(day, 1, ?4) ");

            if (StringUtils.isNotBlank(account)) {
                sb.append(String.format(" and account = '%s'", account));
            }
            if (StringUtils.isNotBlank(pccCode)) {
                sb.append(String.format(" and pcc_code = '%s' ", pccCode));
            }
            if (StringUtils.isNotBlank(sourceSystem)) {
                sb.append(String.format(" and source_system = '%s' ", sourceSystem));
            }
            if (StringUtils.isNotBlank(phoneNumber)) {
                sb.append(String.format(" and phone = '%s'", phoneNumber));
            }

            sb.append(
                    "        ) " +
                            "        union all " +
                            "        ( " +
                            "                select " +
                            "                        m.orig_file_name, " +
                            "                        d.proc_flow, " +
                            "                        d.proc_stage, " +
                            "                        msg, " +
                            "                        phone, " +
                            "                        null as detail_schedule_time1, " +
                            "                        null as detail_schedule_time2, " +
                            "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                            "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                            "                        d.bc_status as status1, " +
                            "                        d.pnp_status as status2, " +
                            "                        d.sms_status as status3, " +
                            "                        a.pcc_code, " +
                            "                        a.account, " +
                            "                        a.source_system, " +
                            "                        a.employee_id, " +
                            "                        d.creat_time " +
                            "                from " +
                            "                        bcs_pnp_detail_unica as d " +
                            "                left join bcs_pnp_main_unica as m on d.pnp_main_id = m.pnp_main_id " +
                            "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                            "                where d.creat_time >= ?5 and d.creat_time <  dateadd(day, 1, ?6) ");

            if (StringUtils.isNotBlank(account)) {
                sb.append(String.format(" and account = '%s'", account));
            }
            if (StringUtils.isNotBlank(pccCode)) {
                sb.append(String.format(" and pcc_code = '%s' ", pccCode));
            }
            if (StringUtils.isNotBlank(sourceSystem)) {
                sb.append(String.format(" and source_system = '%s' ", sourceSystem));
            }
            if (StringUtils.isNotBlank(phoneNumber)) {
                sb.append(String.format(" and phone = '%s'", phoneNumber));
            }

            sb.append(
                    "        ) " +
                            "        union all " +
                            "        ( " +
                            "                select " +
                            "                        m.orig_file_name, " +
                            "                        d.proc_flow, " +
                            "                        d.proc_stage, " +
                            "                        msg, " +
                            "                        phone, " +
                            "                        null as detail_schedule_time1, " +
                            "                        null as detail_schedule_time2, " +
                            "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                            "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                            "                        d.bc_status as status1, " +
                            "                        d.pnp_status as status2, " +
                            "                        d.sms_status as status3, " +
                            "                        a.pcc_code, " +
                            "                        a.account, " +
                            "                        a.source_system, " +
                            "                        a.employee_id, " +
                            "                        d.creat_time " +
                            "                from " +
                            "                        bcs_pnp_detail_every8d as d " +
                            "                left join bcs_pnp_main_every8d as m on d.pnp_main_id = m.pnp_main_id " +
                            "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                            "                where d.creat_time >= ?7 and d.creat_time <  dateadd(day, 1, ?8) ");

            if (StringUtils.isNotBlank(account)) {
                sb.append(String.format(" and account = '%s'", account));
            }
            if (StringUtils.isNotBlank(pccCode)) {
                sb.append(String.format(" and pcc_code = '%s' ", pccCode));
            }
            if (StringUtils.isNotBlank(sourceSystem)) {
                sb.append(String.format(" and source_system = '%s' ", sourceSystem));
            }
            if (StringUtils.isNotBlank(phoneNumber)) {
                sb.append(String.format(" and phone = '%s'", phoneNumber));
            }

            sb.append("        ) " +
                    " ) as r1");

            boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
            log.info("oracleUseDepartmentCheck:" + oracleUseDepartmentCheck);
            if (oracleUseDepartmentCheck) {
                String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
                if (StringUtils.isNotBlank(empAva)) {
                    sb.append(empAva);
                }
            }

            sb.append(" ) as r2 where rownum >= ?9 and rownum < ?10 ");

            log.info("str1: " + sb.toString());
            log.info("rowStart:" + rowStart);
            log.info("rowEnd:" + rowEnd);
            Query query = entityManager.createNativeQuery(sb.toString())
                    .setParameter(1, startDate)
                    .setParameter(2, endDate)
                    .setParameter(3, startDate)
                    .setParameter(4, endDate)
                    .setParameter(5, startDate)
                    .setParameter(6, endDate)
                    .setParameter(7, startDate)
                    .setParameter(8, endDate)
                    .setParameter(9, rowStart)
                    .setParameter(10, rowEnd);

            List<Object[]> list = query.getResultList();

            Map<String, List<String>> map = new LinkedHashMap<>();

            int count = 0;
            for (Object[] objectArray : list) {
                count++;
                log.info("c:" + count);
                List<String> dataList = new ArrayList<>();
                map.put(Integer.toString(count), dataList);
                for (int i = 0, max = 14; i < max; i++) {
                    if (objectArray[i] == null) {
                        dataList.add("");
                    } else {
                        dataList.add(objectArray[i].toString());
                    }
                }
            }
            DataUtils.toPrettyJsonUseJackson(map);
            return map;
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public int getPNPDetailReportTotalPages(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId, String phoneNumber) {
//        String queryString =
//                "SELECT COUNT(*) FROM ( "
//                        + "SELECT ORIG_FILE_NAME, PROC_FLOW, SOURCE, MSG, PHONE, PNP_DELIVERY_EXPIRE_TIME, DETAIL_SCHEDULE_TIME, CREAT_TIME, STATUS, PCC_CODE, ACCOUNT, SOURCE_SYSTEM, EMPLOYEE_ID, "
//                        + "ROW_NUMBER() OVER ( ORDER BY CREAT_TIME desc) AS RowNum "
//                        + "FROM ( "
//                        + "SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, D.DETAIL_SCHEDULE_TIME, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
//                        + "FROM BCS_PNP_DETAIL_MING AS D "
//                        + "LEFT JOIN BCS_PNP_MAIN_MING AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
//                        + "LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
//                        + "UNION All "
//                        + "SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
//                        + "FROM BCS_PNP_DETAIL_MITAKE AS D "
//                        + "LEFT JOIN BCS_PNP_MAIN_MITAKE AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
//                        + "LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
//                        + "UNION All "
//                        + "SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
//                        + "FROM BCS_PNP_DETAIL_UNICA AS D "
//                        + "LEFT JOIN BCS_PNP_MAIN_UNICA AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
//                        + "LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
//                        + "UNION All "
//                        + "SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
//                        + "FROM BCS_PNP_DETAIL_EVERY8D AS D "
//                        + "LEFT JOIN BCS_PNP_MAIN_EVERY8D AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
//                        + "LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
//                        + ") AS R1 "
//                        + "WHERE CREAT_TIME >= ?1 "
//                        + "AND CREAT_TIME <  DATEADD(DAY, 1, ?2) ";
//
//        if (StringUtils.isNotBlank(account)) {
//            queryString += "AND ACCOUNT = '" + account + "' ";
//        }
//        if (StringUtils.isNotBlank(pccCode)) {
//            queryString += "AND PCC_CODE = '" + pccCode + "' ";
//        }
//        if (StringUtils.isNotBlank(sourceSystem)) {
//            queryString += "AND SOURCE_SYSTEM = '" + sourceSystem + "' ";
//        }
//        if (StringUtils.isNotBlank(phoneNumber)) {
//            queryString += "AND PHONE = '" + phoneNumber + "' ";
//        }
//
//        boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
//        log.info("oracleUseDepartmentCheck:" + oracleUseDepartmentCheck);
//        if (oracleUseDepartmentCheck) {
//            String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
//            if (StringUtils.isNotBlank(empAva)) {
//                queryString += empAva;
//            }
//        }
//
//        queryString +=
//                ") AS R2 ";
//
//        log.info("str1: " + queryString);
//        Query query = entityManager.createNativeQuery(queryString).setParameter(1, startDate).setParameter(2, endDate);

        //--------------
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ( " +
                "        select " +
                "                orig_file_name, " +
                "                proc_flow, " +
                "                proc_stage, " +
                "                msg, " +
                "                phone, " +
                "                detail_schedule_time1, " +
                "                detail_schedule_time2, " +
                "                bc_time, " +
                "                pnp_time, " +
                "                status1, " +
                "                status2, " +
                "                status3, " +
                "                pcc_code, " +
                "                account, " +
                "                source_system, " +
                "                employee_id, " +
                "                creat_time, " +
                "                row_number() over ( " +
                "        order by " +
                "                creat_time desc) as rownum " +
                "        from ( " +
                "        ( " +
                "                select " +
                "                        m.orig_file_name, " +
                "                        d.proc_flow, " +
                "                        d.proc_stage, " +
                "                        msg, " +
                "                        d.phone, " +
                "                        d.detail_schedule_time as detail_schedule_time1, " +
                "                        d.detail_schedule_time as detail_schedule_time2, " +
                "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                "                        d.bc_status as status1, " +
                "                        d.pnp_status as status2, " +
                "                        d.sms_status as status3, " +
                "                        a.pcc_code, " +
                "                        a.account, " +
                "                        a.source_system, " +
                "                        a.employee_id, " +
                "                        d.creat_time " +
                "                from " +
                "                        bcs_pnp_detail_ming as d " +
                "                left join bcs_pnp_main_ming as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "                where d.creat_time >= ?1 and d.creat_time <  dateadd(day, 1, ?2) ");

        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            sb.append(String.format(" and phone = '%s'", phoneNumber));
        }

        sb.append(
                "        ) " +
                        "        union all " +
                        "        ( " +
                        "               select " +
                        "                        m.orig_file_name, " +
                        "                        d.proc_flow, " +
                        "                        d.proc_stage, " +
                        "                        msg, " +
                        "                        d.phone, " +
                        "                        null as detail_schedule_time1, " +
                        "                        null as detail_schedule_time2, " +
                        "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                        "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                        "                        d.bc_status as status1, " +
                        "                        d.pnp_status as status2, " +
                        "                        d.sms_status as status3, " +
                        "                        a.pcc_code, " +
                        "                        a.account, " +
                        "                        a.source_system, " +
                        "                        a.employee_id, " +
                        "                        d.creat_time " +
                        "                from " +
                        "                        bcs_pnp_detail_mitake as d " +
                        "                left join bcs_pnp_main_mitake as m on d.pnp_main_id = m.pnp_main_id " +
                        "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                        "                where d.creat_time >= ?3 and d.creat_time <  dateadd(day, 1, ?4) ");

        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            sb.append(String.format(" and phone = '%s'", phoneNumber));
        }

        sb.append(
                "        ) " +
                        "        union all " +
                        "        ( " +
                        "                select " +
                        "                        m.orig_file_name, " +
                        "                        d.proc_flow, " +
                        "                        d.proc_stage, " +
                        "                        msg, " +
                        "                        d.phone, " +
                        "                        null as detail_schedule_time1, " +
                        "                        null as detail_schedule_time2, " +
                        "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                        "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                        "                        d.bc_status as status1, " +
                        "                        d.pnp_status as status2, " +
                        "                        d.sms_status as status3, " +
                        "                        a.pcc_code, " +
                        "                        a.account, " +
                        "                        a.source_system, " +
                        "                        a.employee_id, " +
                        "                        d.creat_time " +
                        "                from " +
                        "                        bcs_pnp_detail_unica as d " +
                        "                left join bcs_pnp_main_unica as m on d.pnp_main_id = m.pnp_main_id " +
                        "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                        "                where d.creat_time >= ?5 and d.creat_time <  dateadd(day, 1, ?6) ");

        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            sb.append(String.format(" and phone = '%s'", phoneNumber));
        }

        sb.append(
                "        ) " +
                        "        union all " +
                        "        ( " +
                        "                select " +
                        "                        m.orig_file_name, " +
                        "                        d.proc_flow, " +
                        "                        d.proc_stage, " +
                        "                        msg, " +
                        "                        d.phone, " +
                        "                        null as detail_schedule_time1, " +
                        "                        null as detail_schedule_time2, " +
                        "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                        "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                        "                        d.bc_status as status1, " +
                        "                        d.pnp_status as status2, " +
                        "                        d.sms_status as status3, " +
                        "                        a.pcc_code, " +
                        "                        a.account, " +
                        "                        a.source_system, " +
                        "                        a.employee_id, " +
                        "                        d.creat_time " +
                        "                from " +
                        "                        bcs_pnp_detail_every8d as d " +
                        "                left join bcs_pnp_main_every8d as m on d.pnp_main_id = m.pnp_main_id " +
                        "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                        "                where d.creat_time >= ?7 and d.creat_time <  dateadd(day, 1, ?8) ");

        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            sb.append(String.format(" and phone = '%s'", phoneNumber));
        }

        sb.append("        ) " +
                " ) as r1");

        boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
        log.info("oracleUseDepartmentCheck:" + oracleUseDepartmentCheck);
        if (oracleUseDepartmentCheck) {
            String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
            if (StringUtils.isNotBlank(empAva)) {
                sb.append(empAva);
            }
        }

        sb.append(" ) as r2");

        log.info("str1: " + sb.toString());
        Query query = entityManager.createNativeQuery(sb.toString())
                .setParameter(1, startDate)
                .setParameter(2, endDate)
                .setParameter(3, startDate)
                .setParameter(4, endDate)
                .setParameter(5, startDate)
                .setParameter(6, endDate)
                .setParameter(7, startDate)
                .setParameter(8, endDate);
        //--------------

//        List<Object[]> list = query.getResultList();
//        String listStr = list.toString();
//        log.info("List1:" + list.toString());
//
//        // Total = Empty set,  []  => 0
//        if (listStr.length() <= 2) {
//            return "0";
//        }
//
//        // Total < 10
//        char c1 = listStr.charAt(listStr.length() - 2); // 個位數
//        if (listStr.length() == 3) {
//            return (c1 == '0') ? "0" : "1"; // [0] => 0 , [1] => 1
//        }
//
//        // Total >= 10
//        if (c1 == '0') {
//            return listStr.substring(1, listStr.length() - 2); // [430] => 43
//        }
//        char c10 = listStr.charAt(listStr.length() - 3); // 十位數
//        return listStr.substring(1, listStr.length() - 3) + (++c10); // [431] => 44

        //----------------
        return (int)query.getSingleResult();
        //----------------


    }
}
