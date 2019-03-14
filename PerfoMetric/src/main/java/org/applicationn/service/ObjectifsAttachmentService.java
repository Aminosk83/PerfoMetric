package org.applicationn.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.ObjectifsAttachment;
import org.applicationn.domain.ObjectifsEntity;

@Named
public class ObjectifsAttachmentService extends BaseService<ObjectifsAttachment> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ObjectifsAttachmentService(){
        super(ObjectifsAttachment.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM ObjectifsAttachment o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAttachmentsByObjectifs(ObjectifsEntity objectifs) {
        entityManager
                .createQuery("DELETE FROM ObjectifsAttachment c WHERE c.objectifs = :p")
                .setParameter("p", objectifs).executeUpdate();
    }
    
    @Transactional
    public List<ObjectifsAttachment> getAttachmentsList(ObjectifsEntity objectifs) {
        if (objectifs == null || objectifs.getId() == null) {
            return new ArrayList<>();
        }
        // The byte streams are not loaded from database with following line. This would cost too much.
        return entityManager.createQuery("SELECT NEW org.applicationn.domain.ObjectifsAttachment(o.id, o.fileName) FROM ObjectifsAttachment o WHERE o.objectifs.id = :id", ObjectifsAttachment.class).setParameter("id", objectifs.getId()).getResultList();
    }
}
