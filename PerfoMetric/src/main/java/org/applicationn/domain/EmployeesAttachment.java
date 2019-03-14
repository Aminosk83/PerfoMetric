package org.applicationn.domain;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="EmployeesAttachment")
public class EmployeesAttachment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public EmployeesAttachment() {
        super();
    }
    
    public EmployeesAttachment(Long id, String fileName) {
        this.setId(id);
        this.fileName = fileName;
    }

    @Size(max = 200)
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "EMPLOYEES_ID", referencedColumnName = "ID")
    private EmployeesEntity employees;

    @Lob
    private byte[] content;

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public EmployeesEntity getEmployees() {
        return this.employees;
    }

    public void setEmployees(EmployeesEntity employees) {
        this.employees = employees;
    }
}
