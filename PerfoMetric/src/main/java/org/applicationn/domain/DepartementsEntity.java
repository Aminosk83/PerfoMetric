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

@Entity(name="Departements")
@Table(name="\"DEPARTEMENTS\"")
public class DepartementsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private DepartementsImage image;
    
    @Size(max = 50)
    @Column(length = 50, name="\"codeDepartement\"")
    @NotNull
    private String codeDepartement;

    @Size(max = 100)
    @Column(length = 100, name="\"nameDepartement\"")
    @NotNull
    private String nameDepartement;

    @Column(name="\"createdat\"")
    @Temporal(TemporalType.DATE)
    private Date createdat;

    @Column(name="\"updatedat\"")
    @Temporal(TemporalType.DATE)
    private Date updatedat;

    @ManyToOne(optional=true)
    @JoinColumn(name = "POSTES_ID", referencedColumnName = "ID")
    private PostesEntity postes;

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
    
    public DepartementsImage getImage() {
        return image;
    }

    public void setImage(DepartementsImage image) {
        this.image = image;
    }
    
    public String getCodeDepartement() {
        return this.codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }

    public String getNameDepartement() {
        return this.nameDepartement;
    }

    public void setNameDepartement(String nameDepartement) {
        this.nameDepartement = nameDepartement;
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

    public PostesEntity getPostes() {
        return this.postes;
    }

    public void setPostes(PostesEntity postes) {
        this.postes = postes;
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
