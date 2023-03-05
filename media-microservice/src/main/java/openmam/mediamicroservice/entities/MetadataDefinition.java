package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import openmam.mediamicroservice.utils.MetadataGroupToStringSerializer;
import org.hibernate.annotations.Type;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataDefinition {

    public enum Type {
        TEXT,
        LONG_TEXT,
        DATE,
        REFERENCE,
        NUMBER,
        MULTI_VALUED,
        EXTERNAL_KEY_VALUE
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    @org.hibernate.annotations.Type(StringArrayType.class)
    @Column(
            name = "editing_restricted_to_roles",
            columnDefinition = "text[]"
    )
    private String[] editingRestrictedToRoles;

    public String[] getEditingRestrictedToRoles() {
        return editingRestrictedToRoles;
    }

    public void setEditingRestrictedToRoles(String[] editingRestrictedToRoles) {
        this.editingRestrictedToRoles = editingRestrictedToRoles;
    }

    private long orderNumber;

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public MetadataGroup getReferencedMetadataGroup() {
        return referencedMetadataGroup;
    }

    public void setReferencedMetadataGroup(MetadataGroup referencedMetadataGroup) {
        this.referencedMetadataGroup = referencedMetadataGroup;
    }

    @Column(columnDefinition = "boolean default false")
    private boolean searchable;

    @ManyToOne
    @JoinColumn(name = "referenced_group_id")
    @JsonSerialize(using = MetadataGroupToStringSerializer.class)
    private MetadataGroup referencedMetadataGroup;


    public String[] getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = allowedValues;
    }


    @org.hibernate.annotations.Type(StringArrayType.class)
    @Column(
            name = "allowed_values",
            columnDefinition = "text[]"
    )
    private String[] allowedValues;

    private @Id
    @GeneratedValue Long id;

    private String name;
    private String label;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MetadataGroup getMetadataGroup() {
        return metadataGroup;
    }

    public void setMetadataGroup(MetadataGroup metadataGroup) {
        this.metadataGroup = metadataGroup;
    }

    @ManyToOne
    @JoinColumn(name = "metadata_group_id")
    @JsonIgnore
    private MetadataGroup metadataGroup;

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

}
