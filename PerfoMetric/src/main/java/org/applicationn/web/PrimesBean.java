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
import org.applicationn.domain.PrimesEntity;
import org.applicationn.service.EmployeesService;
import org.applicationn.service.PrimesService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

@Named("primesBean")
@ViewScoped
public class PrimesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(PrimesBean.class.getName());
    
    private List<PrimesEntity> primesList;

    private PrimesEntity primes;
    
    @Inject
    private PrimesService primesService;
    
    @Inject
    private EmployeesService employeesService;
    
    private DualListModel<EmployeesEntity> idEmployees;
    private List<String> transferedIdEmployeeIDs;
    private List<String> removedIdEmployeeIDs;
    
    public void prepareNewPrimes() {
        reset();
        this.primes = new PrimesEntity();
        // set any default values now, if you need
        // Example: this.primes.setAnything("test");
    }

    public String persist() {

        if (primes.getId() == null && !isPermitted("primes:create")) {
            return "accessDenied";
        } else if (primes.getId() != null && !isPermitted(primes, "primes:update")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            
            if (primes.getId() != null) {
                primes = primesService.update(primes);
                message = "message_successfully_updated";
            } else {
                primes = primesService.save(primes);
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
        
        primesList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(primes, "primes:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            primesService.delete(primes);
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
    
    public void onDialogOpen(PrimesEntity primes) {
        reset();
        this.primes = primes;
    }
    
    public void reset() {
        primes = null;
        primesList = null;
        
        idEmployees = null;
        transferedIdEmployeeIDs = null;
        removedIdEmployeeIDs = null;
        
    }

    public DualListModel<EmployeesEntity> getIdEmployees() {
        return idEmployees;
    }

    public void setIdEmployees(DualListModel<EmployeesEntity> employeess) {
        this.idEmployees = employeess;
    }
    
    public List<EmployeesEntity> getFullIdEmployeesList() {
        List<EmployeesEntity> allList = new ArrayList<>();
        allList.addAll(idEmployees.getSource());
        allList.addAll(idEmployees.getTarget());
        return allList;
    }
    
    public void onIdEmployeesDialog(PrimesEntity primes) {
        // Prepare the idEmployee PickList
        this.primes = primes;
        List<EmployeesEntity> selectedEmployeessFromDB = employeesService
                .findIdEmployeesByPrimes(this.primes);
        List<EmployeesEntity> availableEmployeessFromDB = employeesService
                .findAvailableIdEmployees(this.primes);
        this.idEmployees = new DualListModel<>(availableEmployeessFromDB, selectedEmployeessFromDB);
        
        transferedIdEmployeeIDs = new ArrayList<>();
        removedIdEmployeeIDs = new ArrayList<>();
    }
    
    public void onIdEmployeesPickListTransfer(TransferEvent event) {
        // If a idEmployee is transferred within the PickList, we just transfer it in this
        // bean scope. We do not change anything it the database, yet.
        for (Object item : event.getItems()) {
            String id = ((EmployeesEntity) item).getId().toString();
            if (event.isAdd()) {
                transferedIdEmployeeIDs.add(id);
                removedIdEmployeeIDs.remove(id);
            } else if (event.isRemove()) {
                removedIdEmployeeIDs.add(id);
                transferedIdEmployeeIDs.remove(id);
            }
        }
        
    }
    
    public void updateIdEmployee(EmployeesEntity employees) {
        // If a new idEmployee is created, we persist it to the database,
        // but we do not assign it to this primes in the database, yet.
        idEmployees.getTarget().add(employees);
        transferedIdEmployeeIDs.add(employees.getId().toString());
    }
    
    public void onIdEmployeesSubmit() {
        // Now we save the changed of the PickList to the database.
        try {
            List<EmployeesEntity> selectedEmployeessFromDB = employeesService
                    .findIdEmployeesByPrimes(this.primes);
            List<EmployeesEntity> availableEmployeessFromDB = employeesService
                    .findAvailableIdEmployees(this.primes);
            
            for (EmployeesEntity employees : selectedEmployeessFromDB) {
                if (removedIdEmployeeIDs.contains(employees.getId().toString())) {
                    employees.setPrimes(null);
                    employeesService.update(employees);
                }
            }
    
            for (EmployeesEntity employees : availableEmployeessFromDB) {
                if (transferedIdEmployeeIDs.contains(employees.getId().toString())) {
                    employees.setPrimes(primes);
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
    
    public PrimesEntity getPrimes() {
        if (this.primes == null) {
            prepareNewPrimes();
        }
        return this.primes;
    }
    
    public void setPrimes(PrimesEntity primes) {
        this.primes = primes;
    }
    
    public List<PrimesEntity> getPrimesList() {
        if (primesList == null) {
            primesList = primesService.findAllPrimesEntities();
        }
        return primesList;
    }

    public void setPrimesList(List<PrimesEntity> primesList) {
        this.primesList = primesList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(PrimesEntity primes, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
