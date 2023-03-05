package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.PartnerUploadRequest;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.PartnerUploadRequestRepository;
import openmam.mediamicroservice.security.repository.UserRepository;
import openmam.mediamicroservice.services.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.security.Principal;
import java.time.Duration;
import java.util.Date;

@RestController
class PartnerUploadController {

    private final Logger logger = LoggerFactory.getLogger(PartnerUploadController.class);
    private final PartnerUploadRequestRepository partnerUploadRequestRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final LocationRepository locationRepository;
    private final SchedulingService schedulingService;

    public PartnerUploadController(PartnerUploadRequestRepository partnerUploadRequestRepository,
                                   UserRepository userRepository,
                                   LocationRepository locationRepository,
                                   SchedulingService schedulingService,
                                   MediaRepository mediaRepository) {
        this.partnerUploadRequestRepository = partnerUploadRequestRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
        this.schedulingService = schedulingService;
        this.locationRepository = locationRepository;
    }

    @Secured({"ROLE_PARTNER"})
    @GetMapping("/my/uploadRequests")
    Page<PartnerUploadRequest> all(Pageable pageable, Principal caller) {
        var partner = userRepository.findByEmail(caller.getName()).get();
        return partnerUploadRequestRepository.findByPartner(partner, pageable);
    }

    @PutMapping("/uploadRequests/{requestId}/status/{newStatus}")
    PartnerUploadRequest changeUploadRequestStatus(@PathVariable long requestId,
                                                   @PathVariable PartnerUploadRequest.PartnerUploadStatus newStatus,
                                                   Principal caller) {
        // TODO secure
        var uploadRequest = partnerUploadRequestRepository.getReferenceById(requestId);
        uploadRequest.setStatus(newStatus);

        if (newStatus == PartnerUploadRequest.PartnerUploadStatus.INGESTING) {
            schedulingService.createIngestPartnerUploadTask(uploadRequest, caller.getName());
        }
        return partnerUploadRequestRepository.save(uploadRequest);
    }

    @PostMapping("/partner/{partnerId}/media/{mediaId}/uploadRequest")
    PartnerUploadRequest newUploadRequest(@RequestBody PartnerUploadRequest uploadRequest,
                                          @PathVariable long partnerId,
                                          @PathVariable long mediaId,
                                          Principal caller) {
        var partner = userRepository.getReferenceById(partnerId);
        var media = mediaRepository.getReferenceById(mediaId);
        uploadRequest.setPartner(partner);
        uploadRequest.setMedia(media);
        uploadRequest.setCreationDate(new Date());
        uploadRequest.setCreatedBy(caller.getName());
        uploadRequest = partnerUploadRequestRepository.save(uploadRequest);

        var location = locationRepository.getReferenceById(2L);
        System.setProperty("aws.accessKeyId", location.getS3accessKey());
        System.setProperty("aws.secretAccessKey", location.getS3accessSecret());
        var presigner = S3Presigner.builder()
                .region(Region.of(location.getS3region()))
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .build();

        var objectRequest = PutObjectRequest.builder()
                .bucket(location.getS3bucketName())
                .key("uploaded-" + mediaId + "-" + partnerId + "-" + uploadRequest.getId())
                .build();

        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24)) // TODO dynamic
                .putObjectRequest(objectRequest)
                .build();

        var presignedRequest = presigner.presignPutObject(presignRequest);
        var presignedUrl = presignedRequest.url().toString();
        uploadRequest.setPresignedUploadURL(presignedUrl);

        return partnerUploadRequestRepository.save(uploadRequest);
    }

}
