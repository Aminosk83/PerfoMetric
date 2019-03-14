package org.applicationn.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

@Entity(name="Resultas")
@Table(name="\"RESULTAS\"")
public class ResultasEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Digits(integer = 5,  fraction = 3)
    @Column(precision = 8, scale = 3, name="\"somme\"")
    @NotNull
    private BigDecimal somme;

    @Column(name="\"createdat\"")
    @Temporal(TemporalType.DATE)
    private Date createdat;

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
    
    public BigDecimal getSomme() {
        return this.somme;
    }

    public void setSomme(BigDecimal somme) {
        this.somme = somme;
    }

    public Date getCreatedat() {
        return this.createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
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
