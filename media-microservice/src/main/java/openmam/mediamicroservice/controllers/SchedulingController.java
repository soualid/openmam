package openmam.mediamicroservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.services.SchedulingService;

@RestController
class SchedulingController {

    private final SchedulingService schedulingService;
    private final Logger logger = LoggerFactory.getLogger(SchedulingController.class);

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/scheduling/lockJob/{type}")
    public Task scheduleTask(@PathVariable Task.Type type, @RequestParam String requestedBy) {
        logger.info(type.toString());
        return schedulingService.scheduleTask(type, requestedBy);
    }

}
