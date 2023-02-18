package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findAll(Pageable pageable);

}