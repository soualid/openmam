package openmam.mediamicroservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import openmam.mediamicroservice.entities.*;
import openmam.mediamicroservice.repositories.*;
import openmam.mediamicroservice.services.MediaService;
import openmam.mediamicroservice.services.SchedulingService;
import openmam.mediamicroservice.utils.Finder;
import org.buildobjects.process.ProcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
class MediaController {

  private final MediaRepository mediaRepository;
  private final MediaElementRepository mediaElementRepository;
  private final Logger logger = LoggerFactory.getLogger(MediaController.class);
  private final LocationRepository locationRepository;
  private final MediaStreamRepository mediaStreamRepository;
  private final SchedulingService schedulingService;
  private final MediaVersionRepository mediaVersionRepository;
  private final MediaService mediaService;

  MediaController(MediaRepository mediaRepository,
                  MediaService mediaService,
                  MediaElementRepository mediaElementRepository,
                  MediaVersionRepository mediaVersionRepository,
                  MediaStreamRepository mediaStreamRepository,
                  LocationRepository locationRepository,
                  SchedulingService schedulingService) {
    this.mediaRepository = mediaRepository;
    this.mediaService = mediaService;
    this.mediaElementRepository = mediaElementRepository;
    this.locationRepository = locationRepository;
    this.mediaStreamRepository = mediaStreamRepository;
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
        if (stream.getType() == MediaStream.Type.VIDEO) {
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
  List<Task> moveMediaElements(@PathVariable long id, @PathVariable long locationId) {
    return schedulingService.createMoveAssetTasks(id, locationId);
  }

  @PostMapping("/media/{id}/scan")
  void scanMediaElementsForMediaId(@PathVariable long id, @RequestBody ScanParameters scanParameters) {
    var media = mediaRepository.findById(id).get();
    logger.info("id: " + id +
            " media: " + media +
            " parameters: " + scanParameters.locationId + " / " + scanParameters.path);

    // à déporter dans un worker
    var location = locationRepository.getReferenceById(scanParameters.locationId);
    var streamsCount = media.getStreamsCount();

    // TODO scan from S3
    if (location.getType() == Location.Type.LOCAL) {
      logger.info("Looking in " + location.getPath());
      Finder finder = new Finder(scanParameters.path);
      try {
        Files.walkFileTree(Path.of(location.getPath()), finder);
        var results = finder.done();
        logger.info("results: " + results);
        var mapper = new ObjectMapper();

        for (var result : results) {
          String output = ProcBuilder.run("ffprobe", "-v", "quiet", "-show_format", "-show_streams", "-print_format", "json", result.getAbsolutePath());
          logger.info("output: " + output);

          var response = mapper.readValue(output, FFProbeResponse.class);
          logger.info("output: " + response);

          var mediaElement = new MediaElement();
          mediaElement.setFilename(result.getName());
          mediaElement.setLocation(location);
          mediaElement.setMedia(media);
          mediaElement.setFullMetadatas(mapper.readTree(output));
          mediaElementRepository.save(mediaElement);

          for (var stream : response.streams) {
            streamsCount++;

            var mediaStream = new MediaStream();
            mediaStream.setMediaElement(mediaElement);
            mediaStream.setStatus(MediaStream.Status.LOCKED);
            mediaStream.setCodecLongName(stream.codecLongName);
            mediaStream.setCodecName(stream.codecName);
            mediaStream.setCodecTagString(stream.codecTagString);
            mediaStream.setCodecType(stream.codecType);

            var streamType = switch (stream.codecType) {
              case "video":
                yield MediaStream.Type.VIDEO;
              case "audio":
                yield MediaStream.Type.AUDIO;
              case "subtitle":
                yield MediaStream.Type.SUBTITLE;
              default:
                throw new RuntimeException("unknown type");
            };

            if (streamType == MediaStream.Type.VIDEO && media.getVideo() == null) {
              media.setVideo(mediaStream);
            }

            mediaStream.setType(streamType);
            mediaStream.setStreamIndex(stream.index);
            mediaStream = mediaStreamRepository.save(mediaStream);

            // automatically schedule transcoding tasks
            schedulingService.createGenerateVariantsTask(mediaStream.getId());
          }
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    media.setElementsCount((long) media.getElements().size());
    media.setStreamsCount(streamsCount);
    mediaRepository.save(media);
  }

  @PostMapping("/media")
  Media newMedia(@RequestBody Media media) {
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

  @PutMapping("/medias/{id}")
  Media replaceMedia(@RequestBody Media media, @PathVariable Long id) {
    
    return mediaRepository.findById(id)
      .map(m -> {
        return mediaRepository.save(m);
      })
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