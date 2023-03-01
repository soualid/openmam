package openmam.mediamicroservice.repositories;

import openmam.mediamicroservice.entities.PartnerUploadRequest;
import openmam.mediamicroservice.entities.Task;
import openmam.mediamicroservice.security.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerUploadRequestRepository extends JpaRepository<PartnerUploadRequest, Long> {

    public Page<PartnerUploadRequest> findByPartner(User partner, Pageable page);
}