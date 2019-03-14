package org.applicationn.domain;

import java.io.Serializable;
import java.math.BigDecimal;
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

@Entity(name="Objectifs")
@Table(name="\"OBJECTIFS\"")
public class ObjectifsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private ObjectifsImage image;
    
    @Size(max = 50)
    @Column(length = 50, name="\"codeObjectif\"")
    private String codeObjectif;

    @Size(max = 100)
    @Column(length = 100, name="\"nameObjectif\"")
    @NotNull
    private String nameObjectif;

    @Digits(integer = 3,  fraction = 2)
    @Column(precision = 5, scale = 2, name="\"poids\"")
    @NotNull
    private BigDecimal poids;

    @Size(max = 200)
    @Column(length = 200, name="\"description\"")
    private String description;

    @Column(name="\"createdat\"")
    @Temporal(TemporalType.DATE)
    private Date createdat;

    @Column(name="\"updatedat\"")
    @Temporal(TemporalType.DATE)
    private Date updatedat;

    @ManyToOne(optional=true)
    @JoinColumn(name = "OBJECTIFSUSER_ID", referencedColumnName = "ID")
    private ObjectifsuserEntity objectifsuser;

    public ObjectifsImage getImage() {
        return image;
    }

    public void setImage(ObjectifsImage image) {
        this.image = image;
    }
    
    public String getCodeObjectif() {
        return this.codeObjectif;
    }

    public void setCodeObjectif(String codeObjectif) {
        this.codeObjectif = codeObjectif;
    }

    public String getNameObjectif() {
        return this.nameObjectif;
    }

    public void setNameObjectif(String nameObjectif) {
        this.nameObjectif = nameObjectif;
    }

    public BigDecimal getPoids() {
        return this.poids;
    }

    public void setPoids(BigDecimal poids) {
        this.poids = poids;
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

    public ObjectifsuserEntity getObjectifsuser() {
        return this.objectifsuser;
    }

    public void setObjectifsuser(ObjectifsuserEntity objectifsuser) {
        this.objectifsuser = objectifsuser;
    }

}
