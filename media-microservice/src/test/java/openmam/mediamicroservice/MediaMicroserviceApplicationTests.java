package openmam.mediamicroservice;

import openmam.mediamicroservice.repositories.MediaRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
class MediaMicroserviceApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(MediaMicroserviceApplicationTests.class);

	@Autowired
	MediaRepository mediaRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testSearchByMetadataValue() {
		var result = mediaRepository.findAllByMetadataValue( Pageable.ofSize(10), "status", "ACCEPTED");
		log.info("{} results", result.getTotalElements());
	}
}
