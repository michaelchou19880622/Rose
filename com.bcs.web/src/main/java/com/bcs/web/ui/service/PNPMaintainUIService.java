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
                    "                sms_time, " +
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
                    "                        d.creat_time, " +
                    "                        convert(varchar, d.sms_time, 120) as sms_time" +
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
                            "                        d.creat_time, " +
                            "                        convert(varchar, d.sms_time, 120) as sms_time" +
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
                            "                        d.creat_time, " +
                            "                        convert(varchar, d.sms_time, 120) as sms_time" +
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
                            "                        d.creat_time, " +
                            "                        convert(varchar, d.sms_time, 120) as sms_time" +
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
                List<String> dataList = new ArrayList<>();
                map.put(Integer.toString(count), dataList);
                for (int i = 0, max = 18; i < max; i++) {
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
                "                sms_time " +
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
                "                        d.creat_time, " +
                "                        d.sms_time " +
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
                        "                        d.creat_time, " +
                        "                        d.sms_time " +
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
                        "                        d.creat_time, " +
                        "                        d.sms_time " +
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
                        "                        d.creat_time, " +
                        "                        d.sms_time " +
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
        int totalCount = (int) query.getSingleResult();
        return totalCount / 10 + 1;
    }
}
