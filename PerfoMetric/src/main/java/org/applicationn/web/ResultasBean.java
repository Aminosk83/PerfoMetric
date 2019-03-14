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

import org.applicationn.domain.EmployeesEntity;
import org.applicationn.domain.ResultasEntity;
import org.applicationn.service.EmployeesService;
import org.applicationn.service.ResultasService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

@Named("resultasBean")
@ViewScoped
public class ResultasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ResultasBean.class.getName());
    
    private List<ResultasEntity> resultasList;

    private ResultasEntity resultas;
    
    @Inject
    private ResultasService resultasService;
    
    @Inject
    private EmployeesService employeesService;
    
    private DualListModel<EmployeesEntity> idemployees;
    private List<String> transferedIdemployeeIDs;
    private List<String> removedIdemployeeIDs;
    
    public void prepareNewResultas() {
        reset();
        this.resultas = new ResultasEntity();
        // set any default values now, if you need
        // Example: this.resultas.setAnything("test");
    }

    public String persist() {

        if (resultas.getId() == null && !isPermitted("resultas:create")) {
            return "accessDenied";
        } else if (resultas.getId() != null && !isPermitted(resultas, "resultas:update")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            
            if (resultas.getId() != null) {
                resultas = resultasService.update(resultas);
                message = "message_successfully_updated";
            } else {
                resultas = resultasService.save(resultas);
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
        
        resultasList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(resultas, "resultas:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            resultasService.delete(resultas);
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
    
    public void onDialogOpen(ResultasEntity resultas) {
        reset();
        this.resultas = resultas;
    }
    
    public void reset() {
        resultas = null;
        resultasList = null;
        
        idemployees = null;
        transferedIdemployeeIDs = null;
        removedIdemployeeIDs = null;
        
    }

    public DualListModel<EmployeesEntity> getIdemployees() {
        return idemployees;
    }

    public void setIdemployees(DualListModel<EmployeesEntity> employeess) {
        this.idemployees = employeess;
    }
    
    public List<EmployeesEntity> getFullIdemployeesList() {
        List<EmployeesEntity> allList = new ArrayList<>();
        allList.addAll(idemployees.getSource());
        allList.addAll(idemployees.getTarget());
        return allList;
    }
    
    public void onIdemployeesDialog(ResultasEntity resultas) {
        // Prepare the idemployee PickList
        this.resultas = resultas;
        List<EmployeesEntity> selectedEmployeessFromDB = employeesService
                .findIdemployeesByResultas(this.resultas);
        List<EmployeesEntity> availableEmployeessFromDB = employeesService
                .findAvailableIdemployees(this.resultas);
        this.idemployees = new DualListModel<>(availableEmployeessFromDB, selectedEmployeessFromDB);
        
        transferedIdemployeeIDs = new ArrayList<>();
        removedIdemployeeIDs = new ArrayList<>();
    }
    
    public void onIdemployeesPickListTransfer(TransferEvent event) {
        // If a idemployee is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((EmployeesEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdemployeeIDs.add(id);
                removedIdemployeeIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdemployeeIDs.add(id);
                transferedIdemployeeIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdemployee(EmployeesEntity employees) {
        // If a new idemployee is created, we persist it to the database,
        // but we do not assign it to this resultas in the database, yet.
        idemployees.getTarget().add(employees);
        transferedIdemployeeIDs.add(employees.getId().toString());
    }
    
    public void onIdemployeesSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<EmployeesEntity> selectedEmployeessFromDB = employeesService
                    .findIdemployeesByResultas(this.resultas);
            List<EmployeesEntity> availableEmployeessFromDB = employeesService
                    .findAvailableIdemployees(this.resultas);
            
            for (EmployeesEntity employees : selectedEmployeessFromDB) {
                if (removedIdemployeeIDs.contains(employees.getId().toString())) {
                    employees.setResultas(null);
                    employeesService.update(employees);
                }
            }
    
            for (EmployeesEntity employees : availableEmployeessFromDB) {
                if (transferedIdemployeeIDs.contains(employees.getId().toString())) {
                    employees.setResultas(resultas);
                    employeesService.update(employees);
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
    
    public ResultasEntity getResultas() {
        if (this.resultas == null) {
            prepareNewResultas();
        }
        return this.resultas;
    }
    
    public void setResultas(ResultasEntity resultas) {
        this.resultas = resultas;
    }
    
    public List<ResultasEntity> getResultasList() {
        if (resultasList == null) {
            resultasList = resultasService.findAllResultasEntities();
        }
        return resultasList;
    }

    public void setResultasList(List<ResultasEntity> resultasList) {
        this.resultasList = resultasList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(ResultasEntity resultas, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
