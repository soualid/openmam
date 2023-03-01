package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocationRepository extends JpaRepository<Location, Long> {


    @Query("""
            SELECT l FROM Location l WHERE l.hidden = false
            """)
    Page<Location> findAllPublicLocations(Pageable pageable);
}