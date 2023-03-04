package openmam.mediamicroservice;

import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MediaStreamRepository;
import openmam.mediamicroservice.repositories.OutgestProfileRepository;
import openmam.mediamicroservice.services.SchedulingService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

@SpringBootTest
class MediaMicroserviceApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(MediaMicroserviceApplicationTests.class);

	@Autowired
	MediaRepository mediaRepository;
	@Autowired
	SchedulingService schedulingService;
	@Autowired
	MediaStreamRepository mediaStreamRepository;
	@Autowired
	OutgestProfileRepository outgestProfileRepository;
	@Autowired
	LocationRepository locationRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testSearchByMetadataValue() {
		var result = mediaRepository.findAllByMetadataValue( Pageable.ofSize(10), "status", "ACCEPTED");
		log.info("{} results", result.getTotalElements());
	}

	@Test
	void testCreateFFMpegOutgestTask() {
		var outgestProfile = outgestProfileRepository.findById(1L).get();
		var destinationLocation = locationRepository.findById(1L).get();
		var media = mediaRepository.findById(1052L).get();
		var result = schedulingService.createFFMpegOutgestTask(outgestProfile,
				media,
				Arrays.asList(mediaStreamRepository.findById(1102L).get()),
				Arrays.asList(mediaStreamRepository.findById(1103L).get()),
				Arrays.asList(),
				destinationLocation,
				"unit_test"
		);
		log.info("result: {}", result);
	}
}
