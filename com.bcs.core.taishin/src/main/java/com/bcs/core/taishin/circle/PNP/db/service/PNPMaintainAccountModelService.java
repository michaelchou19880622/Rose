package com.bcs.core.taishin.circle.PNP.db.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PNPMaintainAccountModelService {

    @Autowired
    private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository;
    @Autowired
    private PNPMaintainAccountModelCustom PNPMaintainAccountModelCustom;
    @Autowired
    private OracleService oraclePnpService;
    @PersistenceContext
    EntityManager entityManager;

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
        return PNPMaintainAccountModelCustom.queryUseConditions(divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
    }

    public List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent) {
        return pnpMaintainAccountModelRepository.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
    }

    public List<PNPMaintainAccountModel> findByAccountType(String accountType) {
        return pnpMaintainAccountModelRepository.findByAccountType(accountType);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getPNPDetailReportExcelList(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {

        StringBuilder sb = getSQL(account, pccCode, sourceSystem, empId);
        Query query = entityManager.createNativeQuery(sb.toString()).setParameter(1, startDate).setParameter(2, endDate);
        log.info("query:" + query.toString());
        List<Object[]> list = query.getResultList();
        DataUtils.toPrettyJsonUseJackson(list);
        int j = 0;
        for (Object[] objArray : list) {
            log.info(j + ") " + Arrays.toString(objArray));
            j++;
        }

        Map<String, List<String>> map = new LinkedHashMap<>();

        int count = 0;
        for (Object[] o : list) {
            count++;
            log.info("c:" + count);
            List<String> dataList = new ArrayList<>();
            map.put(Integer.toString(count), dataList);
            for (int i = 0, max = 27; i < max; i++) {
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }
        log.info("map1: " + map.toString());

        return map;
    }


    @SuppressWarnings("unchecked")
    public List<Map<Integer, String>> getPNPDetailReportExcelMapList(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {
        try {
            StringBuilder sb = getSQL(account, pccCode, sourceSystem, empId);
            Query query = entityManager.createNativeQuery(DataUtils.replaceUnnecessarySpace(sb.toString()))
                    .setParameter(1, startDate)
                    .setParameter(2, endDate);

            final int maxColumn = 27;
            List<Object[]> rowArrayList = query.getResultList();

            int index = 0;
            List<Map<Integer, String>> rowDataList = new LinkedList<>();
            /* Header */
            Map<Integer, String> columnDataMap = getHeaderMap(maxColumn);
            rowDataList.add(columnDataMap);

            /* Data */
            /* 取出每一行資料*/
            for (Object[] rowData : rowArrayList) {
                columnDataMap = new LinkedHashMap<>(maxColumn);
                for (int columnIndex = 0; columnIndex < maxColumn; columnIndex++) {
                    /* 取出每一欄資料 */
                    String value = rowData[columnIndex] == null ? "" : rowData[columnIndex].toString();
                    value = columnSpecialProcess(columnIndex, value);
                    columnDataMap.put(columnIndex, value);
                }
                rowDataList.add(columnDataMap);
            }

            return rowDataList;
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }

    private String columnSpecialProcess(int columnIndex, String value) {
        /* 資料特殊處理 */
        log.info("{},{}", columnIndex, value);
        switch (columnIndex) {
            case 21:
            case 22:
            case 23:
                /* 21 BC發送狀態 */
                /* 22 PNP發送狀態*/
                /* 23 SMS發送狀態*/
                value = englishStatusToChinese(value);
                break;
            case 3:
                /* 發送通路(ex: PNP_明宣) */
                log.info("Proc: " + value);
                String[] valueArray = value.split(";");
                log.info("Proc Array: " + Arrays.toString(valueArray));
                String stage = valueArray[0];
                String source = valueArray[1];
                String sourceChinese = englishSourceToChinese(source);
                value = String.format("%s_%s", stage, sourceChinese);
                break;
            case 2:
                /* 通路流(1.2.3.4.) */
                value = englishProcFlowToChinese(value);
                break;
            default:
                break;
        }
        return value == null ? "" : value;
    }

    private StringBuilder getSQL(String account, String pccCode, String sourceSystem, String empId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from " +
                " ( " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        null as campaign_id, " +
                "                        null as segment_id, " +
                "                        null as program_id, " +
                "                        null as pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        d.detail_schedule_time as detail_schedule_time1, " +
                "                        d.detail_schedule_time as detail_schedule_time2, " +
                "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                "                        d.bc_status as status1, " +
                "                        d.pnp_status as status2, " +
                "                        d.sms_status as status3, " +
                "                        null as is_international, " +
                "                        convert(varchar, d.creat_time, 120) AS create_time, " +
                "                        convert(varchar, d.modify_time, 120) AS modify_time, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_ming as d " +
                "                left join bcs_pnp_main_ming as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.dest_name, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        null as campaign_id, " +
                "                        null as segment_id, " +
                "                        null as program_id, " +
                "                        null as pid, " +
                "                        d.phone, " +//15
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                "                        d.bc_status as status1, " +//21
                "                        d.pnp_status as status2, " +
                "                        d.sms_status as status3, " +
                "                        null as is_international, " +
                "                        convert(varchar, d.creat_time, 120) AS create_time, " +//25
                "                        convert(varchar, d.modify_time, 120) AS modify_time, " +//26
                "                        a.employee_id" +
                "                from bcs_pnp_detail_mitake as d " +
                "                left join bcs_pnp_main_mitake as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        d.campaign_id, " +
                "                        d.segment_id, " +
                "                        d.program_id, " +
                "                        d.pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                "                        d.bc_status as status1, " +
                "                        d.pnp_status as status2, " +
                "                        d.sms_status as status3, " +
                "                        null as is_international, " +
                "                        convert(varchar, d.creat_time, 120) AS create_time, " +
                "                        convert(varchar, d.modify_time, 120) AS modify_time, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_unica as d " +
                "                left join bcs_pnp_main_unica as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        d.campaign_id, " +
                "                        d.segment_id, " +
                "                        d.program_id, " +
                "                        d.pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        convert(varchar, d.line_push_time, 120) as bc_time, " +
                "                        convert(varchar, d.pnp_time, 120) as pnp_time, " +
                "                        d.bc_status as status1, " +
                "                        d.pnp_status as status2, " +
                "                        d.sms_status as status3, " +
                "                        null as is_international, " +
                "                        convert(varchar, d.creat_time, 120) AS create_time, " +
                "                        convert(varchar, d.modify_time, 120) AS modify_time, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_every8d as d " +
                "                left join bcs_pnp_main_every8d as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                " ) as r1 " +
                " where create_time >= ?1 " +
                " and create_time <  dateadd(day, 1, ?2) ");


        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }

        boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
        log.info("oracleUseDepartmentCheck:" + oracleUseDepartmentCheck);
        if (oracleUseDepartmentCheck) {
            String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
            if (StringUtils.isNotBlank(empAva)) {
                sb.append(empAva);
            }
        }

        sb.append(" order by create_time desc ");

        log.info("str1: " + DataUtils.replaceUnnecessarySpace(sb.toString()));
        return sb;
    }

    private Map<Integer, String> getHeaderMap(int maxColumn) {
        Map<Integer, String> columnDataMap = new LinkedHashMap<>(maxColumn);
        columnDataMap.put(0, "序號");
        columnDataMap.put(1, "前方來源系統");
        columnDataMap.put(2, "通路流");
        columnDataMap.put(3, "發送通路");
        columnDataMap.put(4, "發送帳號");
        columnDataMap.put(5, "掛帳PccCode");
        columnDataMap.put(6, "發送廠商訊息批次代碼");
        columnDataMap.put(7, "發送廠商訊息流水號");
        columnDataMap.put(8, "訊息樣板");
        columnDataMap.put(9, "訊息內文");
        columnDataMap.put(10, "訊息內文點數");
        columnDataMap.put(11, "行銷活動代碼");
        columnDataMap.put(12, "行銷活動階段");
        columnDataMap.put(13, "行銷活動客群代碼");
        columnDataMap.put(14, "客戶ID");
        columnDataMap.put(15, "客戶手機號碼");
        columnDataMap.put(16, "UID");
        columnDataMap.put(17, "預約日期");
        columnDataMap.put(18, "預約時間");
        columnDataMap.put(19, "BC發送日期");
        columnDataMap.put(20, "PNP發送時間");
        columnDataMap.put(21, "BC發送狀態");
        columnDataMap.put(22, "PNP發送狀態");
        columnDataMap.put(23, "SMS發送狀態");
        columnDataMap.put(24, "是否國際簡訊");
        columnDataMap.put(25, "資料建立日期");
        columnDataMap.put(26, "資料更新日期");
        return columnDataMap;
    }

    private String englishStatusToChinese(String status) {
        switch (status) {

            case "DRAFT":
                return "正在存進資料庫";
            case "WAIT":
                return "等待進入處理程序";
            case "SCHEDULED":
                return "等待預約發送";
            case "BC_PROCESS":
                return "進行BC發送處理中";
            case "BC_SENDING":
                return "BC發送中";
            case "BC_COMPLETE":
                return "BC處理程序完成";
            case "BC_FAIL":
                return "BC發送失敗";
            case "BC_FAIL_PNP_PROCESS":
                return "轉發PNP";
            case "BC_FAIL_SMS_PROCESS":
                return "轉發SMS";
            case "PNP_SENDING":
                return "PNP發送中";
            case "CHECK_DELIVERY":
                return "已發送，等待回應";
            case "PNP_COMPLETE":
                return "PNP處理程序完成";
            case "PNP_FAIL_SMS_PROCESS":
                return "轉發SMS";
            case "SMS_COMPLETE":
                return "SMS處理程序完成";
            case "SMS_FAIL":
                return "SMS發送失敗";


            case "PROCESS":
                return "發送處理進行中";
            case "FINISH":
                return "發送處理完成";
            case "SENDING":
                return "發送中";
            case "DELETE":
                return "已刪除";
            case "COMPLETE":
                return "處理程序完成";


            default:
                return status;
        }
    }

    /**
     * Transfer Source Code To Chinese Name
     *
     * @param sourceCode Source Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    private String englishSourceToChinese(String sourceCode) {
        log.info("sourceCode: " + sourceCode);
        switch (sourceCode) {
            case "1":
                return "三竹";
            case "2":
                return "互動";
            case "3":
                return "明宣";
            case "4":
                return "UNICA";
            default:
                return sourceCode;
        }
    }


    /**
     * Transfer procFlow Code To Chinese Name
     *
     * @param procFlowCode procFlow Code 1. 2. 3. 4.
     * @return Source Chinese Name
     */
    private String englishProcFlowToChinese(String procFlowCode) {
        log.info("procFlowCode: " + procFlowCode);
        switch (procFlowCode) {
            case "0":
                return "SMS";
            case "1":
                return "BC";
            case "2":
                return "BC->PNP";
            case "3":
                return "BC->PNP->SMS";
            default:
                return procFlowCode;
        }
    }

}
