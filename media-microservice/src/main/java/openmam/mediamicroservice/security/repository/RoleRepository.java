package openmam.mediamicroservice.security.repository;

import openmam.mediamicroservice.security.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String role);
}
