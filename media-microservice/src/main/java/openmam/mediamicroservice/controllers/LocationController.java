package openmam.mediamicroservice.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.MetadataDefinition;
import openmam.mediamicroservice.entities.MetadataGroup;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MetadataDefinitionRepository;
import openmam.mediamicroservice.repositories.MetadataGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
class LocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping("/locations")
    Page<Location> all(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

}
