package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.Media;
import openmam.mediamicroservice.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = """
    SELECT t FROM Task t WHERE t.media.id = ?1 AND t.status IN ?2
    """)
    List<Task> findByStatuses(Long id, List<Task.Status> asList);
}