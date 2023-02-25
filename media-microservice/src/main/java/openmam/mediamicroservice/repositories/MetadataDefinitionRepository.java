package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.MetadataDefinition;
import openmam.mediamicroservice.entities.MetadataGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataDefinitionRepository extends JpaRepository<MetadataDefinition, Long> {

}