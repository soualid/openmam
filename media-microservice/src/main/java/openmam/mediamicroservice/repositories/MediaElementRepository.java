package openmam.mediamicroservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import openmam.mediamicroservice.entities.MediaElement;

import java.util.List;

public interface MediaElementRepository extends JpaRepository<MediaElement, Long> {

    List<MediaElement> findByMediaId(long id);

}