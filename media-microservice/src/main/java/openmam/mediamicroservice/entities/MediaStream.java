package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MediaStream {

    public static MediaStream from(Status status) {
        var mediaStream = new MediaStream();
        mediaStream.setStatus(status);
        mediaStream.setCreationDate(null);
        return mediaStream;
    }

    public MediaElement getMediaElement() {
        return mediaElement;
    }

    public void setMediaElement(MediaElement mediaElement) {
        this.mediaElement = mediaElement;
    }

    public Integer getStreamIndex() {
        return streamIndex;
    }

    public void setStreamIndex(Integer streamIndex) {
        this.streamIndex = streamIndex;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public Date creationDate = new Date();

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Type {
        AUDIO, VIDEO, SUBTITLE
    }

    private @Id
    @GeneratedValue Long id;
    @ManyToOne
    @JoinColumn(name = "media_element_id")
    @JsonIgnore
    private MediaElement mediaElement;
    private Integer streamIndex;
    private String codecTagString;
    private String codecType;
    private String codecName;

    public String getCodecTagString() {
        return codecTagString;
    }

    public void setCodecTagString(String codecTagString) {
        this.codecTagString = codecTagString;
    }

    public String getCodecType() {
        return codecType;
    }

    public void setCodecType(String codecType) {
        this.codecType = codecType;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getCodecLongName() {
        return codecLongName;
    }

    public void setCodecLongName(String codecLongName) {
        this.codecLongName = codecLongName;
    }

    private String codecLongName;

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Status {
        MISSING_VARIANT, LOCKED, READY
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
