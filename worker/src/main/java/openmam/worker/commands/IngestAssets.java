package openmam.worker.commands;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import openmam.worker.dto.FFProbeResponse;
import openmam.worker.dto.Task;
import openmam.worker.utils.ApplicationConfiguration;
import openmam.worker.utils.Finder;
import org.buildobjects.process.ProcBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static openmam.worker.WorkerApplication.currentAccessToken;


@ShellComponent
public class IngestAssets {

    private static Logger logger = Logger.getLogger(IngestAssets.class.getName());

    public static void handleTask(Task task, ApplicationConfiguration applicationConfiguration) {
        var messageConverter = new MappingJackson2HttpMessageConverter();
        var configuration = messageConverter.getObjectMapper().convertValue(task.additionalJobInputs, IngestAssetsAdditionalParameters.class);

        // TODO scan from S3
        var location = task.destinationLocation;
        if (location.type == Task.LocationType.LOCAL) {
            logger.info("Looking in " + location.path);
            Finder finder = new Finder(configuration.scanPath);
            try {
                Files.walkFileTree(Path.of(location.path), finder);
                var results = finder.done();
                logger.info("results: " + results);
                var mapper = new ObjectMapper();
                var streams = new ArrayList<Task.MediaStream>();
                for (var result : results) {

                    // TODO check path
                    String output = ProcBuilder.run("ffprobe", "-v", "quiet", "-show_format", "-show_streams", "-print_format", "json", result.getAbsolutePath());
                    logger.info("output: " + output);

                    var response = mapper.readValue(output, FFProbeResponse.class);
                    logger.info("output: " + response);

                    var mediaElement = new Task.MediaElement();
                    mediaElement.filename = result.getName();
                    mediaElement.location = location;
                    mediaElement.fullMetadatas = mapper.readTree(output);

                    for (var stream : response.streams) {

                        var mediaStream = new Task.MediaStream();
                        mediaStream.status = Task.MediaStreamStatus.LOCKED;
                        mediaStream.codecLongName = stream.codecLongName;
                        mediaStream.codecName = stream.codecName;
                        mediaStream.codecTagString = stream.codecTagString;
                        mediaStream.codecType = stream.codecType;

                        var streamType = switch (stream.codecType) {
                            case "video":
                                yield Task.MediaStreamType.VIDEO;
                            case "audio":
                                yield Task.MediaStreamType.AUDIO;
                            case "subtitle":
                                yield Task.MediaStreamType.SUBTITLE;
                            default:
                                throw new RuntimeException("unknown type");
                        };

                        mediaStream.type = streamType;
                        mediaStream.streamIndex = stream.index;

                        streams.add(mediaStream);
                    }

                    var url = applicationConfiguration.mediaServiceHost + "/media/" + task.media.id + "/scanCallback";
                    var urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                            .encode()
                            .toUriString();

                    var restTemplate = new RestTemplateBuilder()
                            .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                            .build();
                    var headers = new HttpHeaders();
                    headers.setBearerAuth(currentAccessToken);

                    var callbackData = new IngestAssetsCallbackData();
                    callbackData.mediaStreams = streams;
                    callbackData.mediaElement = mediaElement;
                    restTemplate.postForObject(urlTemplate, new HttpEntity<>(callbackData, headers), Task.class);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static class IngestAssetsAdditionalParameters {
        public String scanPath;
    }

    public static class IngestAssetsCallbackData {
        public Task.MediaElement mediaElement;
        public List<Task.MediaStream> mediaStreams;
    }

}