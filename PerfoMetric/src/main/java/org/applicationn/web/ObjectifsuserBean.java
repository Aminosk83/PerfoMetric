package org.applicationn.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.applicationn.domain.ObjectifsEntity;
import org.applicationn.domain.ObjectifsuserEntity;
import org.applicationn.domain.PostesEntity;
import org.applicationn.service.ObjectifsService;
import org.applicationn.service.ObjectifsuserService;
import org.applicationn.service.PostesService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

@Named("objectifsuserBean")
@ViewScoped
public class ObjectifsuserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ObjectifsuserBean.class.getName());
    
    private List<ObjectifsuserEntity> objectifsuserList;

    private ObjectifsuserEntity objectifsuser;
    
    @Inject
    private ObjectifsuserService objectifsuserService;
    
    @Inject
    private ObjectifsService objectifsService;
    
    @Inject
    private PostesService postesService;
    
    private DualListModel<ObjectifsEntity> idObjectifss;
    private List<String> transferedIdObjectifsIDs;
    private List<String> removedIdObjectifsIDs;
    
    private DualListModel<PostesEntity> idPostess;
    private List<String> transferedIdPostesIDs;
    private List<String> removedIdPostesIDs;
    
    public void prepareNewObjectifsuser() {
        reset();
        this.objectifsuser = new ObjectifsuserEntity();
        // set any default values now, if you need
        // Example: this.objectifsuser.setAnything("test");
    }

    public String persist() {

        String message;
        
        try {
            
            if (objectifsuser.getId() != null) {
                objectifsuser = objectifsuserService.update(objectifsuser);
                message = "message_successfully_updated";
            } else {
                objectifsuser = objectifsuserService.save(objectifsuser);
                message = "message_successfully_created";
            }
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_optimistic_locking_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_save_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        
        objectifsuserList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        String message;
        
        try {
            objectifsuserService.delete(objectifsuser);
            message = "message_successfully_deleted";
            reset();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_delete_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        FacesContext.getCurrentInstance().addMessage(null, MessageFactory.getMessage(message));
        
        return null;
    }
    
    public void onDialogOpen(ObjectifsuserEntity objectifsuser) {
        reset();
        this.objectifsuser = objectifsuser;
    }
    
    public void reset() {
        objectifsuser = null;
        objectifsuserList = null;
        
        idObjectifss = null;
        transferedIdObjectifsIDs = null;
        removedIdObjectifsIDs = null;
        
        idPostess = null;
        transferedIdPostesIDs = null;
        removedIdPostesIDs = null;
        
    }

    public DualListModel<ObjectifsEntity> getIdObjectifss() {
        return idObjectifss;
    }

    public void setIdObjectifss(DualListModel<ObjectifsEntity> objectifss) {
        this.idObjectifss = objectifss;
    }
    
    public List<ObjectifsEntity> getFullIdObjectifssList() {
        List<ObjectifsEntity> allList = new ArrayList<>();
        allList.addAll(idObjectifss.getSource());
        allList.addAll(idObjectifss.getTarget());
        return allList;
    }
    
    public void onIdObjectifssDialog(ObjectifsuserEntity objectifsuser) {
        // Prepare the idObjectifs PickList
        this.objectifsuser = objectifsuser;
        List<ObjectifsEntity> selectedObjectifssFromDB = objectifsService
                .findIdObjectifssByObjectifsuser(this.objectifsuser);
        List<ObjectifsEntity> availableObjectifssFromDB = objectifsService
                .findAvailableIdObjectifss(this.objectifsuser);
        this.idObjectifss = new DualListModel<>(availableObjectifssFromDB, selectedObjectifssFromDB);
        
        transferedIdObjectifsIDs = new ArrayList<>();
        removedIdObjectifsIDs = new ArrayList<>();
    }
    
    public void onIdObjectifssPickListTransfer(TransferEvent event) {
        // If a idObjectifs is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((ObjectifsEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdObjectifsIDs.add(id);
                removedIdObjectifsIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdObjectifsIDs.add(id);
                transferedIdObjectifsIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdObjectifs(ObjectifsEntity objectifs) {
        // If a new idObjectifs is created, we persist it to the database,
        // but we do not assign it to this objectifsuser in the database, yet.
        idObjectifss.getTarget().add(objectifs);
        transferedIdObjectifsIDs.add(objectifs.getId().toString());
    }
    
    public DualListModel<PostesEntity> getIdPostess() {
        return idPostess;
    }

    public void setIdPostess(DualListModel<PostesEntity> postess) {
        this.idPostess = postess;
    }
    
    public List<PostesEntity> getFullIdPostessList() {
        List<PostesEntity> allList = new ArrayList<>();
        allList.addAll(idPostess.getSource());
        allList.addAll(idPostess.getTarget());
        return allList;
    }
    
    public void onIdPostessDialog(ObjectifsuserEntity objectifsuser) {
        // Prepare the idPostes PickList
        this.objectifsuser = objectifsuser;
        List<PostesEntity> selectedPostessFromDB = postesService
                .findIdPostessByObjectifsuser(this.objectifsuser);
        List<PostesEntity> availablePostessFromDB = postesService
                .findAvailableIdPostess(this.objectifsuser);
        this.idPostess = new DualListModel<>(availablePostessFromDB, selectedPostessFromDB);
        
        transferedIdPostesIDs = new ArrayList<>();
        removedIdPostesIDs = new ArrayList<>();
    }
    
    public void onIdPostessPickListTransfer(TransferEvent event) {
        // If a idPostes is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((PostesEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdPostesIDs.add(id);
                removedIdPostesIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdPostesIDs.add(id);
                transferedIdPostesIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdPostes(PostesEntity postes) {
        // If a new idPostes is created, we persist it to the database,
        // but we do not assign it to this objectifsuser in the database, yet.
        idPostess.getTarget().add(postes);
        transferedIdPostesIDs.add(postes.getId().toString());
    }
    
    public void onIdObjectifssSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<ObjectifsEntity> selectedObjectifssFromDB = objectifsService
                    .findIdObjectifssByObjectifsuser(this.objectifsuser);
            List<ObjectifsEntity> availableObjectifssFromDB = objectifsService
                    .findAvailableIdObjectifss(this.objectifsuser);
            
            for (ObjectifsEntity objectifs : selectedObjectifssFromDB) {
                if (removedIdObjectifsIDs.contains(objectifs.getId().toString())) {
                    objectifs.setObjectifsuser(null);
                    objectifsService.update(objectifs);
                }
            }
    
            for (ObjectifsEntity objectifs : availableObjectifssFromDB) {
                if (transferedIdObjectifsIDs.contains(objectifs.getId().toString())) {
                    objectifs.setObjectifsuser(objectifsuser);
                    objectifsService.update(objectifs);
                }
            }
            
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_changes_saved");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            
            reset();

        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_optimistic_locking_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_picklist_save_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
    }
    
    public void onIdPostessSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<PostesEntity> selectedPostessFromDB = postesService
                    .findIdPostessByObjectifsuser(this.objectifsuser);
            List<PostesEntity> availablePostessFromDB = postesService
                    .findAvailableIdPostess(this.objectifsuser);
            
            for (PostesEntity postes : selectedPostessFromDB) {
                if (removedIdPostesIDs.contains(postes.getId().toString())) {
                    postes.setObjectifsuser(null);
                    postesService.update(postes);
                }
            }
    
            for (PostesEntity postes : availablePostessFromDB) {
                if (transferedIdPostesIDs.contains(postes.getId().toString())) {
                    postes.setObjectifsuser(objectifsuser);
                    postesService.update(postes);
                }
            }
            
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_changes_saved");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            
            reset();

        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_optimistic_locking_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_picklist_save_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
    }
    
    public ObjectifsuserEntity getObjectifsuser() {
        if (this.objectifsuser == null) {
            prepareNewObjectifsuser();
        }
        return this.objectifsuser;
    }
    
    public void setObjectifsuser(ObjectifsuserEntity objectifsuser) {
        this.objectifsuser = objectifsuser;
    }
    
    public List<ObjectifsuserEntity> getObjectifsuserList() {
        if (objectifsuserList == null) {
            objectifsuserList = objectifsuserService.findAllObjectifsuserEntities();
        }
        return objectifsuserList;
    }

    public void setObjectifsuserList(List<ObjectifsuserEntity> objectifsuserList) {
        this.objectifsuserList = objectifsuserList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(ObjectifsuserEntity objectifsuser, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
