package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.PrimesEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class PrimesService extends BaseService<PrimesEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public PrimesService(){
        super(PrimesEntity.class);
    }
    
    @Transactional
    public List<PrimesEntity> findAllPrimesEntities() {
        
        return entityManager.createQuery("SELECT o FROM Primes o ", PrimesEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Primes o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public PrimesEntity save(PrimesEntity primes) {
        String username = SecurityWrapper.getUsername();
        
        primes.updateAuditInformation(username);
        
        return super.save(primes);
    }
    
    @Override
    @Transactional
    public PrimesEntity update(PrimesEntity primes) {
        String username = SecurityWrapper.getUsername();
        primes.updateAuditInformation(username);
        return super.update(primes);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(PrimesEntity primes) {

        /* This is called before a Primes is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.cutAllPrimesIdEmployeesAssignments(primes);
        
    }

    // Remove all assignments from all idEmployee a primes. Called before delete a primes.
    @Transactional
    private void cutAllPrimesIdEmployeesAssignments(PrimesEntity primes) {
        entityManager
                .createQuery("UPDATE Employees c SET c.primes = NULL WHERE c.primes = :p")
                .setParameter("p", primes).executeUpdate();
    }
    
}
