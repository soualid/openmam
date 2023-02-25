package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataReference {

    private @Id
    @GeneratedValue Long id;

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    @JsonInclude
    @Transient
    private String representation;

    @ManyToOne
    @JoinColumn(name = "referenced_group_id")
    @JsonIgnore
    private MetadataGroup referencedMetadataGroup;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode dynamicMetadatas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetadataGroup getReferencedMetadataGroup() {
        return referencedMetadataGroup;
    }

    public void setReferencedMetadataGroup(MetadataGroup referencedMetadataGroup) {
        this.referencedMetadataGroup = referencedMetadataGroup;
    }

    public JsonNode getDynamicMetadatas() {
        return dynamicMetadatas;
    }

    public void setDynamicMetadatas(JsonNode dynamicMetadatas) {
        this.dynamicMetadatas = dynamicMetadatas;
    }
}
