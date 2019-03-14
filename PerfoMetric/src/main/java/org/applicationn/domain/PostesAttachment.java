package org.applicationn.domain;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="PostesAttachment")
public class PostesAttachment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public PostesAttachment() {
        super();
    }
    
    public PostesAttachment(Long id, String fileName) {
        this.setId(id);
        this.fileName = fileName;
    }

    @Size(max = 200)
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "POSTES_ID", referencedColumnName = "ID")
    private PostesEntity postes;

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

    public PostesEntity getPostes() {
        return this.postes;
    }

    public void setPostes(PostesEntity postes) {
        this.postes = postes;
    }
}
