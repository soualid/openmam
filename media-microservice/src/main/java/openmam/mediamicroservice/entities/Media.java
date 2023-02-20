package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Media {

    private @Id
    @GeneratedValue Long id;

    public JsonNode getDynamicMetadatas() {
        return dynamicMetadatas;
    }

    public Long elementsCount = 0L;
    public Long streamsCount = 0L;

    public Long getElementsCount() {
        return elementsCount;
    }

    public void setElementsCount(Long elementsCount) {
        this.elementsCount = elementsCount;
    }

    public Long getStreamsCount() {
        return streamsCount;
    }

    public void setStreamsCount(Long streamsCount) {
        this.streamsCount = streamsCount;
    }

    public MediaStream getVideo() {
        return video;
    }

    public void setVideo(MediaStream video) {
        this.video = video;
    }

    public void setDynamicMetadatas(JsonNode dynamicMetadatas) {
        this.dynamicMetadatas = dynamicMetadatas;
    }

    @ManyToOne
    @JoinColumn(name = "video_stream_id")
    private MediaStream video;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode dynamicMetadatas;

    private String name;

    public Location getVariantsLocation() {
        return variantsLocation;
    }

    public void setVariantsLocation(Location variantsLocation) {
        this.variantsLocation = variantsLocation;
    }

    @ManyToOne
    @JoinColumn(name = "variants_location_id")
    private Location variantsLocation;

    public List<MediaElement> getElements() {
        return elements;
    }

    public void setElements(List<MediaElement> elements) {
        this.elements = elements;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "media")
    private List<MediaElement> elements;

    public List<MediaVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<MediaVersion> versions) {
        this.versions = versions;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "media")
    private List<MediaVersion> versions;

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
