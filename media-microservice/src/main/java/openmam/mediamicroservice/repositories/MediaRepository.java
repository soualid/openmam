package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findAll(Pageable pageable);

    @Query(value = """
            SELECT * FROM media where dynamic_metadatas ->> ?1 = ?2            
            """, nativeQuery = true
    )
    Page<Media> findAllByMetadataValue(Pageable pageable, String metadataKey, String metadataValue);
}