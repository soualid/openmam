package openmam.mediamicroservice.security.repository;

import openmam.mediamicroservice.security.dto.UserDTO;
import openmam.mediamicroservice.security.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    public Page<UserDTO> findAllAsDTO(Pageable p);
    public Page<UserDTO> findByRolesWithUsernamePrefixAsDTO(Pageable p, long roleId, String prefix);
}
