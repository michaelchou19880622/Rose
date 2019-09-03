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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 主要為台新Oracle Database連線及取得相關人事資料用
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
            // use LDAP or Local Database
            boolean isUseLdap = "true".equals(CoreConfigReader.getString("oracleUseLdap"));
            String server = "";
            String user = "";
            String password = "";
            String databasesName = "";

            if (isUseLdap) {
                String ldapHost = CoreConfigReader.getString("oracleLdapHost");
                String apName = CoreConfigReader.getString("oracleApName");
                int apGroup = CoreConfigReader.getInteger("oracleApGroup");
                String searchBase = CoreConfigReader.getString("oracleSearchBase");
                String connection = getDataBaseConnectionInfo(ldapHost, apName, apGroup, searchBase);
                logger.info("connection:" + connection);

                String[] split = connection.split(";");

                for (String str : split) {
                    if (StringUtils.isNotBlank(str)) {
                        String[] keyValue = str.split("=");

                        if (keyValue.length == 2) {
                            if (KEY_SERVER.equals(keyValue[0])) {
                                server = keyValue[1];
                            }
                            if (KEY_UID.equals(keyValue[0])) {
                                user = keyValue[1];
                            }
                            if (KEY_PWD.equals(keyValue[0])) {
                                password = keyValue[1];
                            }
                            if (KEY_DATABASE.equals(keyValue[0])) {
                                databasesName = keyValue[1];
                            }
                        }
                    }
                }
                user = user.toUpperCase();
                password = password.toUpperCase();
                databasesName = databasesName.toUpperCase();
                server = server.toUpperCase();
            } else {
                user = "SYSTEM";
                password = "123";
                databasesName = "XEPDB1";
                server = "LOCALHOST";
            }

            String oracleDataSourceUrl = "jdbc:oracle:thin:@" + server + ":1521/" + databasesName;
            Connection con = DriverManager.getConnection(oracleDataSourceUrl, user, password);

            /* Get ORACLE_SCHEMA_HR */
            final String ORACLE_SCHEMA_HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);

            logger.info("Server                 : " + server);
            logger.info("User                   : " + user);
            logger.info("Password               : " + password);
            logger.info("Databases Name         : " + databasesName);
            logger.info("Oracle Data Source Url : " + oracleDataSourceUrl);
            logger.info("ORACLE_SCHEMA_HR       : " + ORACLE_SCHEMA_HR);

            Statement stmt = con.createStatement();
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

            TaishinEmployee emp = new TaishinEmployee();
            ResultSet rs = stmt.executeQuery(sb.toString());
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
            // connect to database
            String ldapHost = CoreConfigReader.getString("oracleLdapHost");
            String apName = CoreConfigReader.getString("oracleApName");
            int apGroup = CoreConfigReader.getInteger("oracleApGroup");
            String searchBase = CoreConfigReader.getString("oracleSearchBase");
            String connection = getDataBaseConnectionInfo(ldapHost, apName, apGroup, searchBase);
            logger.info("connection:" + connection);

            String[] split = connection.split(";");
            String server = "";
            String user = "";
            String password = "";
            String databaseName = "";
            for (String str : split) {
                if (StringUtils.isNotBlank(str)) {
                    String[] keyValue = str.split("=");

                    if (keyValue.length == 2) {
                        if (KEY_SERVER.equals(keyValue[0])) {
                            server = keyValue[1];
                        }
                        if (KEY_UID.equals(keyValue[0])) {
                            user = keyValue[1];
                        }
                        if (KEY_PWD.equals(keyValue[0])) {
                            password = keyValue[1];
                        }
                        if (KEY_DATABASE.equals(keyValue[0])) {
                            databaseName = keyValue[1];
                        }
                    }
                }
            }
            user = user.toUpperCase();
            password = password.toUpperCase();
            databaseName = databaseName.toUpperCase();
            server = server.toUpperCase();

            final String ORACLE_DATASOURCE_URL = "jdbc:oracle:thin:@" + server + ":1521/" + databaseName;
            Connection con = DriverManager.getConnection(ORACLE_DATASOURCE_URL, user, password);
            final String ORACLE_SCHEMA_HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
            logger.info("Server                : " + server);
            logger.info("User                  : " + user);
            logger.info("Password              : " + password);
            logger.info("Database Name         : " + databaseName);
            logger.info("ORACLE_DATASOURCE_URL : " + ORACLE_DATASOURCE_URL);
            logger.info("ORACLE_SCHEMA_HR      : " + ORACLE_SCHEMA_HR);

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
                            " WHERE TRIM(EMP_ID) = '%s' ", ORACLE_SCHEMA_HR, ORACLE_SCHEMA_HR, empId);
            logger.info("sqlString:" + sqlString);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
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

            // get List of EmpIds

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(" SELECT EMP_ID FROM %s.HR_EMP_SW " +
                            " LEFT OUTER JOIN %s.HR_DEPT_SW ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO ",
                    ORACLE_SCHEMA_HR, ORACLE_SCHEMA_HR)
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

            Statement stmt2 = con.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sb.toString());

            List<String> empIdList = new ArrayList<>();
            while (rs2.next()) {
                empIdList.add(trim(rs2.getString(1)));
            }
            logger.info("Employee Id List: " + empIdList);

            /* Result */
            // Merge to IN('', '') String
            String returnSQL = String.format(" AND EMPLOYEE_ID IN ('%s') ", StringUtils.join(empIdList, "', '"));
            logger.info("returnSQL:" + returnSQL);
            con.close();
            return returnSQL;
        } catch (Exception e) {
            logger.info("[getAvailableEmpIdsByEmpId] error:" + e);
            return null;
        }
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
}