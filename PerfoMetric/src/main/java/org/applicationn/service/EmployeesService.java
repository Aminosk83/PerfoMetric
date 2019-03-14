package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Transactional;

import org.applicationn.domain.EmployeesEntity;
import org.applicationn.domain.PrimesEntity;
import org.applicationn.domain.ResultasEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class EmployeesService extends BaseService<EmployeesEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public EmployeesService(){
        super(EmployeesEntity.class);
    }
    
    @Inject
    private EmployeesAttachmentService attachmentService;
    
    @Transactional
    public List<EmployeesEntity> findAllEmployeesEntities() {
        
        return entityManager.createQuery("SELECT o FROM Employees o LEFT JOIN FETCH o.image", EmployeesEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Employees o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public EmployeesEntity save(EmployeesEntity employees) {
        String username = SecurityWrapper.getUsername();
        
        employees.updateAuditInformation(username);
        
        return super.save(employees);
    }
    
    @Override
    @Transactional
    public EmployeesEntity update(EmployeesEntity employees) {
        String username = SecurityWrapper.getUsername();
        employees.updateAuditInformation(username);
        return super.update(employees);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(EmployeesEntity employees) {

        /* This is called before a Employees is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.attachmentService.deleteAttachmentsByEmployees(employees);
        
        this.cutAllEmployeesIdPostesAssignments(employees);
        
    }

    // Remove all assignments from all idPoste a employees. Called before delete a employees.
    @Transactional
    private void cutAllEmployeesIdPostesAssignments(EmployeesEntity employees) {
        entityManager
                .createQuery("UPDATE Postes c SET c.employees = NULL WHERE c.employees = :p")
                .setParameter("p", employees).executeUpdate();
    }
    
    @Transactional
    public List<EmployeesEntity> findAvailableIdEmployees(PrimesEntity primes) {
        return entityManager.createQuery("SELECT o FROM Employees o WHERE o.primes IS NULL", EmployeesEntity.class).getResultList();
    }

    @Transactional
    public List<EmployeesEntity> findIdEmployeesByPrimes(PrimesEntity primes) {
        return entityManager.createQuery("SELECT o FROM Employees o WHERE o.primes = :primes", EmployeesEntity.class).setParameter("primes", primes).getResultList();
    }

    @Transactional
    public List<EmployeesEntity> findAvailableIdemployees(ResultasEntity resultas) {
        return entityManager.createQuery("SELECT o FROM Employees o WHERE o.resultas IS NULL", EmployeesEntity.class).getResultList();
    }

    @Transactional
    public List<EmployeesEntity> findIdemployeesByResultas(ResultasEntity resultas) {
        return entityManager.createQuery("SELECT o FROM Employees o WHERE o.resultas = :resultas", EmployeesEntity.class).setParameter("resultas", resultas).getResultList();
    }

    @Transactional
    public EmployeesEntity lazilyLoadImageToEmployees(EmployeesEntity employees) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(employees, "image") && employees.getId() != null) {
            employees = find(employees.getId());
            employees.getImage().getId();
        }
        return employees;
    }
    
}
