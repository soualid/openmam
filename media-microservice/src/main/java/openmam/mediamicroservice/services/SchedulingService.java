package openmam.mediamicroservice.services;

import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MediaStreamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import openmam.mediamicroservice.entities.MediaStream;
import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.repositories.TaskRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static openmam.mediamicroservice.entities.Task.Status.SUCCEEDED;

@Service
public class SchedulingService {

    private final MediaStreamRepository mediaStreamRepository;
    private final TaskRepository taskRepository;
    private final Logger logger = LoggerFactory.getLogger(SchedulingService.class);
    private final MediaRepository mediaRepository;
    private final LocationRepository locationRepository;

    public SchedulingService(MediaStreamRepository mediaStreamRepository,
                             LocationRepository locationRepository,
                             MediaRepository mediaRepository,
                             TaskRepository taskRepository) {
        this.mediaStreamRepository = mediaStreamRepository;
        this.taskRepository = taskRepository;
        this.mediaRepository = mediaRepository;
        this.locationRepository = locationRepository; 
    }

    @Transactional
    public List<Task> createMoveAssetTasks(Long mediaId, Long locationId) {
        var media = mediaRepository.getReferenceById(mediaId);
        var location = locationRepository.getReferenceById(locationId);
        var result = new ArrayList<Task>();

        var task = new Task();
        task.setDestinationLocation(location);
        task.setMedia(media);
        task.setStatus(Task.Status.PENDING);
        task.setCreatedBy("todo");
        task.setType(Task.Type.MOVE_ASSET);
        result.add(taskRepository.save(task));

        return result;
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
            task.setCreatedBy("todo");
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
        }
        task.setStatus(SUCCEEDED);
        task.setEndDate(new Date());
        taskRepository.save(task);
    }
}
