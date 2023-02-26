package openmam.mediamicroservice.security.repository;

import openmam.mediamicroservice.security.converters.UserToUserDTOConverter;
import openmam.mediamicroservice.security.dto.UserDTO;
import openmam.mediamicroservice.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    @Lazy
    UserRepository userRepository;

    public Page<UserDTO> findAllAsDTO(Pageable p) {;
        return userRepository.findAll(p).map(UserToUserDTOConverter::convert);
    }
}
