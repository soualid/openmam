package openmam.mediamicroservice.activiti;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@ConditionalOnProperty(value="activiti.enabled", havingValue="true")
class ProcessStartingRestController {

    @Autowired
    private ProcessEngine processEngine;

    @RequestMapping(method = RequestMethod.POST, value = "/process/deploy")
    Deployment deploy(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) throws IOException {
        var dep = processEngine.getRepositoryService().createDeployment();
        dep.name(name);
        //dep.addInputStream(name, file.getInputStream());
        String text = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        dep.addString(name, text);
        var deployment = dep.deploy();
        return deployment;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/process/{processId}/history")
    List<HistoricActivityInstance> processHistory(@PathVariable String processId) throws IOException {
        return processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processId).list();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/process/{processId}/image")
    String image(@PathVariable String processId) throws IOException {
        var repositoryService = this.processEngine.getRepositoryService();
        var processInstance = this.processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processId).singleResult();
        String processDefinitionId = null;
        if (processInstance == null) {
            var historicalProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = historicalProcessInstance.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        // get process model
        var model = repositoryService.getBpmnModel(processDefinitionId);
        var definition = processEngine.getRepositoryService().getProcessDefinition(processDefinitionId);

        if (model != null && model.getLocationMap().size() > 0) {
            var generator = new DefaultProcessDiagramGenerator();
            //
            //processEngine.getRuntimeService().createExecutionQuery().processInstanceId(processInstance.getId()).singleResult().
            var is = generator.generateDiagram(model,
                    processInstance == null ? List.of("end_event") : processEngine.getRuntimeService().getActiveActivityIds(processInstance.getId()),
                    getHighLightedFlows((ProcessDefinitionEntity) definition, processId, processInstance != null));
            var b64 = Base64.getEncoder().encodeToString(IOUtils.toByteArray(is));

            return b64;
        }
        return null;
    }

    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinition,
            String processInstanceId,
            boolean running) {

        var historicActivityInstances = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();

        var historicActivityInstanceList = new ArrayList<String>();
        var highLightedFlows = new ArrayList<String>();
        var highLightedActivities = new ArrayList<String>();

        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        highLightedActivities.addAll(historicActivityInstanceList);

        // add current activities to list
        if (running) {
            highLightedActivities.addAll(processEngine.getRuntimeService().getActiveActivityIds(processInstanceId));
        }

        var model = processEngine.getRepositoryService().getBpmnModel(processDefinition.getId());

        if(model.getProcesses()!=null && model.getProcesses().size() > 0) {
            for(var process : model.getProcesses()) {
                highLightedFlows.addAll(getHighLightedFlowsInner(highLightedActivities, process));
            }
        }
        return highLightedFlows;
    }

    private List<String> getHighLightedFlowsInner(ArrayList<String> highLightedActivities, FlowElementsContainer process) {
        var sequenceFlows = new ArrayList<String>();
        if(process.getFlowElements()!=null && process.getFlowElements().size()>0) {
            for(FlowElement element : process.getFlowElements()) {
                if(element!=null && element instanceof SequenceFlow) {
                    String sourceRef = ((SequenceFlow) element).getSourceRef();
                    String targetRef = ((SequenceFlow) element).getTargetRef();

                    FlowElement srcElement = process.getFlowElement(sourceRef);
                    FlowElement targetElement = process.getFlowElement(targetRef);

                    if(drawLine(highLightedActivities, srcElement, targetElement)) {
                        sequenceFlows.add(element.getId());
                    }
                } else if (element instanceof SubProcess) {
                    getHighLightedFlowsInner(highLightedActivities, (SubProcess)element);
                }
            }
        }
        return sequenceFlows;
    }

    private boolean drawLine(ArrayList<String> highLightedActivities, FlowElement srcElement, FlowElement targetElement) {
        if(srcElement !=null && targetElement!=null) {
            if(highLightedActivities.contains(srcElement.getId())
                    && highLightedActivities.contains(targetElement.getId())) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/bpmn/test")
    byte[] launch() throws IOException {

        var dep = processEngine.getRepositoryService().createDeployment();
        dep.addClasspathResource("processes/my-process.bpmn20.xml");
        dep.deploy();
  /*
  var a = this.processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId("20002").singleResult();
  var aa=  a.getDiagramResourceName();
  InputStream imageStream = this.processEngine.getRepositoryService().getResourceAsStream(
          a.getDeploymentId(), aa);
return imageStream;
*/
        var asyncProcess = this.processEngine.getRuntimeService()
                .startProcessInstanceByKey("media-qc");
/*
        var asyncProcess = this.processEngine.getRuntimeService()
                .createProcessInstanceQuery().processInstanceId("40005").singleResult();
        */
        var repositoryService = this.processEngine.getRepositoryService();
        var processInstance = this.processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(asyncProcess.getId()).singleResult();

//processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId("40005").list()
        if (processInstance != null) {

            var tasks = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).list();
            var task = tasks.get(0);

            processEngine.getTaskService().complete(task.getId(), Map.of("status", "refused"));

            // get process model
            var model = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
            processEngine.getRuntimeService().getActiveActivityIds(processInstance.getId());
            /*
            var l = new BpmnAutoLayout(model);
            l.execute();
            */



            if (model != null && model.getLocationMap().size() > 0) {
                var generator = new DefaultProcessDiagramGenerator();
                //
                var is = generator.generateDiagram(model, processEngine.getRuntimeService().getActiveActivityIds(processInstance.getId()),
                        new ArrayList<>());
                return IOUtils.toByteArray(is);
            }
        }
        return null;
    }
}
  /*
  ProcessInstance asyncProcess = this.processEngine.getRuntimeService()
    .startProcessInstanceByKey("my-process");
  return Collections.singletonMap("executionId", asyncProcess.getId());

   */
