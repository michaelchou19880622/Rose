package com.bcs.core.db.repository;

import com.bcs.core.db.entity.SendGroupDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
public class GroupGenerateRepository {


    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SendGroupDetailRepository sendGroupDetailRepository;

    public static List<String> validQueryOp = Arrays.asList(">", ">=", "<", "<=", "=");

    @PostConstruct
    public void init() {

    }

    public BigInteger findMIDCountBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
        Query query = buildFindQuery(sendGroupDetails, "COUNT(DISTINCT MID)");
        Object result = query.getSingleResult();
        if (result instanceof BigInteger) {
            log.debug("findMidCountBySendGroupDetail : BigInteger:" + result);
            return (BigInteger) result;
        } else if (result instanceof Integer) {
            log.debug("findMidCountBySendGroupDetail : Integer:" + result);
            return BigInteger.valueOf((long) result);
        } else {
            return BigInteger.ZERO;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> findMIDBySendGroupDetailGroupId(Long groupId) throws Exception {
        List<SendGroupDetail> sendGroupDetails = sendGroupDetailRepository.findBySendGroupGroupId(groupId);
        Query query = buildFindQuery(sendGroupDetails, "DISTINCT MID");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public String checkMIDBySendGroupDetailGroupId(Long groupId, String mid) throws Exception {
        List<SendGroupDetail> sendGroupDetails = sendGroupDetailRepository.findBySendGroupGroupId(groupId);
        Query query = buildFindQuery(sendGroupDetails, "MID", mid);
        List<String> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> findMIDBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {

        Query query = buildFindQuery(sendGroupDetails, "DISTINCT MID");
        return query.getResultList();
    }

    private Query buildFindQuery(List<SendGroupDetail> sendGroupDetails, String selectColumns) throws Exception {
        return this.buildFindQuery(sendGroupDetails, selectColumns, null);
    }

    /**
     * 建立用來查詢 IK0105 的 Query
     *
     * @param sendGroupDetails
     * @return
     * @throws Exception
     */
    private Query buildFindQuery(List<SendGroupDetail> sendGroupDetails, String selectColumns, String mid) throws Exception {
        Validate.notEmpty(sendGroupDetails);
        Validate.notEmpty(selectColumns);

        List<SendGroupDetail> sendGroupSetting = new ArrayList<>();
        List<SendGroupDetail> uploadMidSetting = new ArrayList<>();

        // 分離 Upload Mid Detail Setting
        for (SendGroupDetail detail : sendGroupDetails) {
            if ("UploadMid".equals(detail.getQueryField())) {
                uploadMidSetting.add(detail);
            } else {
                sendGroupSetting.add(detail);
            }
        }
        log.info("UploadMid Size: {}, SendGroupSetting Size: {}", uploadMidSetting.size(), sendGroupSetting.size());

        /* Override List */
        sendGroupDetails = sendGroupSetting;

        //  驗證 queryOp，避免SQL攻擊(SQL injection)
        checkSendGroupDetail(sendGroupDetails);


        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" SELECT %s FROM (", selectColumns));

        log.info("UploadMid Size: {}, SendGroupSetting Size: {}", uploadMidSetting.size(), sendGroupSetting.size());
        if (!sendGroupDetails.isEmpty()) {
            sb.append(generateMidFieldSettingFrom(sendGroupDetails, 1));
        }

        log.info("UploadMid Size: {}, SendGroupSetting Size: {}", uploadMidSetting.size(), sendGroupSetting.size());
        // Setting Upload Mid SQL
        if (!uploadMidSetting.isEmpty()) {
            if (!sendGroupDetails.isEmpty()) {
                sb.append(", ");
                sb.append(generateUploadMidSettingFrom(uploadMidSetting, sendGroupDetails.size() * 2 + 1));
            } else {
                sb = new StringBuilder();
                sb.append(String.format(" SELECT %s FROM", selectColumns));
                sb.append(generateUploadMidSettingFrom(uploadMidSetting, 1));
                if (StringUtils.isNotBlank(mid)) {
                    sb.append(" WHERE MID = ?");
                    sb.append((uploadMidSetting.size() + 1));
                }
            }
        }
        log.info("UploadMid Size: {}, SendGroupSetting Size: {}", uploadMidSetting.size(), sendGroupSetting.size());

        // Setting Upload Mid SQL
        if (!uploadMidSetting.isEmpty()) {
            if (!sendGroupDetails.isEmpty()) {
                sb.append(" WHERE MID = EVENT_SET.MID ");

                if (StringUtils.isNotBlank(mid)) {
                    sb.append(" AND MID = ?");
                    sb.append((sendGroupDetails.size() * 2 + 1 + uploadMidSetting.size()));
                }
            }
        } else {
            if (!sendGroupDetails.isEmpty() && StringUtils.isNotBlank(mid)) {
                sb.append(" WHERE MID = ?");
                sb.append((sendGroupDetails.size() * 2 + 1));
                sb.append(" ");
            }
        }

        log.info(sb.toString());

        if (StringUtils.isBlank(sb)) {
            throw new Exception("SQL Error : Blank");
        }

        Query query = entityManager.createNativeQuery(sb.toString());
        query.setHint("javax.persistence.query.timeout", 300000);

        for (int i = 0; i < sendGroupDetails.size(); i++) {
            query.setParameter(2 * i + 1, sendGroupDetails.get(i).getQueryField());
            query.setParameter(2 * i + 2, sendGroupDetails.get(i).getQueryValue());
            log.info("setParameter Field:" + (2 * i + 1) + ", " + sendGroupDetails.get(i).getQueryField());
            log.info("setParameter Value:" + (2 * i + 2) + ", " + sendGroupDetails.get(i).getQueryValue());
        }

        // Setting Upload Mid Parameter
        if (!uploadMidSetting.isEmpty()) {

            for (int i = 0; i < uploadMidSetting.size(); i++) {
                String value = uploadMidSetting.get(i).getQueryValue();
                query.setParameter(sendGroupDetails.size() * 2 + i + 1, value.split(":")[0]);
                log.info("setParameter:" + (sendGroupDetails.size() * 2 + i + 1) + ", " + value.split(":")[0]);
            }
        }

        if (StringUtils.isNotBlank(mid)) {
            query.setParameter(sendGroupDetails.size() * 2 + uploadMidSetting.size() + 1, mid);
            log.info("setParameter:" + (sendGroupDetails.size() * 2 + uploadMidSetting.size() + 1) + ", " + mid);
        }

        return query;
    }

    /**
     * @param sendGroupDetails
     * @param params
     * @return SQL String
     */
    private String generateMidFieldSettingFrom(List<SendGroupDetail> sendGroupDetails, int params) {
        log.info("generateMidFieldSettingFrom start!!");
        if (sendGroupDetails == null || sendGroupDetails.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("( SELECT f.MID as MID FROM BCS_USER_FIELD_SET f ");

        sb.append(" INNER JOIN BCS_LINE_USER u ON u.MID = f.MID ");

        if (sendGroupDetails.size() > 1) {
            for (int i = 1; i < sendGroupDetails.size(); i++) {
                sb.append(String.format(" INNER JOIN BCS_USER_FIELD_SET f%d ON f.MID = f%d.MID ", i, i));
            }
        }

        SendGroupDetail detail = sendGroupDetails.get(0);
        sb.append(" WHERE f.KEY_DATA = ?" + params + " and f.VALUE " + detail.getQueryOp() + " ?" + (params + 1) + " ");
        sb.append(" AND u.status != 'BLOCK' and u.status != 'SYSADD' and (u.STATUS = 'BINDED' OR u.STATUS = 'UNBIND') ");

        if (sendGroupDetails.size() > 1) {
            for (int i = 1; i < sendGroupDetails.size(); i++) {
                detail = sendGroupDetails.get(i);
                sb.append(" AND f" + i + ".KEY_DATA = ?" + (2 * i + params) + " and f" + i + ".VALUE " + detail.getQueryOp() + " ?" + (2 * i + params + 1) + " ");
            }
        }

        sb.append(" ) AS FIELD_SET ");
        log.info("Result: {}", sb.toString());
        return sb.toString();
    }

    /**
     * 產生 Upload Mid Setting From SQL
     *
     * @param sendGroupDetails
     * @param params
     * @return
     */
    private String generateUploadMidSettingFrom(List<SendGroupDetail> sendGroupDetails, int params) {
        log.info("generateUploadMidSettingFrom Start!!");
        if (sendGroupDetails == null || sendGroupDetails.isEmpty()) {
            return null;
        }
        String sqlString =
                "( "
                        + " SELECT s.MID as MID"
                        + " FROM BCS_USER_EVENT_SET s ";

        sqlString += " INNER JOIN BCS_LINE_USER k ON k.MID = s.MID ";

        if (sendGroupDetails.size() > 1) {
            for (int i = 1; i < sendGroupDetails.size(); i++) {
                sqlString += " INNER JOIN BCS_USER_EVENT_SET s" + i + " ON s.MID = s" + i + ".MID ";
            }
        }

        sqlString += " WHERE s.REFERENCE_ID = ?" + params + " ";

        sqlString += " AND k.status != 'BLOCK' and k.status != 'SYSADD' and (k.STATUS = 'BINDED' OR k.STATUS = 'UNBIND') ";


        if (sendGroupDetails.size() > 1) {
            for (int i = 1; i < sendGroupDetails.size(); i++) {
                sqlString += " OR s" + i + ".REFERENCE_ID = ?" + (i + params) + " ";
            }
        }

        sqlString += " ) AS EVENT_SET ";
        log.info("Result: {}", sqlString);
        return sqlString;
    }

    /**
     * 驗證 queryField、queryOp，避免SQL攻擊(SQL injection)
     *
     * @param sendGroupDetails
     */
    private void checkSendGroupDetail(List<SendGroupDetail> sendGroupDetails) {
        for (SendGroupDetail sendGroupDetail : sendGroupDetails) {
            String queryOp = sendGroupDetail.getQueryOp();

            if (!validQueryOp.contains(queryOp)) {
                throw new IllegalArgumentException("queryOp is illegal! queryOp : " + queryOp);
            }
        }
    }
}
