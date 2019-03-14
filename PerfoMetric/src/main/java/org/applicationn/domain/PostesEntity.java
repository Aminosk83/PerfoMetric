package org.applicationn.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity(name="Postes")
@Table(name="\"POSTES\"")
public class PostesEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private PostesImage image;
    
    @Size(max = 50)
    @Column(length = 50, name="\"codePoste\"")
    @NotNull
    private String codePoste;

    @Size(max = 100)
    @Column(length = 100, name="\"namePoste\"")
    @NotNull
    private String namePoste;

    @Size(max = 1000)
    @Column(length = 1000, name="\"description\"")
    private String description;

    @Column(name="\"createdat\"")
    @Temporal(TemporalType.DATE)
    private Date createdat;

    @Column(name="\"updatedat\"")
    @Temporal(TemporalType.DATE)
    private Date updatedat;

    @ManyToOne(optional=true)
    @JoinColumn(name = "EMPLOYEES_ID", referencedColumnName = "ID")
    private EmployeesEntity employees;

    @ManyToOne(optional=true)
    @JoinColumn(name = "OBJECTIFSUSER_ID", referencedColumnName = "ID")
    private ObjectifsuserEntity objectifsuser;

    @Column(name = "ENTRY_CREATED_BY")
    private String createdBy;

    @Column(name = "ENTRY_CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "ENTRY_MODIFIED_BY")
    private String modifiedBy;

    @Column(name = "ENTRY_MODIFIED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;
    
    public PostesImage getImage() {
        return image;
    }

    public void setImage(PostesImage image) {
        this.image = image;
    }
    
    public String getCodePoste() {
        return this.codePoste;
    }

    public void setCodePoste(String codePoste) {
        this.codePoste = codePoste;
    }

    public String getNamePoste() {
        return this.namePoste;
    }

    public void setNamePoste(String namePoste) {
        this.namePoste = namePoste;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedat() {
        return this.createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }

    public Date getUpdatedat() {
        return this.updatedat;
    }

    public void setUpdatedat(Date updatedat) {
        this.updatedat = updatedat;
    }

    public EmployeesEntity getEmployees() {
        return this.employees;
    }

    public void setEmployees(EmployeesEntity employees) {
        this.employees = employees;
    }

    public ObjectifsuserEntity getObjectifsuser() {
        return this.objectifsuser;
    }

    public void setObjectifsuser(ObjectifsuserEntity objectifsuser) {
        this.objectifsuser = objectifsuser;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
    
    public void updateAuditInformation(String username) {
        if (this.getId() != null) {
            modifiedAt = new Date();
            modifiedBy = username;
        } else {
            createdAt = new Date();
            modifiedAt = createdAt;
            createdBy = username;
            modifiedBy = createdBy;
        }
    }
    
}
