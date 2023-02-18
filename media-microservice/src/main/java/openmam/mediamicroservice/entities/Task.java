package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {

    public enum Type {
        SCAN,
        GENERATE_VARIANTS
    }

    public enum Status {
        PENDING,
        WORKING,
        DONE,
        FAILED
    }

    private @Id
    @GeneratedValue Long id;

    public MediaElement getMediaElement() {
        return mediaElement;
    }

    public void setMediaElement(MediaElement mediaElement) {
        this.mediaElement = mediaElement;
    }

    @ManyToOne
    @JoinColumn(name = "source_media_element_id")
    private MediaElement mediaElement;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    @ManyToOne
    @JoinColumn(name = "source_media_id")
    private Media media;

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    @ManyToOne
    @JoinColumn(name = "destination_location_id")
    private Location destinationLocation;

    @ManyToOne
    @JoinColumn(name = "media_stream_id")
    private MediaStream mediaStream;

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    private Date creationDate = new Date();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private String createdBy;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
