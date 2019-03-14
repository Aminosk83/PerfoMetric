package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Transactional;

import org.applicationn.domain.EmployeesEntity;
import org.applicationn.domain.ObjectifsuserEntity;
import org.applicationn.domain.PostesEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class PostesService extends BaseService<PostesEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public PostesService(){
        super(PostesEntity.class);
    }
    
    @Inject
    private PostesAttachmentService attachmentService;
    
    @Transactional
    public List<PostesEntity> findAllPostesEntities() {
        
        return entityManager.createQuery("SELECT o FROM Postes o LEFT JOIN FETCH o.image", PostesEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Postes o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public PostesEntity save(PostesEntity postes) {
        String username = SecurityWrapper.getUsername();
        
        postes.updateAuditInformation(username);
        
        return super.save(postes);
    }
    
    @Override
    @Transactional
    public PostesEntity update(PostesEntity postes) {
        String username = SecurityWrapper.getUsername();
        postes.updateAuditInformation(username);
        return super.update(postes);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(PostesEntity postes) {

        /* This is called before a Postes is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.attachmentService.deleteAttachmentsByPostes(postes);
        
        this.cutAllPostesIdDepartementsAssignments(postes);
        
    }

    // Remove all assignments from all idDepartement a postes. Called before delete a postes.
    @Transactional
    private void cutAllPostesIdDepartementsAssignments(PostesEntity postes) {
        entityManager
                .createQuery("UPDATE Departements c SET c.postes = NULL WHERE c.postes = :p")
                .setParameter("p", postes).executeUpdate();
    }
    
    @Transactional
    public List<PostesEntity> findAvailableIdPostes(EmployeesEntity employees) {
        return entityManager.createQuery("SELECT o FROM Postes o WHERE o.employees IS NULL", PostesEntity.class).getResultList();
    }

    @Transactional
    public List<PostesEntity> findIdPostesByEmployees(EmployeesEntity employees) {
        return entityManager.createQuery("SELECT o FROM Postes o WHERE o.employees = :employees", PostesEntity.class).setParameter("employees", employees).getResultList();
    }

    @Transactional
    public List<PostesEntity> findAvailableIdPostess(ObjectifsuserEntity objectifsuser) {
        return entityManager.createQuery("SELECT o FROM Postes o WHERE o.objectifsuser IS NULL", PostesEntity.class).getResultList();
    }

    @Transactional
    public List<PostesEntity> findIdPostessByObjectifsuser(ObjectifsuserEntity objectifsuser) {
        return entityManager.createQuery("SELECT o FROM Postes o WHERE o.objectifsuser = :objectifsuser", PostesEntity.class).setParameter("objectifsuser", objectifsuser).getResultList();
    }

    @Transactional
    public PostesEntity lazilyLoadImageToPostes(PostesEntity postes) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(postes, "image") && postes.getId() != null) {
            postes = find(postes.getId());
            postes.getImage().getId();
        }
        return postes;
    }
    
}
