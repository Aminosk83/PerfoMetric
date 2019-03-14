package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.ObjectifsuserEntity;

@Named
public class ObjectifsuserService extends BaseService<ObjectifsuserEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ObjectifsuserService(){
        super(ObjectifsuserEntity.class);
    }
    
    @Transactional
    public List<ObjectifsuserEntity> findAllObjectifsuserEntities() {
        
        return entityManager.createQuery("SELECT o FROM Objectifsuser o ", ObjectifsuserEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Objectifsuser o", Long.class).getSingleResult();
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(ObjectifsuserEntity objectifsuser) {

        /* This is called before a Objectifsuser is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.cutAllObjectifsuserIdObjectifssAssignments(objectifsuser);
        
        this.cutAllObjectifsuserIdPostessAssignments(objectifsuser);
        
    }

    // Remove all assignments from all idObjectifs a objectifsuser. Called before delete a objectifsuser.
    @Transactional
    private void cutAllObjectifsuserIdObjectifssAssignments(ObjectifsuserEntity objectifsuser) {
        entityManager
                .createQuery("UPDATE Objectifs c SET c.objectifsuser = NULL WHERE c.objectifsuser = :p")
                .setParameter("p", objectifsuser).executeUpdate();
    }
    
    // Remove all assignments from all idPostes a objectifsuser. Called before delete a objectifsuser.
    @Transactional
    private void cutAllObjectifsuserIdPostessAssignments(ObjectifsuserEntity objectifsuser) {
        entityManager
                .createQuery("UPDATE Postes c SET c.objectifsuser = NULL WHERE c.objectifsuser = :p")
                .setParameter("p", objectifsuser).executeUpdate();
    }
    
}
