package openmam.mediamicroservice.security.repository;

import openmam.mediamicroservice.security.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    public Page<UserDTO> findAllAsDTO(Pageable p);
}
