package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.ResultasEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class ResultasService extends BaseService<ResultasEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ResultasService(){
        super(ResultasEntity.class);
    }
    
    @Transactional
    public List<ResultasEntity> findAllResultasEntities() {
        
        return entityManager.createQuery("SELECT o FROM Resultas o ", ResultasEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Resultas o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public ResultasEntity save(ResultasEntity resultas) {
        String username = SecurityWrapper.getUsername();
        
        resultas.updateAuditInformation(username);
        
        return super.save(resultas);
    }
    
    @Override
    @Transactional
    public ResultasEntity update(ResultasEntity resultas) {
        String username = SecurityWrapper.getUsername();
        resultas.updateAuditInformation(username);
        return super.update(resultas);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(ResultasEntity resultas) {

        /* This is called before a Resultas is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.cutAllResultasIdemployeesAssignments(resultas);
        
    }

    // Remove all assignments from all idemployee a resultas. Called before delete a resultas.
    @Transactional
    private void cutAllResultasIdemployeesAssignments(ResultasEntity resultas) {
        entityManager
                .createQuery("UPDATE Employees c SET c.resultas = NULL WHERE c.resultas = :p")
                .setParameter("p", resultas).executeUpdate();
    }
    
}
