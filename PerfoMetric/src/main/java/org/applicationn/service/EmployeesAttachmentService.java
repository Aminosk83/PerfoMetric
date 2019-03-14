package org.applicationn.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.EmployeesAttachment;
import org.applicationn.domain.EmployeesEntity;

@Named
public class EmployeesAttachmentService extends BaseService<EmployeesAttachment> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public EmployeesAttachmentService(){
        super(EmployeesAttachment.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM EmployeesAttachment o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAttachmentsByEmployees(EmployeesEntity employees) {
        entityManager
                .createQuery("DELETE FROM EmployeesAttachment c WHERE c.employees = :p")
                .setParameter("p", employees).executeUpdate();
    }
    
    @Transactional
    public List<EmployeesAttachment> getAttachmentsList(EmployeesEntity employees) {
        if (employees == null || employees.getId() == null) {
            return new ArrayList<>();
        }
        // The byte streams are not loaded from database with following line. This would cost too much.
        return entityManager.createQuery("SELECT NEW org.applicationn.domain.EmployeesAttachment(o.id, o.fileName) FROM EmployeesAttachment o WHERE o.employees.id = :id", EmployeesAttachment.class).setParameter("id", employees.getId()).getResultList();
    }
}
