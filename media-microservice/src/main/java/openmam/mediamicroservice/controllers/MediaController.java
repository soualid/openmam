package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.*;
import openmam.mediamicroservice.repositories.*;
import openmam.mediamicroservice.services.MediaService;
import openmam.mediamicroservice.services.SchedulingService;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static openmam.mediamicroservice.entities.MediaStream.Type.VIDEO;

@RestController
class MediaController {

  @Autowired(required = false)
  private ProcessEngine processEngine;

  private final MediaRepository mediaRepository;
  private final MediaElementRepository mediaElementRepository;
  private final Logger logger = LoggerFactory.getLogger(MediaController.class);
  private final LocationRepository locationRepository;
  private final MediaStreamRepository mediaStreamRepository;
  private final TaskRepository taskRepository;
  private final SchedulingService schedulingService;
  private final MediaVersionRepository mediaVersionRepository;
  private final MediaService mediaService;

  @Autowired
  private ConfigurationRepository configurationRepository;

  MediaController(MediaRepository mediaRepository,
                  MediaService mediaService,
                  MediaElementRepository mediaElementRepository,
                  MediaVersionRepository mediaVersionRepository,
                  MediaStreamRepository mediaStreamRepository,
                  LocationRepository locationRepository,
                  SchedulingService schedulingService,
                  TaskRepository taskRepository) {
    this.mediaRepository = mediaRepository;
    this.mediaService = mediaService;
    this.mediaElementRepository = mediaElementRepository;
    this.locationRepository = locationRepository;
    this.mediaStreamRepository = mediaStreamRepository;
    this.taskRepository = taskRepository;
    this.mediaVersionRepository = mediaVersionRepository;
    this.schedulingService = schedulingService;
  }

  @GetMapping("/medias")
  Page<Media> all(Pageable pageable) {
    return mediaRepository.findAll(pageable);
  }

  @GetMapping("/media/{id}/elements")
  List<MediaElement> findMediaElementsForMediaId(long id) {
    return mediaElementRepository.findByMediaId(id);
  }

  @GetMapping("/media/{id}")
  Media getMediaById(@PathVariable long id) {
    var media = mediaRepository.getReferenceById(id);
    return media;
  }

  @GetMapping("/media/{id}/hls")
  String getMediaMasterPlaylistById(@PathVariable long id) {
    var media = mediaRepository.getReferenceById(id);
    var buffer = """
            #EXTM3U
            #EXT-X-VERSION:4
                        
            # AUDIO groups
            """;
    MediaStream video = null;
    for (var element: media.getElements()) {
      for (var stream: element.getStreams()) {
        if (stream.getType() == VIDEO) {
          video = stream;
          break;
        }
      }
    }
    for (var version: media.getVersions()) {
      buffer += """
              #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio",LANGUAGE="%s", NAME="%s",AUTOSELECT=YES,DEFAULT=YES,CHANNELS="2",URI="%s"
              """.formatted(version.getLanguage(), version.getName(),
              "%s_audio_%s_%s/playlist.m3u8".formatted(media.getId(), version.getAudio().getMediaElement().getId(), version.getAudio().getStreamIndex()));
    }
    buffer += """
            #EXT-X-STREAM-INF:BANDWIDTH=6134000,RESOLUTION=1024x458,CODECS="avc1.4d001f,mp4a.40.2",AUDIO="audio"
            %s_video_%s_%s/playlist.m3u8            
            """.formatted(media.getId(), video.getMediaElement().getId(), video.getStreamIndex());
    return buffer;
  }

  private static class ScanParameters {
    public long locationId;
    public String path;
  }

  @PostMapping("/media/{id}/move/{locationId}")
  List<Task> moveMediaElements(@PathVariable long id, @PathVariable long locationId, Principal caller) {
    return schedulingService.createMoveAssetTasks(id, locationId, caller.getName());
  }

  @PostMapping("/media/{mediaId}/scanCallback")
  Media scanMediaElementsTaskCallback(@PathVariable long mediaId,
                                     @RequestBody ScanMediaElementTaskCallbackRequestBody body) {
    // TODO transactional
    var media = mediaRepository.getReferenceById(mediaId);
    body.mediaElement.setMedia(media);
    var mediaElement = mediaElementRepository.save(body.mediaElement);
    mediaElement.setStreams(new ArrayList<>());
    var streamsCount = media.getStreamsCount();
    for (var stream : body.mediaStreams) {
      streamsCount++;
      stream.setMediaElement(mediaElement);
      stream = mediaStreamRepository.save(stream);
      mediaElement.getStreams().add(stream);
      if (stream.getType() == VIDEO && media.getVideo() == null) {
        media.setVideo(stream);
      }
      // automatically schedule transcoding tasks
      schedulingService.createGenerateVariantsTask(stream.getId());
    }
    // media.getElements().add(mediaElement);
    media.setStreamsCount(streamsCount);
    logger.info("number of elements {}", (long) media.getElements().size());
    media.setElementsCount((long) media.getElements().size());
    logger.info("number of elements {}", media.getElementsCount());

    return mediaRepository.save(media);
  }

  public static class ScanMediaElementTaskCallbackRequestBody {
    public MediaElement mediaElement;
    public List<MediaStream> mediaStreams;
  }

  @PostMapping("/media/{id}/scan")
  Task scanMediaElementsForMediaId(@PathVariable long id,
                                   @RequestBody ScanParameters scanParameters,
                                   Principal caller) {
    var media = mediaRepository.findById(id).get();
    logger.info("id: " + id +
            " media: " + media +
            " parameters: " + scanParameters.locationId + " / " + scanParameters.path);

    var location = locationRepository.getReferenceById(scanParameters.locationId);

    return schedulingService.createScanTask(media, location, scanParameters.path, caller.getName());
  }

  @PostMapping("/media")
  Media newMedia(@RequestBody Media media) {
    // ACTIVITI_MEDIA_WORKFLOW_KEY
    var activitiMediaWorkflowKey = configurationRepository.findByKey(Configuration.Key.ACTIVITI_MEDIA_WORKFLOW_KEY);
    if (activitiMediaWorkflowKey != null && processEngine != null) {
      var workflowKey = activitiMediaWorkflowKey.value;
      var process = processEngine.getRuntimeService().startProcessInstanceByKey(workflowKey);
      media.setActivitiProcessId(process.getId());
    }
    return mediaRepository.save(media);
  }

  @PostMapping("/media/{mediaId}/version")
  MediaVersion newMediaVersion(@RequestBody MediaVersion mediaVersion,
                               @PathVariable long mediaId,
                               @RequestParam Long audioId,
                               @RequestParam(required = false) Long[] subtitleIds) {
    var media = mediaRepository.getReferenceById(mediaId);
    mediaVersion.setMedia(media);
    var audio = mediaStreamRepository.getReferenceById(audioId);
    mediaVersion.setAudio(audio);
    mediaVersion.setSubtitles(new ArrayList<>());
    if (subtitleIds != null) {
      for (var subtitleStreamId : subtitleIds) {
        var stream = mediaStreamRepository.getReferenceById(subtitleStreamId);
        mediaVersion.getSubtitles().add(stream);
      }
    }
    var version = mediaVersionRepository.save(mediaVersion);
    media = mediaRepository.getReferenceById(mediaId);
    mediaService.generateMasterPlaylist(media);
    return version;
  }

  @GetMapping("/medias/{id}")
  Media one(@PathVariable Long id) {

    return mediaRepository.findById(id)
            .orElseThrow(() -> new MediaNotFoundException(id));
  }

  @GetMapping("/media/{id}/tasks/pending")
  List<Task> getPendingTasksForMedia(@PathVariable Long id) {
    return taskRepository.findByStatuses(id, Arrays.asList(Task.Status.PENDING, Task.Status.WORKING));
  }

  @PutMapping("/medias/{id}")
  Media updateMedia(@RequestBody Media media, @PathVariable Long id) {
    
    return mediaRepository.findById(id)
      .map(m -> mediaRepository.save(m))
      .orElseGet(() -> {
        media.setId(id);
        return mediaRepository.save(media);
      });
  }

  @DeleteMapping("/medias/{id}")
  void deleteEmployee(@PathVariable Long id) {
    mediaRepository.deleteById(id);
  }
}

class MediaNotFoundException extends RuntimeException {

  MediaNotFoundException(Long id) {
    super("Could not find media " + id);
  }
}