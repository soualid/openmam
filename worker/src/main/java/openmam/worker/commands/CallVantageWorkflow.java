package openmam.worker.commands;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import openmam.worker.dto.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ShellComponent
public class CallVantageWorkflow {
    private static Logger log = LoggerFactory.getLogger(CallVantageWorkflow.class.getName());

    public static void handleTask(Task result)
    {

        var messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(
                new ObjectMapper().setPropertyNamingStrategy(
                        new PropertyNamingStrategies.UpperCamelCaseStrategy()
                )
        );
        var configuration = messageConverter.getObjectMapper().convertValue(result.additionalJobInputs, VantageOpenMAMAdditionalJobsInput.class);
        var baseVantageURL = configuration.vantageBaseURL;
        var vantageWorkflowId = configuration.vantageWorkflowID;
        var url = baseVantageURL + "/Rest/Workflows/" + vantageWorkflowId + "/submit";
        var urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                .encode()
                .toUriString();

        try {
            var test = messageConverter.getObjectMapper().writeValueAsString(configuration.vantageWorkflowParameters);
            log.info(test);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var restTemplate = new RestTemplateBuilder()
                .additionalMessageConverters(messageConverter)
                .build();
        var response = restTemplate.postForObject(urlTemplate, configuration.vantageWorkflowParameters, VantageCreateJobResponse.class);
        log.info("Created vantage job {}", response.jobIdentifier);
    }

    public static class VantageCreateJobResponse {
        public String jobIdentifier;
    }

    public static class VantageOpenMAMAdditionalJobsInput {
        public String vantageBaseURL;
        public String vantageWorkflowID;
        public VantageWorkflowParameters vantageWorkflowParameters;
    }

    public static class VantageWorkflowParameters {
        public String jobName;
        public int priority = 0;
        public List<String> attachments;
        public List<String> labels;
        public List<VantageWorkflowMedia> medias;
        public List<VantageWorkflowVariable> variables;
    }

    public static class VantageWorkflowMedia {
        public String identifier;
        public List<String> files;
        public String name;
    }

    public static class VantageWorkflowVariable {
        public String identifier;
        public String defaultValue;
        public String typeCode;
        public String value;
        public String name;
    }
}