package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.Media;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.security.entities.Role;
import openmam.mediamicroservice.security.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Comparator;

@RestController
class DashboardController {

    private final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public DashboardController(MediaRepository mediaRepository, UserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    Page<Media> all(Principal caller, Pageable pageable) {
        var user = userRepository.findByEmail(caller.getName());
        var role = user.get().getRoles().stream().max(Comparator.comparing(Role::getPriority)).get();
        return mediaRepository.findAllByMetadataValue(pageable,
                role.getDashboardMetadataFilter(),
                role.getDashboardMetadataFilterValue());
    }

}
