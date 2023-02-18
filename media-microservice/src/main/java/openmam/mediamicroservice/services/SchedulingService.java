package openmam.mediamicroservice.services;

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

@Service
public class SchedulingService {

    private final MediaStreamRepository mediaStreamRepository;
    private final TaskRepository taskRepository;
    private final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    public SchedulingService(MediaStreamRepository mediaStreamRepository, TaskRepository taskRepository) {
        this.mediaStreamRepository = mediaStreamRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task scheduleTask(Task.Type type, String requestedBy) {
        return switch(type) {
            case GENERATE_VARIANTS -> createGenerateVariantsTask(requestedBy);
            default -> null;
        };
    }

    private Task createGenerateVariantsTask(String requestedBy) {
        Example<MediaStream> example = Example.of(MediaStream.from(MediaStream.Status.MISSING_VARIANT));
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
            task.setStatus(Task.Status.WORKING);
            task.setCreatedBy(requestedBy);
            task.setType(Task.Type.GENERATE_VARIANTS);
            mediaStreamRepository.save(mediaStream);
            return taskRepository.save(task);
        }
        return null;
    }
}
