package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Transactional;

import org.applicationn.domain.ObjectifsEntity;
import org.applicationn.domain.ObjectifsuserEntity;

@Named
public class ObjectifsService extends BaseService<ObjectifsEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ObjectifsService(){
        super(ObjectifsEntity.class);
    }
    
    @Inject
    private ObjectifsAttachmentService attachmentService;
    
    @Transactional
    public List<ObjectifsEntity> findAllObjectifsEntities() {
        
        return entityManager.createQuery("SELECT o FROM Objectifs o LEFT JOIN FETCH o.image", ObjectifsEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Objectifs o", Long.class).getSingleResult();
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(ObjectifsEntity objectifs) {

        /* This is called before a Objectifs is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.attachmentService.deleteAttachmentsByObjectifs(objectifs);
        
    }

    @Transactional
    public List<ObjectifsEntity> findAvailableIdObjectifss(ObjectifsuserEntity objectifsuser) {
        return entityManager.createQuery("SELECT o FROM Objectifs o WHERE o.objectifsuser IS NULL", ObjectifsEntity.class).getResultList();
    }

    @Transactional
    public List<ObjectifsEntity> findIdObjectifssByObjectifsuser(ObjectifsuserEntity objectifsuser) {
        return entityManager.createQuery("SELECT o FROM Objectifs o WHERE o.objectifsuser = :objectifsuser", ObjectifsEntity.class).setParameter("objectifsuser", objectifsuser).getResultList();
    }

    @Transactional
    public ObjectifsEntity lazilyLoadImageToObjectifs(ObjectifsEntity objectifs) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(objectifs, "image") && objectifs.getId() != null) {
            objectifs = find(objectifs.getId());
            objectifs.getImage().getId();
        }
        return objectifs;
    }
    
}
