package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.OutgestProfile;
import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MediaStreamRepository;
import openmam.mediamicroservice.repositories.OutgestProfileRepository;
import openmam.mediamicroservice.services.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
class OutgestController {

    private final Logger logger = LoggerFactory.getLogger(OutgestController.class);
    @Autowired
    private OutgestProfileRepository outgestProfileRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private MediaStreamRepository mediaStreamRepository;
    @Autowired
    private SchedulingService schedulingService;

    @GetMapping("/outgest/profiles")
    Page<OutgestProfile> all(Pageable pageable) {
        return outgestProfileRepository.findAll(pageable);
    }

    @PostMapping("/outgest")
    Task triggerOutgest(@RequestBody TriggerOutgestParameters parameters, Principal caller) {

        var outgestProfile = outgestProfileRepository.findById(parameters.outgestProfileId).get();
        var destinationLocation = locationRepository.findById(parameters.destinationLocationId).get();
        var media = mediaRepository.findById(parameters.mediaId).get();
        var result = schedulingService.createFFMpegOutgestTask(outgestProfile,
                media,
                parameters.videoStreamIds.stream().map(s -> mediaStreamRepository.findById(s).get()).collect(Collectors.toList()),
                parameters.audioStreamIds.stream().map(s -> mediaStreamRepository.findById(s).get()).collect(Collectors.toList()),
                Arrays.asList(),
                destinationLocation,
                caller.getName()
        );

        return result;
    }

    private static class TriggerOutgestParameters {
        public long locationId;
        public long outgestProfileId;
        public long mediaId;
        public List<Long> videoStreamIds;
        public List<Long> audioStreamIds;
        public long destinationLocationId;
    }

}
