package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {


}