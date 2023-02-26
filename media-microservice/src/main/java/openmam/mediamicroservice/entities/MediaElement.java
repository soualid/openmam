package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MediaElement {

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private @Id
    @GeneratedValue Long id;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "media_id")
    @JsonIgnore
    private Media media;

    private String filename;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public JsonNode getFullMetadatas() {
        return fullMetadatas;
    }

    public void setFullMetadatas(JsonNode fullMetadatas) {
        this.fullMetadatas = fullMetadatas;
    }

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode fullMetadatas;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mediaElement")
    private List<MediaStream> streams;

    public List<MediaStream> getStreams() {
        return streams;
    }

    public void setStreams(List<MediaStream> streams) {
        this.streams = streams;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
