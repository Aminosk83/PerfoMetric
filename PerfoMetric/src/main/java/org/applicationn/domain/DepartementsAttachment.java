package org.applicationn.domain;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="DepartementsAttachment")
public class DepartementsAttachment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public DepartementsAttachment() {
        super();
    }
    
    public DepartementsAttachment(Long id, String fileName) {
        this.setId(id);
        this.fileName = fileName;
    }

    @Size(max = 200)
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "DEPARTEMENTS_ID", referencedColumnName = "ID")
    private DepartementsEntity departements;

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

    public DepartementsEntity getDepartements() {
        return this.departements;
    }

    public void setDepartements(DepartementsEntity departements) {
        this.departements = departements;
    }
}
