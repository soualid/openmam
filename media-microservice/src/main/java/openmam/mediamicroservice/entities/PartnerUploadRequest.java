package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import openmam.mediamicroservice.security.entities.User;
import openmam.mediamicroservice.utils.MetadataGroupToStringSerializer;
import openmam.mediamicroservice.utils.OnlyIdAsNumberAndToStringSerializer;
import openmam.mediamicroservice.utils.OnlyIdAsNumberSerializer;

import java.util.Date;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerUploadRequest {

    public enum PartnerUploadStatus {
        PENDING,
        UPLOADED,
        INGESTING,
        INGESTED, VALIDATED
    }

    private @Id
    @GeneratedValue Long id;

    @ManyToOne
    @JoinColumn(name = "partner_user_id")
    @JsonSerialize(using = OnlyIdAsNumberAndToStringSerializer.class)
    private User partner;

    @ManyToOne
    @JsonSerialize(using = OnlyIdAsNumberAndToStringSerializer.class)
    @JoinColumn(name = "destination_media_id")
    private Media media;

    public PartnerUploadStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerUploadStatus status) {
        this.status = status;
    }

    @Enumerated(EnumType.STRING)
    private PartnerUploadStatus status = PartnerUploadStatus.PENDING;

    private Date creationDate = new Date();

    private String createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPartner() {
        return partner;
    }

    public void setPartner(User partner) {
        this.partner = partner;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getPresignedUploadURL() {
        return presignedUploadURL;
    }

    public void setPresignedUploadURL(String presignedUploadURL) {
        this.presignedUploadURL = presignedUploadURL;
    }

    @Column(columnDefinition="TEXT")
    private String presignedUploadURL;


}
