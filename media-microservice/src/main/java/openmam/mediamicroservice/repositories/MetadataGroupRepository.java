package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.MetadataGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataGroupRepository extends JpaRepository<MetadataGroup, Long> {


}