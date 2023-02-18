package openmam.mediamicroservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import openmam.mediamicroservice.entities.MediaStream;

import java.util.List;

public interface MediaStreamRepository extends JpaRepository<MediaStream, Long> {

    List<MediaStream> findByMediaElementId(long id);

}