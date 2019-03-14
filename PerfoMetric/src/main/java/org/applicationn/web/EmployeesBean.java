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
import org.applicationn.domain.EmployeesAttachment;
import org.applicationn.domain.EmployeesEntity;
import org.applicationn.domain.EmployeesImage;
import org.applicationn.domain.PostesEntity;
import org.applicationn.domain.security.UserEntity;
import org.applicationn.service.EmployeesAttachmentService;
import org.applicationn.service.EmployeesService;
import org.applicationn.service.PostesService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.service.security.UserService;
import org.applicationn.web.util.MessageFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@Named("employeesBean")
@ViewScoped
public class EmployeesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(EmployeesBean.class.getName());
    
    private List<EmployeesEntity> employeesList;

    private EmployeesEntity employees;
    
    private List<EmployeesAttachment> employeesAttachments;
    
    @Inject
    private EmployeesService employeesService;
    
    @Inject
    private EmployeesAttachmentService employeesAttachmentService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    @Inject
    private UserService userService;
    
    @Inject
    private PostesService postesService;
    
    private DualListModel<PostesEntity> idPostes;
    private List<String> transferedIdPosteIDs;
    private List<String> removedIdPosteIDs;
    
    private List<UserEntity> availableIdUserList;
    
    public void prepareNewEmployees() {
        reset();
        this.employees = new EmployeesEntity();
        // set any default values now, if you need
        // Example: this.employees.setAnything("test");
    }

    public String persist() {

        if (employees.getId() == null && !isPermitted("employees:create")) {
            return "accessDenied";
        } else if (employees.getId() != null && !isPermitted(employees, "employees:update")) {
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
            
                    EmployeesImage employeesImage = new EmployeesImage();
                    employeesImage.setContent(baos.toByteArray());
                    employees.setImage(employeesImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (employees.getId() != null) {
                employees = employeesService.update(employees);
                message = "message_successfully_updated";
            } else {
                employees = employeesService.save(employees);
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
        
        employeesList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(employees, "employees:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            employeesService.delete(employees);
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
    
    public void onDialogOpen(EmployeesEntity employees) {
        reset();
        this.employees = employees;
    }
    
    public void reset() {
        employees = null;
        employeesList = null;
        
        employeesAttachments = null;
        
        idPostes = null;
        transferedIdPosteIDs = null;
        removedIdPosteIDs = null;
        
        availableIdUserList = null;
        
        uploadedImage = null;
        uploadedImageContents = null;
        
    }

    // Get a List of all idUser
    public List<UserEntity> getAvailableIdUser() {
        if (this.availableIdUserList == null) {
            this.availableIdUserList = userService.findAvailableIdUser(this.employees);
        }
        return this.availableIdUserList;
    }
    
    // Update idUser of the current employees
    public void updateIdUser(UserEntity user) {
        this.employees.setIdUser(user);
        // Maybe we just created and assigned a new idUser. So reset the availableIdUserList.
        availableIdUserList = null;
    }
    
    public DualListModel<PostesEntity> getIdPostes() {
        return idPostes;
    }

    public void setIdPostes(DualListModel<PostesEntity> postess) {
        this.idPostes = postess;
    }
    
    public List<PostesEntity> getFullIdPostesList() {
        List<PostesEntity> allList = new ArrayList<>();
        allList.addAll(idPostes.getSource());
        allList.addAll(idPostes.getTarget());
        return allList;
    }
    
    public void onIdPostesDialog(EmployeesEntity employees) {
        // Prepare the idPoste PickList
        this.employees = employees;
        List<PostesEntity> selectedPostessFromDB = postesService
                .findIdPostesByEmployees(this.employees);
        List<PostesEntity> availablePostessFromDB = postesService
                .findAvailableIdPostes(this.employees);
        this.idPostes = new DualListModel<>(availablePostessFromDB, selectedPostessFromDB);
        
        transferedIdPosteIDs = new ArrayList<>();
        removedIdPosteIDs = new ArrayList<>();
    }
    
    public void onIdPostesPickListTransfer(TransferEvent event) {
        // If a idPoste is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((PostesEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdPosteIDs.add(id);
                removedIdPosteIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdPosteIDs.add(id);
                transferedIdPosteIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdPoste(PostesEntity postes) {
        // If a new idPoste is created, we persist it to the database,
        // but we do not assign it to this employees in the database, yet.
        idPostes.getTarget().add(postes);
        transferedIdPosteIDs.add(postes.getId().toString());
    }
    
    public void onIdPostesSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<PostesEntity> selectedPostessFromDB = postesService
                    .findIdPostesByEmployees(this.employees);
            List<PostesEntity> availablePostessFromDB = postesService
                    .findAvailableIdPostes(this.employees);
            
            for (PostesEntity postes : selectedPostessFromDB) {
                if (removedIdPosteIDs.contains(postes.getId().toString())) {
                    postes.setEmployees(null);
                    postesService.update(postes);
                }
            }
    
            for (PostesEntity postes : availablePostessFromDB) {
                if (transferedIdPosteIDs.contains(postes.getId().toString())) {
                    postes.setEmployees(employees);
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
        } else if (employees != null && employees.getImage() != null) {
            employees = employeesService.lazilyLoadImageToEmployees(employees);
            return employees.getImage().getContent();
        }
        return null;
    }
    
    public List<EmployeesAttachment> getEmployeesAttachments() {
        if (this.employeesAttachments == null && this.employees != null && this.employees.getId() != null) {
            // The byte streams are not loaded from database with following line. This would cost too much.
            this.employeesAttachments = this.employeesAttachmentService.getAttachmentsList(employees);
        }
        return this.employeesAttachments;
    }
    
    public void handleAttachmentUpload(FileUploadEvent event) {
        
        EmployeesAttachment employeesAttachment = new EmployeesAttachment();
        
        try {
            // Would be better to use ...getFile().getContents(), but does not work on every environment
            employeesAttachment.setContent(IOUtils.toByteArray(event.getFile().getInputstream()));
        
            employeesAttachment.setFileName(event.getFile().getFileName());
            employeesAttachment.setEmployees(employees);
            employeesAttachmentService.save(employeesAttachment);
            
            // set employeesAttachment to null, will be refreshed on next demand
            this.employeesAttachments = null;
            
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
            EmployeesAttachment employeesAttachment) {
        if (employeesAttachment != null && employeesAttachment.getContent() == null) {
            employeesAttachment = employeesAttachmentService
                    .find(employeesAttachment.getId());
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(
                employeesAttachment.getContent()),
                new MimetypesFileTypeMap().getContentType(employeesAttachment
                        .getFileName()), employeesAttachment.getFileName());
    }

    public String deleteAttachment(EmployeesAttachment attachment) {
        
        employeesAttachmentService.delete(attachment);
        
        // set employeesAttachment to null, will be refreshed on next demand
        this.employeesAttachments = null;
        
        FacesMessage facesMessage = MessageFactory.getMessage(
                "message_successfully_deleted", "Attachment");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }
    
    public EmployeesEntity getEmployees() {
        if (this.employees == null) {
            prepareNewEmployees();
        }
        return this.employees;
    }
    
    public void setEmployees(EmployeesEntity employees) {
        this.employees = employees;
    }
    
    public List<EmployeesEntity> getEmployeesList() {
        if (employeesList == null) {
            employeesList = employeesService.findAllEmployeesEntities();
        }
        return employeesList;
    }

    public void setEmployeesList(List<EmployeesEntity> employeesList) {
        this.employeesList = employeesList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(EmployeesEntity employees, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
