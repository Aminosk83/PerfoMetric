package org.applicationn.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
import org.applicationn.domain.ObjectifsAttachment;
import org.applicationn.domain.ObjectifsEntity;
import org.applicationn.domain.ObjectifsImage;
import org.applicationn.service.ObjectifsAttachmentService;
import org.applicationn.service.ObjectifsService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@Named("objectifsBean")
@ViewScoped
public class ObjectifsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ObjectifsBean.class.getName());
    
    private List<ObjectifsEntity> objectifsList;

    private ObjectifsEntity objectifs;
    
    private List<ObjectifsAttachment> objectifsAttachments;
    
    @Inject
    private ObjectifsService objectifsService;
    
    @Inject
    private ObjectifsAttachmentService objectifsAttachmentService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    public void prepareNewObjectifs() {
        reset();
        this.objectifs = new ObjectifsEntity();
        // set any default values now, if you need
        // Example: this.objectifs.setAnything("test");
    }

    public String persist() {

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
            
                    ObjectifsImage objectifsImage = new ObjectifsImage();
                    objectifsImage.setContent(baos.toByteArray());
                    objectifs.setImage(objectifsImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (objectifs.getId() != null) {
                objectifs = objectifsService.update(objectifs);
                message = "message_successfully_updated";
            } else {
                objectifs = objectifsService.save(objectifs);
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
        
        objectifsList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        String message;
        
        try {
            objectifsService.delete(objectifs);
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
    
    public void onDialogOpen(ObjectifsEntity objectifs) {
        reset();
        this.objectifs = objectifs;
    }
    
    public void reset() {
        objectifs = null;
        objectifsList = null;
        
        objectifsAttachments = null;
        
        uploadedImage = null;
        uploadedImageContents = null;
        
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
        } else if (objectifs != null && objectifs.getImage() != null) {
            objectifs = objectifsService.lazilyLoadImageToObjectifs(objectifs);
            return objectifs.getImage().getContent();
        }
        return null;
    }
    
    public List<ObjectifsAttachment> getObjectifsAttachments() {
        if (this.objectifsAttachments == null && this.objectifs != null && this.objectifs.getId() != null) {
            // The byte streams are not loaded from database with following line. This would cost too much.
            this.objectifsAttachments = this.objectifsAttachmentService.getAttachmentsList(objectifs);
        }
        return this.objectifsAttachments;
    }
    
    public void handleAttachmentUpload(FileUploadEvent event) {
        
        ObjectifsAttachment objectifsAttachment = new ObjectifsAttachment();
        
        try {
            // Would be better to use ...getFile().getContents(), but does not work on every environment
            objectifsAttachment.setContent(IOUtils.toByteArray(event.getFile().getInputstream()));
        
            objectifsAttachment.setFileName(event.getFile().getFileName());
            objectifsAttachment.setObjectifs(objectifs);
            objectifsAttachmentService.save(objectifsAttachment);
            
            // set objectifsAttachment to null, will be refreshed on next demand
            this.objectifsAttachments = null;
            
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
            ObjectifsAttachment objectifsAttachment) {
        if (objectifsAttachment != null && objectifsAttachment.getContent() == null) {
            objectifsAttachment = objectifsAttachmentService
                    .find(objectifsAttachment.getId());
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(
                objectifsAttachment.getContent()),
                new MimetypesFileTypeMap().getContentType(objectifsAttachment
                        .getFileName()), objectifsAttachment.getFileName());
    }

    public String deleteAttachment(ObjectifsAttachment attachment) {
        
        objectifsAttachmentService.delete(attachment);
        
        // set objectifsAttachment to null, will be refreshed on next demand
        this.objectifsAttachments = null;
        
        FacesMessage facesMessage = MessageFactory.getMessage(
                "message_successfully_deleted", "Attachment");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }
    
    public ObjectifsEntity getObjectifs() {
        if (this.objectifs == null) {
            prepareNewObjectifs();
        }
        return this.objectifs;
    }
    
    public void setObjectifs(ObjectifsEntity objectifs) {
        this.objectifs = objectifs;
    }
    
    public List<ObjectifsEntity> getObjectifsList() {
        if (objectifsList == null) {
            objectifsList = objectifsService.findAllObjectifsEntities();
        }
        return objectifsList;
    }

    public void setObjectifsList(List<ObjectifsEntity> objectifsList) {
        this.objectifsList = objectifsList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(ObjectifsEntity objectifs, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
