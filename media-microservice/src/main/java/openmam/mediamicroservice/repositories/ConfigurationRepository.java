package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Configuration;
import openmam.mediamicroservice.entities.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {


    Configuration findByKey(Configuration.Key key);
}