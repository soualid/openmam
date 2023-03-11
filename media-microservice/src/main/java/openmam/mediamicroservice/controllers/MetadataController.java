package openmam.mediamicroservice.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import openmam.mediamicroservice.entities.*;
import openmam.mediamicroservice.repositories.*;
import openmam.mediamicroservice.services.SchedulingService;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
class MetadataController {

    private final Logger logger = LoggerFactory.getLogger(MetadataController.class);
    private final MetadataGroupRepository metadataGroupRepository;
    private final MetadataDefinitionRepository metadataDefinitionRepository;
    private final MediaRepository mediaRepository;
    private final MediaVersionRepository mediaVersionRepository;
    private final MetadataReferenceRepository metadataReferenceRepository;

    @Autowired(required = false)
    private ProcessEngine processEngine;

    public MetadataController(MetadataGroupRepository metadataGroupRepository,
                              MetadataDefinitionRepository metadataDefinitionRepository,
                              MetadataReferenceRepository metadataReferenceRepository,
                              MediaRepository mediaRepository,
                              MediaVersionRepository mediaVersionRepository) {
        this.mediaRepository = mediaRepository;
        this.metadataGroupRepository = metadataGroupRepository;
        this.metadataReferenceRepository = metadataReferenceRepository;
        this.metadataDefinitionRepository = metadataDefinitionRepository;
        this.mediaVersionRepository = mediaVersionRepository;
    }

    @PostMapping("/metadataGroup")
    public MetadataGroup createMetadataGroup(@RequestBody MetadataGroup group) {
        return metadataGroupRepository.save(group);
    }

    @PostMapping("/metadataReference/{metadataGroupId}")
    public MetadataReference createMetadataRereference(@PathVariable long metadataGroupId,
                                                   @RequestBody MetadataReference reference) {
        //referencedMetadataGroup
        var metadataGroup = metadataGroupRepository.getReferenceById(metadataGroupId);
        reference.setReferencedMetadataGroup(metadataGroup);
        return metadataReferenceRepository.save(reference);
    }

    @GetMapping("/metadataGroup/{id}")
    MetadataGroup one(@PathVariable Long id) {

        return metadataGroupRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException(id));
    }

    @PostMapping("/metadata")
    public void persistMetadatasForEntity(@RequestBody JsonNode datas) {
        logger.info("{}", datas.toPrettyString());
        datas.fieldNames().forEachRemaining((s) -> {
            var data = datas.get(s);
            logger.info("{} {}", s, data);
            if (data.get("targetType").asText().equals(MetadataGroup.Attachment.MEDIA.toString())) {
                var media = this.mediaRepository.getReferenceById(data.get("targetId").asLong());
                if (media.getActivitiProcessId() != null && processEngine != null) {
                    data.fields().forEachRemaining(e -> {
                        var key = e.getKey();
                        logger.info("checking {}!", key);
                        if (media.getDynamicMetadatas() == null ||
                                (data.has(key) && media.getDynamicMetadatas().has(key) && data.get(key).textValue() != null &&
                                !data.get(key).textValue().equals(media.getDynamicMetadatas().get(key).textValue()))) {
                            logger.info("{} changed!", key);
                            var metadataDefinition = metadataDefinitionRepository.findByName(key);
                            if (metadataDefinition != null && metadataDefinition.getWorkflowStepToTrigger() != null) {
                                logger.info("workflow step to trigger: {}", metadataDefinition.getWorkflowStepToTrigger());
                                var activeActivity = processEngine.getRuntimeService().getActiveActivityIds(media.getActivitiProcessId());
                                logger.info("active activity: {}", activeActivity);
                                if (activeActivity.size() > 0 && activeActivity.get(0).equals(metadataDefinition.getWorkflowStepToTrigger())) {
                                    logger.info("we need to trigger that workflow");
                                    var task = processEngine.getTaskService().createTaskQuery()
                                            .processInstanceId(media.getActivitiProcessId())
                                            .taskDefinitionKeyLike(metadataDefinition.getWorkflowStepToTrigger())
                                            .singleResult();
                                    processEngine.getTaskService().complete(task.getId(),
                                            Map.of("value", data.get(key).textValue()));
                                }
                            }
                        }
                    });
                }
                media.setDynamicMetadatas(data);
                mediaRepository.save(media);
            } else if (data.get("targetType").asText().equals(MetadataGroup.Attachment.MEDIA_VERSION.toString())) {
                this.mediaVersionRepository.findById(data.get("targetId").asLong()).ifPresent(mediaVersion -> {
                    mediaVersion.setDynamicMetadatas(data);
                    mediaVersionRepository.save(mediaVersion);
                });
            }
        });

    }

    @PostMapping("/metadataGroup/{metadataGroupId}/metadataDefinition")
    public MetadataDefinition createMetadataDefinition(@RequestBody MetadataDefinition metadataDefinition,
                                                       @PathVariable long metadataGroupId,
                                                       @RequestParam(required = false) Long referencedMetadataGroupId) {
        var group = metadataGroupRepository.getReferenceById(metadataGroupId);
        metadataDefinition.setMetadataGroup(group);
        if (group.getAttachmentType() == MetadataGroup.Attachment.REFERENCEABLE) {
            var referencedGroup = metadataGroupRepository.getReferenceById(referencedMetadataGroupId);
            metadataDefinition.setReferencedMetadataGroup(referencedGroup);
        }
        return metadataDefinitionRepository.save(metadataDefinition);
    }

    @GetMapping("/metadataGroups")
    Page<MetadataGroup> all(Pageable pageable) {
        return metadataGroupRepository.findAll(pageable);
    }

    @GetMapping("/metadataGroup/{groupId}/autocomplete")
    List<MetadataReference> autocomplete(@PathVariable long groupId) {
        var result = metadataReferenceRepository.findAll();
        var group = metadataGroupRepository.getReferenceById(groupId);
        var includableFieldsForRepresentation = group.getMetadatas().stream()
                .filter(m -> m.isSearchable())
                .map(m -> m.getName()).collect(Collectors.toList());
        for (var reference: result) {
            var representation = new ArrayList<String>();
            for (var fieldName : includableFieldsForRepresentation) {
                representation.add(reference.getDynamicMetadatas().get(fieldName).asText());
            }
            reference.setRepresentation(String.join(" ", representation));
        }
        return result;
    }


}
