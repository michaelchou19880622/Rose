package com.bcs.web.ui.service;

import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ???
 */
@Slf4j
@Service
public class PNPMaintainUIService {

    private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository;
    private PNPMaintainAccountModelCustom pnpMaintainAccountModelCustom;

    @Autowired
    public PNPMaintainUIService(PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository,
                                PNPMaintainAccountModelCustom pnpMaintainAccountModelCustom) {
        this.pnpMaintainAccountModelRepository = pnpMaintainAccountModelRepository;
        this.pnpMaintainAccountModelCustom = pnpMaintainAccountModelCustom;

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
}
