package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {


}