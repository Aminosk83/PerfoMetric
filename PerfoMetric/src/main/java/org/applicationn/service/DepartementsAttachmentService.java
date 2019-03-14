package org.applicationn.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.DepartementsAttachment;
import org.applicationn.domain.DepartementsEntity;

@Named
public class DepartementsAttachmentService extends BaseService<DepartementsAttachment> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public DepartementsAttachmentService(){
        super(DepartementsAttachment.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM DepartementsAttachment o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAttachmentsByDepartements(DepartementsEntity departements) {
        entityManager
                .createQuery("DELETE FROM DepartementsAttachment c WHERE c.departements = :p")
                .setParameter("p", departements).executeUpdate();
    }
    
    @Transactional
    public List<DepartementsAttachment> getAttachmentsList(DepartementsEntity departements) {
        if (departements == null || departements.getId() == null) {
            return new ArrayList<>();
        }
        // The byte streams are not loaded from database with following line. This would cost too much.
        return entityManager.createQuery("SELECT NEW org.applicationn.domain.DepartementsAttachment(o.id, o.fileName) FROM DepartementsAttachment o WHERE o.departements.id = :id", DepartementsAttachment.class).setParameter("id", departements.getId()).getResultList();
    }
}
