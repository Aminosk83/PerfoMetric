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
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.applicationn.domain.security.UserEntity;

@Entity(name="Employees")
@Table(name="\"EMPLOYEES\"")
public class EmployeesEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private EmployeesImage image;
    
    @Size(max = 50)
    @Column(length = 50, name="\"codeEmployee\"")
    @NotNull
    private String codeEmployee;

    @Size(max = 200)
    @Column(length = 200, name="\"name\"")
    @NotNull
    private String name;

    @OneToOne(optional=true, cascade=CascadeType.DETACH)
    @JoinColumn(name="IDUSER_ID", nullable=true)
    private UserEntity idUser;

    @Column(name="\"createat\"")
    @Temporal(TemporalType.DATE)
    private Date createat;

    @Column(name="\"updateat\"")
    @Temporal(TemporalType.DATE)
    private Date updateat;

    @Column(name="\"statusId\"")
    @Digits(integer = 4, fraction = 0)
    @NotNull
    private Integer statusId;

    @ManyToOne(optional=true)
    @JoinColumn(name = "PRIMES_ID", referencedColumnName = "ID")
    private PrimesEntity primes;

    @ManyToOne(optional=true)
    @JoinColumn(name = "RESULTAS_ID", referencedColumnName = "ID")
    private ResultasEntity resultas;

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
    
    public EmployeesImage getImage() {
        return image;
    }

    public void setImage(EmployeesImage image) {
        this.image = image;
    }
    
    public String getCodeEmployee() {
        return this.codeEmployee;
    }

    public void setCodeEmployee(String codeEmployee) {
        this.codeEmployee = codeEmployee;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getIdUser() {
        return this.idUser;
    }

    public void setIdUser(UserEntity idUser) {
        this.idUser = idUser;
    }

    public Date getCreateat() {
        return this.createat;
    }

    public void setCreateat(Date createat) {
        this.createat = createat;
    }

    public Date getUpdateat() {
        return this.updateat;
    }

    public void setUpdateat(Date updateat) {
        this.updateat = updateat;
    }

    public Integer getStatusId() {
        return this.statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public PrimesEntity getPrimes() {
        return this.primes;
    }

    public void setPrimes(PrimesEntity primes) {
        this.primes = primes;
    }

    public ResultasEntity getResultas() {
        return this.resultas;
    }

    public void setResultas(ResultasEntity resultas) {
        this.resultas = resultas;
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
