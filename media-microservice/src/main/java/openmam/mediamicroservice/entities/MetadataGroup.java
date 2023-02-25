package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataGroup {

    private @Id
    @GeneratedValue Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "metadataGroup")
    private List<MetadataDefinition> metadatas;

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    private long orderNumber;

    public enum Attachment {
        MEDIA, MEDIA_STREAM, MEDIA_ELEMENT, MEDIA_VERSION, REFERENCEABLE
    }
    @Enumerated(EnumType.STRING)
    private Attachment attachmentType;
    public Attachment getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(Attachment attachmentType) {
        this.attachmentType = attachmentType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetadataDefinition> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List<MetadataDefinition> metadata) {
        this.metadatas = metadata;
    }
}
