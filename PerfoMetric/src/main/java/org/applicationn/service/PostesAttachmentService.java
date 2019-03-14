package org.applicationn.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.PostesAttachment;
import org.applicationn.domain.PostesEntity;

@Named
public class PostesAttachmentService extends BaseService<PostesAttachment> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public PostesAttachmentService(){
        super(PostesAttachment.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM PostesAttachment o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAttachmentsByPostes(PostesEntity postes) {
        entityManager
                .createQuery("DELETE FROM PostesAttachment c WHERE c.postes = :p")
                .setParameter("p", postes).executeUpdate();
    }
    
    @Transactional
    public List<PostesAttachment> getAttachmentsList(PostesEntity postes) {
        if (postes == null || postes.getId() == null) {
            return new ArrayList<>();
        }
        // The byte streams are not loaded from database with following line. This would cost too much.
        return entityManager.createQuery("SELECT NEW org.applicationn.domain.PostesAttachment(o.id, o.fileName) FROM PostesAttachment o WHERE o.postes.id = :id", PostesAttachment.class).setParameter("id", postes.getId()).getResultList();
    }
}
