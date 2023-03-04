package openmam.worker.commands;

import openmam.worker.WorkerApplication;
import openmam.worker.dto.AuthenticationRequest;
import openmam.worker.dto.AuthenticationResponse;
import openmam.worker.dto.Task;
import openmam.worker.utils.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.String.format;

@ShellComponent
public class PollForTasks {
    private final Logger logger = LoggerFactory.getLogger(PollForTasks.class);

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @ShellMethod(key = "poll-for-tasks", value = "poll for tasks")
    public void pollForTasks()
    {
        var restTemplate = new RestTemplateBuilder()
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        while (true) {
            try {
                Thread.sleep(1000);

                var url = applicationConfiguration.mediaServiceHost + "/authenticate";
                String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                        .encode()
                        .toUriString();
                var authResult = restTemplate.postForObject(urlTemplate,
                        new AuthenticationRequest(applicationConfiguration.openMamLogin, applicationConfiguration.openMamPassword),
                        AuthenticationResponse.class);

                WorkerApplication.currentAccessToken = authResult.accessToken;
                var headers = new HttpHeaders();
                headers.setBearerAuth(authResult.accessToken);

                url = applicationConfiguration.mediaServiceHost + "/scheduling/lockNextJob";

                var hostname = InetAddress.getLocalHost().getHostName();
                urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("requestedBy", hostname)
                        .encode()
                        .toUriString();
                var result = restTemplate.postForObject(urlTemplate, new HttpEntity<>(null, headers), Task.class);

                if (result == null) {
                    logger.info("No more tasks to be done, waiting...");
                    continue;
                }
                logger.info("Handling #{} of type {}", result.id, result.type);
                switch (result.type) {
                    case GENERATE_VARIANTS -> GenerateVariants.handleTask(result);
                    case MOVE_ASSET -> MoveAssets.handleTask(result);
                    case SCAN -> IngestAssets.handleTask(result, applicationConfiguration);
                    case INGEST_PARTNER_UPLOAD -> IngestFromPartnerUpload.handleTask(result, applicationConfiguration);
                    case FFMPEG_OUTGEST -> FFMpegOutgest.handleTask(result);
                }

                url = applicationConfiguration.mediaServiceHost + "/scheduling/endJob/" + result.id;
                urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                        .encode()
                        .toUriString();

                restTemplate = new RestTemplateBuilder()
                        .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                        .build();
                restTemplate.postForObject(urlTemplate, new HttpEntity<>(null, headers), Task.class);

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}