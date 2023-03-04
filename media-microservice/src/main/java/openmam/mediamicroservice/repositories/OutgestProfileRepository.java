package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.OutgestProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutgestProfileRepository extends JpaRepository<OutgestProfile, Long> {

}