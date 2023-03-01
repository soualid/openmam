
package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.repositories.TaskRepository;
import openmam.mediamicroservice.security.dto.UserDTO;
import openmam.mediamicroservice.security.entities.User;
import openmam.mediamicroservice.security.repository.RoleRepository;
import openmam.mediamicroservice.security.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    Page<UserDTO> all(Pageable pageable, String roleName) {
        return userRepository.findAllAsDTO(pageable);
    }

    @GetMapping("/users/role/{roleName}")
    List<UserDTO> allByRoles(Pageable pageable,
                             @PathVariable String roleName,
                             @RequestParam(required = false, defaultValue = "") String prefix) {
        var role = roleRepository.findByName(roleName);
        return userRepository.findByRolesWithUsernamePrefixAsDTO(pageable, role.getId(), prefix).getContent();
    }

}
