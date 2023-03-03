package openmam.mediamicroservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import openmam.mediamicroservice.entities.Location;

public class IngestFromPartnerJobsInput {
        public long partnerUploadId;
        public long partnerId;

        public JsonNode sourceLocation;
    }