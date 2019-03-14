package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Transactional;

import org.applicationn.domain.DepartementsEntity;
import org.applicationn.domain.PostesEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class DepartementsService extends BaseService<DepartementsEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public DepartementsService(){
        super(DepartementsEntity.class);
    }
    
    @Inject
    private DepartementsAttachmentService attachmentService;
    
    @Transactional
    public List<DepartementsEntity> findAllDepartementsEntities() {
        
        return entityManager.createQuery("SELECT o FROM Departements o LEFT JOIN FETCH o.image", DepartementsEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Departements o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public DepartementsEntity save(DepartementsEntity departements) {
        String username = SecurityWrapper.getUsername();
        
        departements.updateAuditInformation(username);
        
        return super.save(departements);
    }
    
    @Override
    @Transactional
    public DepartementsEntity update(DepartementsEntity departements) {
        String username = SecurityWrapper.getUsername();
        departements.updateAuditInformation(username);
        return super.update(departements);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(DepartementsEntity departements) {

        /* This is called before a Departements is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.attachmentService.deleteAttachmentsByDepartements(departements);
        
    }

    @Transactional
    public List<DepartementsEntity> findAvailableIdDepartements(PostesEntity postes) {
        return entityManager.createQuery("SELECT o FROM Departements o WHERE o.postes IS NULL", DepartementsEntity.class).getResultList();
    }

    @Transactional
    public List<DepartementsEntity> findIdDepartementsByPostes(PostesEntity postes) {
        return entityManager.createQuery("SELECT o FROM Departements o WHERE o.postes = :postes", DepartementsEntity.class).setParameter("postes", postes).getResultList();
    }

    @Transactional
    public DepartementsEntity lazilyLoadImageToDepartements(DepartementsEntity departements) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(departements, "image") && departements.getId() != null) {
            departements = find(departements.getId());
            departements.getImage().getId();
        }
        return departements;
    }
    
}
