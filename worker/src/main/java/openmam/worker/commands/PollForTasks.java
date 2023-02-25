package openmam.worker.commands;

import openmam.worker.dto.Task;
import org.buildobjects.process.ProcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

import static java.lang.String.format;

@ShellComponent
public class PollForTasks {
    private final Logger logger = LoggerFactory.getLogger(PollForTasks.class);
    private static final String mediaServiceHost = "http://localhost:8080";

    @ShellMethod(key = "poll-for-tasks", value = "poll for tasks")
    public void pollForTasks()
    {
        while (true) {
            try {
                Thread.sleep(1000);
                var url = mediaServiceHost + "/scheduling/lockNextJob";
                var hostname = InetAddress.getLocalHost().getHostName();
                String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("requestedBy", hostname)
                        .encode()
                        .toUriString();

                var restTemplate = new RestTemplateBuilder()
                        .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                        .build();
                var result = restTemplate.postForObject(urlTemplate, null, Task.class);

                if (result == null) {
                    logger.info("No more tasks to be done, waiting...");
                    continue;
                }
                logger.info("Handling #{} of type {}", result.id, result.type);
                switch (result.type) {
                    case GENERATE_VARIANTS -> GenerateVariants.handleTask(result);
                    case MOVE_ASSET -> MoveAssets.handleTask(result);
                }

                url = mediaServiceHost + "/scheduling/endJob/" + result.id;
                urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                        .encode()
                        .toUriString();

                restTemplate = new RestTemplateBuilder()
                        .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                        .build();
                restTemplate.postForObject(urlTemplate, null, Task.class);

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}