package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The interface Pnp maintain account model repository.
 *
 * @author ???
 */
@Repository
public interface PNPMaintainAccountModelRepository extends EntityRepository<PNPMaintainAccountModel, Long> {
    /**
     * Find by division name and department name and group name and pcc code and account and employee id and account type list.
     *
     * @param divisionName   the division name
     * @param departmentName the department name
     * @param groupName      the group name
     * @param pccCode        the pcc code
     * @param account        the account
     * @param employeeId     the employee id
     * @param accountType    the account type
     * @return the list
     */
    List<PNPMaintainAccountModel> findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType);

    /**
     * Find by account and source system and pnp content list.
     *
     * @param account      the account
     * @param sourceSystem the source system
     * @param pnpContent   the pnp content
     * @return the list
     */
    List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent);

    /**
     * Find by account and source system list.
     *
     * @param account      the account
     * @param sourceSystem the source system
     * @return the list
     */
    List<PNPMaintainAccountModel> findByAccountAndSourceSystem(String account, String sourceSystem);

    /**
     * Find by account and source system and status list.
     *
     * @param account      the account
     * @param sourceSystem the source system
     * @param status       the status
     * @return the list
     */
    List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndStatus(String account, String sourceSystem, boolean status);

    /**
     * Find by account type list.
     *
     * @param accountType the account type
     * @return the list
     */
    @Transactional(timeout = 30)
    @Query("select x from PNPMaintainAccountModel x where x.accountType = ?1 order by x.modifyTime desc")
    List<PNPMaintainAccountModel> findByAccountType(String accountType);

}
