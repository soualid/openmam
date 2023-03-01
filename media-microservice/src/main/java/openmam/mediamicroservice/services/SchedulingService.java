package openmam.mediamicroservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import openmam.mediamicroservice.dto.IngestFromPartnerJobsInput;
import openmam.mediamicroservice.entities.*;
import openmam.mediamicroservice.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static openmam.mediamicroservice.entities.Configuration.Key.PARTNER_UPLOAD_INGEST_LOCATION;
import static openmam.mediamicroservice.entities.Configuration.Key.PARTNER_UPLOAD_LOCATION;
import static openmam.mediamicroservice.entities.Task.Status.SUCCEEDED;
import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Service
public class SchedulingService {

    private final MediaStreamRepository mediaStreamRepository;
    private final TaskRepository taskRepository;
    private final PartnerUploadRequestRepository partnerUploadRequestRepository;
    private final Logger logger = LoggerFactory.getLogger(SchedulingService.class);
    private final MediaRepository mediaRepository;
    private final LocationRepository locationRepository;
    private final ConfigurationRepository configurationRepository;

    public SchedulingService(MediaStreamRepository mediaStreamRepository,
                             LocationRepository locationRepository,
                             MediaRepository mediaRepository,
                             ConfigurationRepository configurationRepository,
                             TaskRepository taskRepository,
                             PartnerUploadRequestRepository partnerUploadRequestRepository) {
        this.mediaStreamRepository = mediaStreamRepository;
        this.taskRepository = taskRepository;
        this.mediaRepository = mediaRepository;
        this.configurationRepository = configurationRepository;
        this.locationRepository = locationRepository;
        this.partnerUploadRequestRepository = partnerUploadRequestRepository;
    }

    @Transactional
    public List<Task> createMoveAssetTasks(Long mediaId, Long locationId, String caller) {
        var media = mediaRepository.getReferenceById(mediaId);
        var location = locationRepository.getReferenceById(locationId);
        var result = new ArrayList<Task>();

        var task = new Task();
        task.setDestinationLocation(location);
        task.setMedia(media);
        task.setStatus(Task.Status.PENDING);
        task.setCreatedBy(caller);
        task.setType(Task.Type.MOVE_ASSET);
        result.add(taskRepository.save(task));

        return result;
    }

    public Task createIngestPartnerUploadTask(PartnerUploadRequest uploadRequest, String caller) {
        var task = new Task();
        task.setCreationDate(new Date());
        task.setCreatedBy(caller);
        task.setStatus(Task.Status.PENDING);
        task.setMedia(uploadRequest.getMedia());
        task.setType(Task.Type.INGEST_PARTNER_UPLOAD);
        var objectMapper = new ObjectMapper();
        var additionalJobsInput = new IngestFromPartnerJobsInput();
        additionalJobsInput.partnerId = uploadRequest.getPartner().getId();
        additionalJobsInput.partnerUploadId = uploadRequest.getId();
        var location = locationRepository.findById(
                Long.valueOf(configurationRepository.findByKey(PARTNER_UPLOAD_LOCATION).getValue())
        ).get();
        additionalJobsInput.sourceLocation = objectMapper.valueToTree(location);
        var destinationLocation = locationRepository.findById(
                Long.valueOf(configurationRepository.findByKey(PARTNER_UPLOAD_INGEST_LOCATION).getValue())
        ).get();
        task.setDestinationLocation(destinationLocation);
        task.setAdditionalJobInputs(objectMapper.convertValue(additionalJobsInput, JsonNode.class));
        return taskRepository.save(task);
    }

    public static class ScanMediaElementTaskRequestBody {
        public String scanPath;
    }

    @Transactional
    public Task createScanTask(Media media, Location location, String path, String caller) {
        var task = new Task();
        task.setCreationDate(new Date());
        task.setCreatedBy(caller);
        task.setStatus(Task.Status.PENDING);
        task.setMedia(media);
        task.setDestinationLocation(location);
        task.setType(Task.Type.SCAN);
        var objectMapper = new ObjectMapper();
        var additionalJobsInput = new ScanMediaElementTaskRequestBody();
        additionalJobsInput.scanPath = path;
        task.setAdditionalJobInputs(objectMapper.convertValue(additionalJobsInput, JsonNode.class));
        return taskRepository.save(task);
    }

    @Transactional
    public Task createGenerateVariantsTask(Long mediaStreamId) {
        Example<MediaStream> example = Example.of(MediaStream.from(null, mediaStreamId));

        Pageable limit = PageRequest.of(0,1, Sort.by("creationDate").descending());
        var result = mediaStreamRepository.findAll(example, limit);
        var first = result.getContent().stream().findFirst();
        if (first.isPresent()) {
            var mediaStream = first.get();
            mediaStream.setStatus(MediaStream.Status.LOCKED);
            var task = new Task();
            task.setDestinationLocation(mediaStream.getMediaElement().getLocation());
            task.setMediaStream(mediaStream);
            task.setMediaElement(mediaStream.getMediaElement());
            task.setMedia(mediaStream.getMediaElement().getMedia());
            task.setStatus(Task.Status.PENDING);
            task.setCreatedBy("first_ingest");
            task.setType(Task.Type.GENERATE_VARIANTS);
            mediaStreamRepository.save(mediaStream);
            return taskRepository.save(task);
        }
        return null;
    }

    @Transactional
    public Task acquireLockForNextTask(String requestedBy) {
        Example<Task> example = Example.of(Task.from(Task.Status.PENDING));
        Pageable limit = PageRequest.of(0,1, Sort.by("creationDate").descending());
        var result = taskRepository.findAll(example, limit);
        if (result.hasContent()) {
            var task = result.getContent().stream().findFirst().get();
            task.setStatus(Task.Status.WORKING);
            task.setLockedBy(requestedBy);
            task = taskRepository.save(task);
            return task;
        }
        return null;
    }

    @Transactional
    public void endTask(Long jobId) {
        var task = taskRepository.getReferenceById(jobId);
        switch(task.getType()) {
            case MOVE_ASSET -> {
                for (var element: task.getMedia().getElements()) {
                    element.setLocation(task.getDestinationLocation());
                }
                mediaRepository.save(task.getMedia());
            }
            case INGEST_PARTNER_UPLOAD -> {
                var partnerUploadId = task.getAdditionalJobInputs().get("partnerUploadId").longValue();
                var partnerUpload = partnerUploadRequestRepository.getReferenceById(partnerUploadId);
                partnerUpload.setStatus(PartnerUploadRequest.PartnerUploadStatus.INGESTED);
                partnerUploadRequestRepository.save(partnerUpload);
            }
        }
        task.setStatus(SUCCEEDED);
        task.setEndDate(new Date());
        taskRepository.save(task);
    }
}
