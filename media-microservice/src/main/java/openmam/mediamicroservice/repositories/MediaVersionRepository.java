package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Media;
import openmam.mediamicroservice.entities.MediaVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaVersionRepository extends JpaRepository<MediaVersion, Long> {

    Page<MediaVersion> findAll(Pageable pageable);

}