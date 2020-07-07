package com.bcs.core.taishin.circle.db.service;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.repository.TaishinEmployeeRepository;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.tsib.RunBat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.hamcrest.CoreMatchers.nullValue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主要為台新Oracle Database連線及取得相關人事資料用
 *
 * @author ???
 */
@Slf4j
@Service
public class OracleService {

    private static final String KEY_SERVER = "server";
    private static final String KEY_UID = "uid";
    private static final String KEY_PWD = "pwd";
    private static final String KEY_DATABASE = "database";

    private TaishinEmployeeRepository taishinEmployeeRepository;

    @Autowired
    public OracleService(TaishinEmployeeRepository taishinEmployeeRepository) {
        this.taishinEmployeeRepository = taishinEmployeeRepository;
    }

    public void save(TaishinEmployee employeeRecord) {
        taishinEmployeeRepository.save(employeeRecord);
    }
    
    public TaishinEmployee findByLocalEmployeeId(String empId) throws Exception {
    	log.info("[findByLocalEmployeeId] EMP_ID = {}", empId);
        if (empId == null || empId.trim().isEmpty()) {
            throw new BcsNoticeException("The Employee ID is blank!");
        }
        
        // For Local and SIT 測試使用
        if (!empId.startsWith("LDAP")) {
        	empId = "TEST_EMPLOYEE";
        }
        
    	log.info("Final empId = {}", empId);
        
        TaishinEmployee taishinEmployee = taishinEmployeeRepository.findByEmployeeId(empId);
    	log.info("[findByLocalEmployeeId] taishinEmployee = {}", taishinEmployee);
    	
    	if (taishinEmployee != null) {
    		if (taishinEmployee.getEasyName().equalsIgnoreCase("quit")) {
    			throw new BcsNoticeException(String.format("編號%s員工已離職，故無法進行資料查詢及任何操作!!", empId));
    		}
    	}
        
        return taishinEmployee;
    }

    /**
     * 透過員工ID查詢員工資訊
     *
     * @param empId id
     * @return TaishinEmployee 員工資訊
     * @throws BcsNoticeException BcsNoticeException
     */
    public TaishinEmployee findByEmployeeId(String empId) throws Exception {
        log.info("[findByEmployeeId] EMP_ID=" + empId);
        if (empId == null || empId.trim().isEmpty()) {
            throw new BcsNoticeException("The Employee ID is blank!");
        }
        String[] connectionSettingArray = getConnectSettingArray();
        final String user = connectionSettingArray[1];
        final String password = connectionSettingArray[2];
        final String oracleDatabaseSourceUrl = connectionSettingArray[4];
        final String oracleSchemaHr = connectionSettingArray[5];
        boolean isEmployeeQuit = false;

        TaishinEmployee employee = new TaishinEmployee();
        try (Connection connection = getOracleConnection(user, password, oracleDatabaseSourceUrl)) {
            String sql = getFindByEmployeeIdSQL(empId, oracleSchemaHr);
            try (ResultSet rs = connection.createStatement().executeQuery(sql)) {
                while (rs.next()) {
                	//離職日期是null and empty 才撈取資料
                	if (StringUtils.isBlank(trim(rs.getString(8)))) {
	                    employee.setEmployeeId(empId);
	                    employee.setDepartmentId(trim(rs.getString(3)));
	                    employee.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
	                    employee.setDivisionName(trim(rs.getString(5)));
	                    employee.setDepartmentName(trim(rs.getString(6)));
	                    employee.setEasyName(trim(rs.getString(7)));                    
	                    employee.setGroupName(extractGroupName(employee));
                	} else {
                		isEmployeeQuit = true;
					}
                }
                log.info("TaiShin Employee: " + DataUtils.toPrettyJsonUseJackson(employee));
            } catch (Exception e) {
                //TODO 只有Not Found 才適用該錯誤訊息，其餘錯誤需另外編寫錯誤原因
                log.error("Exception", e);
                throw new BcsNoticeException("查無此員工編號!!");
            }
        }
        
//        if (isEmployeeQuit) {
//            throw new BcsNoticeException(String.format("編號%s員工已離職，無法進行此操作!!", empId));
//        }
        
        if (employee.getEmployeeId() == null
                || employee.getEmployeeId().trim().isEmpty()
                || employee.getDivisionName() == null
                || employee.getDivisionName().trim().isEmpty()
                || employee.getDepartmentId() == null
                || employee.getDepartmentId().trim().isEmpty()
                || employee.getGroupName() == null
                || employee.getGroupName().trim().isEmpty()) {
            throw new BcsNoticeException("查無此員工編號!!");
        }
        return employee;
    }

    public String getAvailableEmpIdsByEmpId(String empId, String role) {
        /* 1692 */
        if (AdminUser.RoleCode.ROLE_CUSTOMER_SERVICE.getRoleId().equals(role)) {
            return "";
        }

        /* 2888 */
        if (AdminUser.RoleCode.ROLE_ADMIN.getRoleId().equals(role)) {
            return "";
        }

        /* 2588 */
        if (AdminUser.RoleCode.ROLE_PNP_ADMIN.getRoleId().equals(role)) {
            return "";
        }

        boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
        if (!oracleUseDepartmentCheck || StringUtils.isBlank(empId)) {
            return "";
        }

        try {
            /* 0.Server 1.User 2.Password 3.databaseName 4.OracleSourceURL 5.OracleSchemaHr */
            String[] connectionSettingArray = getConnectSettingArray2();
            final String user = connectionSettingArray[1];
            final String password = connectionSettingArray[2];
            final String oracleDatabaseSourceUrl = connectionSettingArray[4];
            final String oracleSchemaHr = connectionSettingArray[5];

            String sql = getEmpIdList(oracleSchemaHr, findByEmployeeId(empId));

            List<String> empIdList = new ArrayList<>();
            try (Connection con = getOracleConnection(user, password, oracleDatabaseSourceUrl)) {
                try (ResultSet rs2 = con.createStatement().executeQuery(sql)) {
                    while (rs2.next()) {
                        empIdList.add(trim(rs2.getString(1)));
                    }
                    log.info("Employee Id List: " + empIdList);
                }
            }

            /* Result */
            // Merge to IN('', '') String
            String returnSql = String.format(" AND EMPLOYEE_ID IN ('%s') ", StringUtils.join(empIdList, "', '"));
            log.info("returnSQL:" + returnSql);

            return returnSql;
        } catch (Exception e) {
            log.info("[getAvailableEmpIdsByEmpId] error:" + e);
            return null;
        }
    }

    private String getEmpIdList(String oracleSchemaHr, TaishinEmployee emp) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" SELECT EMP_ID FROM %s.HR_EMP " +
                        " LEFT OUTER JOIN %s.HR_DEPT ON (HR_EMP.DEPT_SER_NO_ACT = HR_DEPT.DEPT_SERIAL_NO ",
                oracleSchemaHr, oracleSchemaHr)
        );

        if (StringUtils.isNotBlank(emp.getGroupName())) {
            sb.append(getGroupAuthSql(emp));
        } else if (StringUtils.isNotBlank(emp.getDepartmentName())) {
            sb.append(getDepartmentAuthSql(emp));
        } else {
            sb.append(getDivisionAuthSql(emp));
        }

        log.info("sqlString2:" + sb.toString());
        return sb.toString();
    }

    /**
     * 處權限
     */
    private String getDivisionAuthSql(TaishinEmployee emp) {
        return String.format(" WHERE TRIM(CARD_DIV) = '%s' ", emp.getDivisionName());
    }

    /**
     * 部權限
     */
    private String getDepartmentAuthSql(TaishinEmployee emp) {
        return String.format(" WHERE TRIM(CARD_DIV) = '%s' AND TRIM(CARD_DEPT) = '%s' ", emp.getDivisionName(), emp.getDepartmentName());
    }

    /**
     * 組權限
     */
    private String getGroupAuthSql(TaishinEmployee emp) {
        return String.format(" WHERE TRIM(CARD_DIV) = '%s' AND TRIM(CARD_DEPT) = '%s' AND DEPT_EASY_NM LIKE ", emp.getDivisionName(), emp.getDepartmentName()) +
                "'%" + emp.getGroupName() + "%'";
    }

    /**
     * 加載JDBC驅動及取得資料庫連線
     *
     * @param user                    user
     * @param password                password
     * @param oracleDatabaseSourceUrl oracleDatabaseSourceUrl
     * @return Connection
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws SQLException           SQLException
     */
    private Connection getOracleConnection(String user, String password, String oracleDatabaseSourceUrl) throws ClassNotFoundException, SQLException {
        log.info("Get Connection...");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connection = DriverManager.getConnection(oracleDatabaseSourceUrl, user, password);
        log.info("Get Connected!!");
        return connection;
    }


    /**
     * @return 0.Server 1.User 2.Password 3.databaseName 4.OracleSourceURL 5.OracleSchemaHr
     */
    private String[] getConnectSettingArray() {

        boolean isUseLdap = "true".equals(CoreConfigReader.getString("oracleUseLdap"));
        String[] connectArray = new String[]{"", "", "", "", "", ""};

        if (isUseLdap) {
            getLdapConnectSetting(connectArray);
        } else {
            getLocalConnectSetting(connectArray);
        }
        log.info("Connection Setting Array: " + Arrays.toString(connectArray));

        return connectArray;
    }

    /**
     * @return 0.Server 1.User 2.Password 3.databaseName 4.OracleSourceURL 5.OracleSchemaHr
     */
    private String[] getConnectSettingArray2() {
        String[] connectArray = new String[]{"", "", "", "", "", ""};
        getLdapConnectSetting(connectArray);
        log.info("Connection Setting Array: " + Arrays.toString(connectArray));
        return connectArray;
    }

    /**
     * Local Setting Array Assign
     *
     * @param connectArray connectArray
     */
    private void getLocalConnectSetting(String[] connectArray) {
        connectArray[0] = "SYSTEM";
        connectArray[1] = "123";
        connectArray[2] = "XEPDB1";
        connectArray[3] = "LOCALHOST";
        connectArray[4] = String.format("jdbc:oracle:thin:@%s:1521/%s", connectArray[0], connectArray[3]);
        connectArray[5] = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
    }

    /**
     * LDAP Setting Array Assign
     *
     * @param connectArray connectArray
     */
    private void getLdapConnectSetting(String[] connectArray) {
        String connection = getDataBaseConnectionInfo(
                CoreConfigReader.getString("oracleLdapHost"),
                CoreConfigReader.getString("oracleApName"),
                CoreConfigReader.getInteger("oracleApGroup"),
                CoreConfigReader.getString("oracleSearchBase"));

        log.info("Connection:" + connection);

        String[] connectionArray = connection.split(";");
        log.info("Connection Array: " + Arrays.toString(connectionArray));
        for (String keyValue : connectionArray) {
            if (StringUtils.isBlank(keyValue)) {
                continue;
            }
            String[] keyValueArray = keyValue.split("=");
            log.info("KeyValue Array: " + Arrays.toString(keyValueArray));
            if (keyValueArray.length == 2) {
                switch (keyValueArray[0]) {
                    case KEY_SERVER:
                        connectArray[0] = keyValueArray[1].trim().toUpperCase();
                        break;
                    case KEY_UID:
                        connectArray[1] = keyValueArray[1].trim().toUpperCase();
                        break;
                    case KEY_PWD:
                        connectArray[2] = keyValueArray[1].trim().toUpperCase();
                        break;
                    case KEY_DATABASE:
                        connectArray[3] = keyValueArray[1].trim().toUpperCase();
                        break;
                    default:
                        break;
                }
            }
        }
        connectArray[4] = String.format("jdbc:oracle:thin:@%s:1521/%s", connectArray[0], connectArray[3]);
        connectArray[5] = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
    }


    /**
     * Replace 處, 部 to Space
     */
    private String extractGroupName(TaishinEmployee emp) {
        String easyName = emp.getEasyName().replaceAll(emp.getDivisionName(), "")
                .replaceAll(emp.getDepartmentName(), "");
        log.info("extractGroupName:" + easyName);
        return easyName;
    }

    /**
     * Trim String if not null of blank
     *
     * @param str String
     * @return After Trim String
     */
    private String trim(String str) {
        return StringUtils.isBlank(str) ? "" : str.trim();
    }

    private String getDataBaseConnectionInfo(String ldapHost, String apName, int apGroup, String searchBase) {
        try {
            RunBat ap1 = new RunBat();
            ap1.SSL = false;
            ap1.ldapHost = ldapHost;
            ap1.searchBase = searchBase;
            return ap1.GetRunBat(apName, apGroup);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return "";
    }


    private String getFindByEmployeeIdSQL(String empId, String oracleSchemaHr) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                " select " +
                        "     emp_id, " +
                        "     dept_ser_no_act, " +
                        "     acct_dept_cd, " +
                        "     acct_grp_cd, " +
                        "     card_div, " +
                        "     card_dept, " +
                        "     dept_easy_nm, " +
                        "     quit_dt " +                        
                        " from " +
                        "     %s.hr_emp " +
                        " left outer join %s.hr_dept " +
                        " on (hr_emp.dept_ser_no_act = hr_dept.dept_serial_no) " +
                        " where " +
                        " trim(emp_id) = '%s'", oracleSchemaHr, oracleSchemaHr, empId));
        log.info("sqlString:" + sb.toString());
        return sb.toString();
    }

}