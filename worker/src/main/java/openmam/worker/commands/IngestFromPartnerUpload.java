package openmam.worker.commands;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import openmam.worker.dto.Task;
import openmam.worker.utils.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.List;

@ShellComponent
public class IngestFromPartnerUpload {
    private static Logger log = LoggerFactory.getLogger(IngestFromPartnerUpload.class.getName());

    public static void handleTask(Task result, ApplicationConfiguration applicationConfiguration)
    {

        var messageConverter = new MappingJackson2HttpMessageConverter();
        var configuration = messageConverter.getObjectMapper().convertValue(result.additionalJobInputs, IngestFromPartnerJobsInput.class);

        System.setProperty("aws.accessKeyId", configuration.sourceLocation.s3accessKey);
        System.setProperty("aws.secretKey", configuration.sourceLocation.s3accessSecret);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(configuration.sourceLocation.s3region)
                .build();
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        var destinationFilename = "received-upload-" + configuration.partnerUploadId;
        var destinationFile = result.destinationLocation.path + destinationFilename;
        var download = tm.download(configuration.sourceLocation.s3bucketName,
                configuration.sourceLocation.path + "uploaded-" + result.media.id + "-" + configuration.partnerId + "-" + configuration.partnerUploadId,
                new File(destinationFile));
        try {
            download.waitForCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // TODO cleanup bucket

        var scanTask = new Task();
        scanTask.media = result.media;
        scanTask.type = Task.TaskType.SCAN;
        scanTask.destinationLocation = result.destinationLocation;
        var additionalJobInputs = new IngestAssets.IngestAssetsAdditionalParameters();
        additionalJobInputs.scanPath = destinationFilename;
        scanTask.additionalJobInputs = messageConverter.getObjectMapper().convertValue(additionalJobInputs, JsonNode.class);
        IngestAssets.handleTask(scanTask, applicationConfiguration);
    }

    public static class VantageCreateJobResponse {
        public String jobIdentifier;
    }

    public static class IngestFromPartnerJobsInput {
        public long partnerUploadId;
        public long partnerId;

        public Task.Location sourceLocation;
    }
}