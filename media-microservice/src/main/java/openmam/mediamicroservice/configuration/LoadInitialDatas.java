package openmam.mediamicroservice.configuration;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import openmam.mediamicroservice.entities.Media;
import openmam.mediamicroservice.repositories.MediaRepository;

//@Configuration
class LoadInitialDatas {

  private static final Logger log = LoggerFactory.getLogger(LoadInitialDatas.class);

  @Bean
  CommandLineRunner initDatabase(MediaRepository mediaRepository,
                                 MediaElementRepository mediaElementRepository,
                                 LocationRepository locationRepository) {

    return args -> {

      mediaElementRepository.deleteAll();
      locationRepository.deleteAll();
      mediaRepository.deleteAll();
      var location = new Location();
      location.setName("SAN");
      location.setType(Location.Type.LOCAL);
      location.setPath("/Users/simonoualid/san");
      locationRepository.save(location);

      for (int i = 1; i < 10; i++) {
        var m = new Media();
        m.setName("Test media #" + i);
        log.info("Preloading " + mediaRepository.save(m));
      }
    };
  }
}