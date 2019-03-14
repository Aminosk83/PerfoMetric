package org.applicationn.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.apache.commons.io.IOUtils;
import org.applicationn.domain.DepartementsEntity;
import org.applicationn.domain.PostesAttachment;
import org.applicationn.domain.PostesEntity;
import org.applicationn.domain.PostesImage;
import org.applicationn.service.DepartementsService;
import org.applicationn.service.PostesAttachmentService;
import org.applicationn.service.PostesService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@Named("postesBean")
@ViewScoped
public class PostesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(PostesBean.class.getName());
    
    private List<PostesEntity> postesList;

    private PostesEntity postes;
    
    private List<PostesAttachment> postesAttachments;
    
    @Inject
    private PostesService postesService;
    
    @Inject
    private PostesAttachmentService postesAttachmentService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    @Inject
    private DepartementsService departementsService;
    
    private DualListModel<DepartementsEntity> idDepartements;
    private List<String> transferedIdDepartementIDs;
    private List<String> removedIdDepartementIDs;
    
    public void prepareNewPostes() {
        reset();
        this.postes = new PostesEntity();
        // set any default values now, if you need
        // Example: this.postes.setAnything("test");
    }

    public String persist() {

        if (postes.getId() == null && !isPermitted("postes:create")) {
            return "accessDenied";
        } else if (postes.getId() != null && !isPermitted(postes, "postes:update")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            
            if (this.uploadedImage != null) {
                try {

                    BufferedImage image;
                    try (InputStream in = new ByteArrayInputStream(uploadedImageContents)) {
                        image = ImageIO.read(in);
                    }
                    image = Scalr.resize(image, Method.BALANCED, 300);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageOutputStream imageOS = ImageIO.createImageOutputStream(baos);
                    Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                            uploadedImage.getContentType());
                    ImageWriter imageWriter = (ImageWriter) imageWriters.next();
                    imageWriter.setOutput(imageOS);
                    imageWriter.write(image);
                    
                    baos.close();
                    imageOS.close();
                    
                    logger.log(Level.INFO, "Resized uploaded image from {0} to {1}", new Object[]{uploadedImageContents.length, baos.toByteArray().length});
            
                    PostesImage postesImage = new PostesImage();
                    postesImage.setContent(baos.toByteArray());
                    postes.setImage(postesImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (postes.getId() != null) {
                postes = postesService.update(postes);
                message = "message_successfully_updated";
            } else {
                postes = postesService.save(postes);
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
        
        postesList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(postes, "postes:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            postesService.delete(postes);
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
    
    public void onDialogOpen(PostesEntity postes) {
        reset();
        this.postes = postes;
    }
    
    public void reset() {
        postes = null;
        postesList = null;
        
        postesAttachments = null;
        
        idDepartements = null;
        transferedIdDepartementIDs = null;
        removedIdDepartementIDs = null;
        
        uploadedImage = null;
        uploadedImageContents = null;
        
    }

    public DualListModel<DepartementsEntity> getIdDepartements() {
        return idDepartements;
    }

    public void setIdDepartements(DualListModel<DepartementsEntity> departementss) {
        this.idDepartements = departementss;
    }
    
    public List<DepartementsEntity> getFullIdDepartementsList() {
        List<DepartementsEntity> allList = new ArrayList<>();
        allList.addAll(idDepartements.getSource());
        allList.addAll(idDepartements.getTarget());
        return allList;
    }
    
    public void onIdDepartementsDialog(PostesEntity postes) {
        // Prepare the idDepartement PickList
        this.postes = postes;
        List<DepartementsEntity> selectedDepartementssFromDB = departementsService
                .findIdDepartementsByPostes(this.postes);
        List<DepartementsEntity> availableDepartementssFromDB = departementsService
                .findAvailableIdDepartements(this.postes);
        this.idDepartements = new DualListModel<>(availableDepartementssFromDB, selectedDepartementssFromDB);
        
        transferedIdDepartementIDs = new ArrayList<>();
        removedIdDepartementIDs = new ArrayList<>();
    }
    
    public void onIdDepartementsPickListTransfer(TransferEvent event) {
        // If a idDepartement is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((DepartementsEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdDepartementIDs.add(id);
                removedIdDepartementIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdDepartementIDs.add(id);
                transferedIdDepartementIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdDepartement(DepartementsEntity departements) {
        // If a new idDepartement is created, we persist it to the database,
        // but we do not assign it to this postes in the database, yet.
        idDepartements.getTarget().add(departements);
        transferedIdDepartementIDs.add(departements.getId().toString());
    }
    
    public void onIdDepartementsSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<DepartementsEntity> selectedDepartementssFromDB = departementsService
                    .findIdDepartementsByPostes(this.postes);
            List<DepartementsEntity> availableDepartementssFromDB = departementsService
                    .findAvailableIdDepartements(this.postes);
            
            for (DepartementsEntity departements : selectedDepartementssFromDB) {
                if (removedIdDepartementIDs.contains(departements.getId().toString())) {
                    departements.setPostes(null);
                    departementsService.update(departements);
                }
            }
    
            for (DepartementsEntity departements : availableDepartementssFromDB) {
                if (transferedIdDepartementIDs.contains(departements.getId().toString())) {
                    departements.setPostes(postes);
                    departementsService.update(departements);
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
    
    public void handleImageUpload(FileUploadEvent event) {
        
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                event.getFile().getContentType());
        if (!imageWriters.hasNext()) {
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_image_type_not_supported");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }
        
        this.uploadedImage = event.getFile();
        this.uploadedImageContents = event.getFile().getContents();
        
        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public byte[] getUploadedImageContents() {
        if (uploadedImageContents != null) {
            return uploadedImageContents;
        } else if (postes != null && postes.getImage() != null) {
            postes = postesService.lazilyLoadImageToPostes(postes);
            return postes.getImage().getContent();
        }
        return null;
    }
    
    public List<PostesAttachment> getPostesAttachments() {
        if (this.postesAttachments == null && this.postes != null && this.postes.getId() != null) {
            // The byte streams are not loaded from database with following line. This would cost too much.
            this.postesAttachments = this.postesAttachmentService.getAttachmentsList(postes);
        }
        return this.postesAttachments;
    }
    
    public void handleAttachmentUpload(FileUploadEvent event) {
        
        PostesAttachment postesAttachment = new PostesAttachment();
        
        try {
            // Would be better to use ...getFile().getContents(), but does not work on every environment
            postesAttachment.setContent(IOUtils.toByteArray(event.getFile().getInputstream()));
        
            postesAttachment.setFileName(event.getFile().getFileName());
            postesAttachment.setPostes(postes);
            postesAttachmentService.save(postesAttachment);
            
            // set postesAttachment to null, will be refreshed on next demand
            this.postesAttachments = null;
            
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_successfully_uploaded");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_upload_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public StreamedContent getAttachmentStreamedContent(
            PostesAttachment postesAttachment) {
        if (postesAttachment != null && postesAttachment.getContent() == null) {
            postesAttachment = postesAttachmentService
                    .find(postesAttachment.getId());
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(
                postesAttachment.getContent()),
                new MimetypesFileTypeMap().getContentType(postesAttachment
                        .getFileName()), postesAttachment.getFileName());
    }

    public String deleteAttachment(PostesAttachment attachment) {
        
        postesAttachmentService.delete(attachment);
        
        // set postesAttachment to null, will be refreshed on next demand
        this.postesAttachments = null;
        
        FacesMessage facesMessage = MessageFactory.getMessage(
                "message_successfully_deleted", "Attachment");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }
    
    public PostesEntity getPostes() {
        if (this.postes == null) {
            prepareNewPostes();
        }
        return this.postes;
    }
    
    public void setPostes(PostesEntity postes) {
        this.postes = postes;
    }
    
    public List<PostesEntity> getPostesList() {
        if (postesList == null) {
            postesList = postesService.findAllPostesEntities();
        }
        return postesList;
    }

    public void setPostesList(List<PostesEntity> postesList) {
        this.postesList = postesList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(PostesEntity postes, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
