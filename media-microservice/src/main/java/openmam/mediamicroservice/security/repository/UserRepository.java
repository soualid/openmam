package openmam.mediamicroservice.security.repository;

import openmam.mediamicroservice.security.entities.Role;
import openmam.mediamicroservice.security.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    @Query(value = """
    SELECT u 
    FROM User u JOIN u.roles r 
    WHERE r.id = ?1 
    AND (
        LOWER(u.email) LIKE LOWER(CONCAT('%', ?2,'%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', ?2,'%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', ?2,'%'))
    )
    """
    )
    Page<User> findByRolesWithUsernamePrefix(Pageable p, long roleId, String prefix);
}
