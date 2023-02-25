package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.MetadataDefinition;
import openmam.mediamicroservice.entities.MetadataReference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataReferenceRepository extends JpaRepository<MetadataReference, Long> {

}