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
public class MediaVersion {

    private @Id
    @GeneratedValue Long id;

    @ManyToOne
    @JoinColumn(name = "media_id")
    @JsonIgnore
    private Media media;

    private String name;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    private String language;

    private Long orderNumber;

    public JsonNode getDynamicMetadatas() {
        return dynamicMetadatas;
    }

    public void setDynamicMetadatas(JsonNode dynamicMetadatas) {
        this.dynamicMetadatas = dynamicMetadatas;
    }

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode dynamicMetadatas;

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    @ManyToOne
    @JoinColumn(name = "audio_stream_id")
    private MediaStream audio;

    @ManyToMany
    @JoinTable(
            name = "media_version_subtitles",
            joinColumns = @JoinColumn(name = "media_version_id"),
            inverseJoinColumns = @JoinColumn(name = "media_stream_id"))
    private List<MediaStream> subtitles;

    public MediaStream getAudio() {
        return audio;
    }

    public void setAudio(MediaStream audio) {
        this.audio = audio;
    }

    public List<MediaStream> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<MediaStream> subtitles) {
        this.subtitles = subtitles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
