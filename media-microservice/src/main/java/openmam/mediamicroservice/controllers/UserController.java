
package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.repositories.TaskRepository;
import openmam.mediamicroservice.security.dto.UserDTO;
import openmam.mediamicroservice.security.entities.User;
import openmam.mediamicroservice.security.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    Page<UserDTO> all(Pageable pageable) {
        return userRepository.findAllAsDTO(pageable);
    }

}
