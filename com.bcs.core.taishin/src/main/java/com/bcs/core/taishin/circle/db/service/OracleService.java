package com.bcs.core.taishin.circle.db.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.repository.TaishinEmployeeRepository;
import com.bcs.core.utils.ErrorRecord;
import com.tsib.RunBat;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主要為台新Oracle Database連線及取得相關人事資料用
 *
 * @author ???
 */
@Service
public class OracleService {
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(OracleService.class);

    private static final String KEY_SERVER = "server";
    private static final String KEY_UID = "uid";
    private static final String KEY_PWD = "pwd";
    private static final String KEY_DATABASE = "database";

    @Autowired
    private TaishinEmployeeRepository taishinEmployeeRepository;

    public void save(TaishinEmployee employeeRecord) {
        taishinEmployeeRepository.save(employeeRecord);
    }

    public TaishinEmployee findByEmployeeId(String empId) {
        logger.info("[findByEmployeeId] EMP_ID=" + empId);
        try {
            String[] connectionSettingArray = getConnectSettingArray();
            final String user = connectionSettingArray[1];
            final String password = connectionSettingArray[2];
            final String oracleDatabaseSourceUrl = connectionSettingArray[4];
            final String oracleSchemaHr = connectionSettingArray[5];

            Connection con = getOracleConnection(user, password, oracleDatabaseSourceUrl);

            Statement stmt = con.createStatement();
            String sql = getFindByEmployeeIdSQL(empId, oracleSchemaHr);

            TaishinEmployee emp = new TaishinEmployee();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                for (int i = 1; i <= 7; i++) {
                    logger.info("[findByEmployeeId] i=" + i + ", s=" + rs.getString(i));
                }
                emp.setEmployeeId(empId);
                emp.setDepartmentId(trim(rs.getString(2)));
                emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
                emp.setDivisionName(trim(rs.getString(5)));
                emp.setDepartmentName(trim(rs.getString(6)));
                emp.setEasyName(trim(rs.getString(7)));
                emp.setGroupName(extractGroupName(emp));
            }
            logger.info("[findByEmployeeId] emp:" + emp);

            rs.close();
            con.close();
            return emp;
        } catch (Exception e) {
            logger.info("[findByEmployeeId] error:" + e);
            return null;
        }
    }

    public String getAvailableEmpIdsByEmpId(String empId) {
        logger.info("[getAvailableEmpIdsByEmpId] EMP_ID=" + empId);
        if (StringUtils.isBlank(empId)) {
            return "";
        }

        try {
            /* 0.Server 1.User 2.Password 3.databaseName 4.OracleSourceURL 5.OracleSchemaHr */
            String[] connectionSettingArray = getConnectSettingArray2();
            final String user = connectionSettingArray[1];
            final String password = connectionSettingArray[2];
            final String oracleDatabaseSourceUrl = connectionSettingArray[4];
            final String oracleSchemaHr = connectionSettingArray[5];


            Connection con = getOracleConnection(user, password, oracleDatabaseSourceUrl);

            String sql = getAvilableEmpIdsByEmpIdSql(empId, oracleSchemaHr);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            TaishinEmployee emp = new TaishinEmployee();
            while (rs.next()) {
                for (int i = 1; i <= 7; i++) {
                    logger.info("[findByEmployeeId] i=" + i + ", s=" + rs.getString(i));
                }
                emp.setEmployeeId(empId);
                emp.setDepartmentId(trim(rs.getString(2)));
                emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
                emp.setDivisionName(trim(rs.getString(5)));
                emp.setDepartmentName(trim(rs.getString(6)));
                emp.setEasyName(trim(rs.getString(7)));
                emp.setGroupName(extractGroupName(emp));
            }
            logger.info("[getAvailableEmpIdsByEmpId] emp:" + emp);


            String sql2 = getEmpIdList(oracleSchemaHr, emp);

            Statement stmt2 = con.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sql2);

            List<String> empIdList = new ArrayList<>();
            while (rs2.next()) {
                empIdList.add(trim(rs2.getString(1)));
            }
            logger.info("Employee Id List: " + empIdList);

            /* Result */
            // Merge to IN('', '') String
            String returnSQL = String.format(" AND EMPLOYEE_ID IN ('%s') ", StringUtils.join(empIdList, "', '"));
            logger.info("returnSQL:" + returnSQL);

            rs.close();
            rs2.close();
            con.close();
            return returnSQL;
        } catch (Exception e) {
            logger.info("[getAvailableEmpIdsByEmpId] error:" + e);
            return null;
        }
    }

    private String getEmpIdList(String oracleSchemaHr, TaishinEmployee emp) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" SELECT EMP_ID FROM %s.HR_EMP_SW " +
                        " LEFT OUTER JOIN %s.HR_DEPT_SW ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO ",
                oracleSchemaHr, oracleSchemaHr)
        );

        if (StringUtils.isNotBlank(emp.getGroupName())) {
            // 組權限
            sb.append(String.format(" WHERE TRIM(CARD_DIV) = '%s' AND TRIM(CARD_DEPT) = '%s' AND DEPT_EASY_NM LIKE ",
                    emp.getDivisionName(), emp.getDepartmentName())
            );
            sb.append("'%");
            sb.append(emp.getGroupName());
            sb.append("%'");
        } else if (StringUtils.isNotBlank(emp.getDepartmentName())) {
            // 部權限
            sb.append(String.format(" WHERE TRIM(CARD_DIV) = '%s' AND TRIM(CARD_DEPT) = '%s' ",
                    emp.getDivisionName(), emp.getDepartmentName())
            );
        } else {
            // 處權限
            sb.append(String.format(" WHERE TRIM(CARD_DIV) = '%s' ",
                    emp.getDivisionName())
            );
        }

        logger.info("sqlString2:" + sb.toString());
        return sb.toString();
    }

    private Connection getOracleConnection(String user, String password, String oracleDatabaseSourceUrl) throws ClassNotFoundException, SQLException {
        logger.info("Get Connection...");
        /* 加載JDBC驅動 */
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connection = DriverManager.getConnection(oracleDatabaseSourceUrl, user, password);
        logger.info("Get Connected!!");
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
            getLocalConnectArray(connectArray);
        }
        logger.info("Connection Setting Array: " + Arrays.toString(connectArray));

        return connectArray;
    }

    /**
     *
     * @return 0.Server 1.User 2.Password 3.databaseName 4.OracleSourceURL 5.OracleSchemaHr
     */
    private String[] getConnectSettingArray2() {
        String[] connectArray = new String[]{"", "", "", "", "", ""};
        getLdapConnectSetting(connectArray);
        logger.info("Connection Setting Array: " + Arrays.toString(connectArray));
        return connectArray;
    }

    private void getLocalConnectArray(String[] connectArray) {
        connectArray[0] = "SYSTEM";
        connectArray[1] = "123";
        connectArray[2] = "XEPDB1";
        connectArray[3] = "LOCALHOST";
        connectArray[4] = String.format("jdbc:oracle:thin:@%s:1521/%s", connectArray[0], connectArray[3]);
        connectArray[5] = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
    }

    private void getLdapConnectSetting(String[] connectArray) {
        String ldapHost = CoreConfigReader.getString("oracleLdapHost");
        String apName = CoreConfigReader.getString("oracleApName");
        int apGroup = CoreConfigReader.getInteger("oracleApGroup");
        String searchBase = CoreConfigReader.getString("oracleSearchBase");
        String connection = getDataBaseConnectionInfo(ldapHost, apName, apGroup, searchBase);

        logger.info("Connection:" + connection);

        String[] split = connection.split(";");
        logger.info("Connection Array: " + Arrays.toString(split));
        for (String str : split) {
            if (StringUtils.isNotBlank(str)) {
                String[] keyValue = str.split("=");
                logger.info("KeyValue Array: " + Arrays.toString(keyValue));
                if (keyValue.length == 2) {
                    if (KEY_SERVER.equals(keyValue[0])) {
                        connectArray[0] = keyValue[1].toUpperCase();
                    }
                    if (KEY_UID.equals(keyValue[0])) {
                        connectArray[1] = keyValue[1].toUpperCase();
                    }
                    if (KEY_PWD.equals(keyValue[0])) {
                        connectArray[2] = keyValue[1].toUpperCase();
                    }
                    if (KEY_DATABASE.equals(keyValue[0])) {
                        connectArray[3] = keyValue[1].toUpperCase();
                    }
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
        logger.info("extractGroupName:" + easyName);
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
            logger.error(ErrorRecord.recordError(e));
        }
        return "";
    }


    private String getFindByEmployeeIdSQL(String empId, String ORACLE_SCHEMA_HR) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                " select " +
                        "     emp_id, " +
                        "     dept_ser_no_act, " +
                        "     acct_dept_cd, " +
                        "     acct_grp_cd, " +
                        "     card_div, " +
                        "     card_dept, " +
                        "     dept_easy_nm " +
                        " from " +
                        "     %s.hr_emp_sw " +
                        " left outer join %s.hr_dept_sw on (hr_emp_sw.dept_ser_no_act = hr_dept_sw.dept_serial_no) " +
                        " where " +
                        " trim(emp_id) = '%s'", ORACLE_SCHEMA_HR, ORACLE_SCHEMA_HR, empId));
        logger.info("sqlString:" + sb.toString());
        return sb.toString();
    }

    private String getAvilableEmpIdsByEmpIdSql(String empId, String oracleSchemaHr) {
        String sqlString = String.format(
                " SELECT " +
                        "     EMP_ID, DEPT_SER_NO_ACT," +
                        "     ACCT_DEPT_CD," +
                        "     ACCT_GRP_CD," +
                        "     CARD_DIV," +
                        "     CARD_DEPT," +
                        "     DEPT_EASY_NM " +
                        " FROM %s.HR_EMP_SW LEFT OUTER JOIN %s.HR_DEPT_SW " +
                        " ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) " +
                        " WHERE TRIM(EMP_ID) = '%s' ", oracleSchemaHr, oracleSchemaHr, empId);
        logger.info("sqlString:" + sqlString);
        return sqlString;
    }
}