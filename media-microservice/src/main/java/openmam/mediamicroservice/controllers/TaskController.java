
package openmam.mediamicroservice.controllers;

import openmam.mediamicroservice.entities.Location;
import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskController {

    private final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/tasks")
    Page<Task> all(Pageable pageable) {
        
        return taskRepository.findAll(pageable);
    }

}
