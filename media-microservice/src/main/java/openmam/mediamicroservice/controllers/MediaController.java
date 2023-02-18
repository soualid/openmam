package openmam.mediamicroservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import openmam.mediamicroservice.entities.*;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaElementRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MediaStreamRepository;
import openmam.mediamicroservice.utils.Finder;
import org.buildobjects.process.ProcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
class MediaController {

  private final MediaRepository mediaRepository;
  private final MediaElementRepository mediaElementRepository;
  private final Logger logger = LoggerFactory.getLogger(MediaController.class);
  private final LocationRepository locationRepository;
  private final MediaStreamRepository mediaStreamRepository;

  MediaController(MediaRepository mediaRepository,
                  MediaElementRepository mediaElementRepository,
                  MediaStreamRepository mediaStreamRepository,
                  LocationRepository locationRepository) {
    this.mediaRepository = mediaRepository;
    this.mediaElementRepository = mediaElementRepository;
    this.locationRepository = locationRepository;
    this.mediaStreamRepository = mediaStreamRepository;
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

  private static class ScanParameters {

    public long locationId;
    public String path;
  }


  @PostMapping("/media/{id}/scan")
  void scanMediaElementsForMediaId(@PathVariable long id, @RequestBody ScanParameters scanParameters) {
    var media = mediaRepository.findById(id).get();
    logger.info("id: " + id +
            " media: " + media +
            " parameters: " + scanParameters.locationId + " / " + scanParameters.path);
    // à déporter dans un worker
    var location = locationRepository.getReferenceById(scanParameters.locationId);

    if (location.getType() == Location.Type.LOCAL) {
      logger.info("Looking in " + location.getPath());
      Finder finder = new Finder(scanParameters.path);
      try {
        Files.walkFileTree(Path.of(location.getPath()), finder);
        var results = finder.done();
        logger.info("results: " + results);
        for (var result : results) {
          String output = ProcBuilder.run("ffprobe", "-v", "quiet", "-show_format", "-show_streams", "-print_format", "json", result.getAbsolutePath());
          logger.info("output: " + output);

          ObjectMapper mapper = new ObjectMapper();
          var response = mapper.readValue(output, FFProbeResponse.class);
          logger.info("output: " + response);

          var mediaElement = new MediaElement();
          mediaElement.setFilename(result.getName());
          mediaElement.setLocation(location);
          mediaElement.setMedia(media);
          mediaElement.setFullMetadatas(output);
          mediaElementRepository.save(mediaElement);

          for (var stream : response.streams) {
            var mediaStream = new MediaStream();
            mediaStream.setMediaElement(mediaElement);
            mediaStream.setStatus(MediaStream.Status.MISSING_VARIANT);
            mediaStream.setCodecLongName(stream.codecLongName);
            mediaStream.setCodecName(stream.codecName);
            mediaStream.setCodecTagString(stream.codecTagString);
            mediaStream.setCodecType(stream.codecType);
            // TODO handle subtitles as well
            var codecType = switch (stream.codecType) {
              case "video":
                yield MediaStream.Type.VIDEO;
              case "audio":
                yield MediaStream.Type.AUDIO;
              default:
                throw new RuntimeException("unknown type");
            };

            mediaStream.setType(codecType);
            mediaStream.setStreamIndex(stream.index);
            mediaStreamRepository.save(mediaStream);
          }
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @PostMapping("/media")
  Media newMedia(@RequestBody Media media) {
    return mediaRepository.save(media);
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