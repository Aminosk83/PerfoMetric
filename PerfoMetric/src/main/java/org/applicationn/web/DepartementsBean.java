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
import org.applicationn.domain.DepartementsAttachment;
import org.applicationn.domain.DepartementsEntity;
import org.applicationn.domain.DepartementsImage;
import org.applicationn.service.DepartementsAttachmentService;
import org.applicationn.service.DepartementsService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@Named("departementsBean")
@ViewScoped
public class DepartementsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(DepartementsBean.class.getName());
    
    private List<DepartementsEntity> departementsList;

    private DepartementsEntity departements;
    
    private List<DepartementsAttachment> departementsAttachments;
    
    @Inject
    private DepartementsService departementsService;
    
    @Inject
    private DepartementsAttachmentService departementsAttachmentService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    public void prepareNewDepartements() {
        reset();
        this.departements = new DepartementsEntity();
        // set any default values now, if you need
        // Example: this.departements.setAnything("test");
    }

    public String persist() {

        if (departements.getId() == null && !isPermitted("departements:create")) {
            return "accessDenied";
        } else if (departements.getId() != null && !isPermitted(departements, "departements:update")) {
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
            
                    DepartementsImage departementsImage = new DepartementsImage();
                    departementsImage.setContent(baos.toByteArray());
                    departements.setImage(departementsImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (departements.getId() != null) {
                departements = departementsService.update(departements);
                message = "message_successfully_updated";
            } else {
                departements = departementsService.save(departements);
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
        
        departementsList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(departements, "departements:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            departementsService.delete(departements);
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
    
    public void onDialogOpen(DepartementsEntity departements) {
        reset();
        this.departements = departements;
    }
    
    public void reset() {
        departements = null;
        departementsList = null;
        
        departementsAttachments = null;
        
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
        } else if (departements != null && departements.getImage() != null) {
            departements = departementsService.lazilyLoadImageToDepartements(departements);
            return departements.getImage().getContent();
        }
        return null;
    }
    
    public List<DepartementsAttachment> getDepartementsAttachments() {
        if (this.departementsAttachments == null && this.departements != null && this.departements.getId() != null) {
            // The byte streams are not loaded from database with following line. This would cost too much.
            this.departementsAttachments = this.departementsAttachmentService.getAttachmentsList(departements);
        }
        return this.departementsAttachments;
    }
    
    public void handleAttachmentUpload(FileUploadEvent event) {
        
        DepartementsAttachment departementsAttachment = new DepartementsAttachment();
        
        try {
            // Would be better to use ...getFile().getContents(), but does not work on every environment
            departementsAttachment.setContent(IOUtils.toByteArray(event.getFile().getInputstream()));
        
            departementsAttachment.setFileName(event.getFile().getFileName());
            departementsAttachment.setDepartements(departements);
            departementsAttachmentService.save(departementsAttachment);
            
            // set departementsAttachment to null, will be refreshed on next demand
            this.departementsAttachments = null;
            
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
            DepartementsAttachment departementsAttachment) {
        if (departementsAttachment != null && departementsAttachment.getContent() == null) {
            departementsAttachment = departementsAttachmentService
                    .find(departementsAttachment.getId());
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(
                departementsAttachment.getContent()),
                new MimetypesFileTypeMap().getContentType(departementsAttachment
                        .getFileName()), departementsAttachment.getFileName());
    }

    public String deleteAttachment(DepartementsAttachment attachment) {
        
        departementsAttachmentService.delete(attachment);
        
        // set departementsAttachment to null, will be refreshed on next demand
        this.departementsAttachments = null;
        
        FacesMessage facesMessage = MessageFactory.getMessage(
                "message_successfully_deleted", "Attachment");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }
    
    public DepartementsEntity getDepartements() {
        if (this.departements == null) {
            prepareNewDepartements();
        }
        return this.departements;
    }
    
    public void setDepartements(DepartementsEntity departements) {
        this.departements = departements;
    }
    
    public List<DepartementsEntity> getDepartementsList() {
        if (departementsList == null) {
            departementsList = departementsService.findAllDepartementsEntities();
        }
        return departementsList;
    }

    public void setDepartementsList(List<DepartementsEntity> departementsList) {
        this.departementsList = departementsList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(DepartementsEntity departements, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
