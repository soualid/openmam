package openmam.worker.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import openmam.worker.dto.FFMpegOutgestJobsInput;
import openmam.worker.dto.Task;
import org.buildobjects.process.ProcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;

import java.util.ArrayList;

@ShellComponent
public class FFMpegOutgest {
    private static Logger log = LoggerFactory.getLogger(FFMpegOutgest.class);


    public static void handleTask(Task result)
    {
        var additionalParameters = new ObjectMapper().convertValue(result.additionalJobInputs, FFMpegOutgestJobsInput.class);
        log.info("additional parameters: {}", additionalParameters);
        var commandParts = additionalParameters.outgestProfile.command.split(" ");
        var filledCommandParts = new ArrayList<String>();
        for (var commandPart: commandParts) {
            var start = commandPart.indexOf("%");
            if (start > -1) {
                var code = commandPart.substring(start, commandPart.length());
                log.info("code {}", code);
                if (code.startsWith("%vsi")) {
                    var trackNumber = code.substring(4);
                    log.info("trackNumber {}", trackNumber);
                    commandPart = commandPart.substring(0, start);
                    var stream = additionalParameters.videoStreams.get(Integer.parseInt(trackNumber)-1);
                    commandPart += stream.stream.streamIndex;
                } else if (code.startsWith("%asi")) {
                    var trackNumber = code.substring(4);
                    log.info("trackNumber {}", trackNumber);
                    commandPart = commandPart.substring(0, start);
                    var stream = additionalParameters.audioStreams.get(Integer.parseInt(trackNumber)-1);
                    // TODO S3
                    commandPart += stream.stream.streamIndex;
                } else if (code.startsWith("%v")) {
                    var trackNumber = code.substring(2);
                    log.info("trackNumber {}", trackNumber);
                    commandPart = commandPart.substring(0, start);
                    var stream = additionalParameters.videoStreams.get(Integer.parseInt(trackNumber)-1);
                    // TODO S3
                    commandPart += stream.element.location.path + stream.element.filename;
                } else if (code.startsWith("%a")) {
                    var trackNumber = code.substring(2);
                    log.info("trackNumber {}", trackNumber);
                    commandPart = commandPart.substring(0, start);
                    var stream = additionalParameters.audioStreams.get(Integer.parseInt(trackNumber)-1);
                    // TODO S3
                    commandPart += stream.element.location.path + stream.element.filename;
                } else if (code.startsWith("%output")) {
                    commandPart = result.destinationLocation.path
                            + "outgest-"
                            + result.id
                            + commandPart.substring(7);
                }
                // TODO subtitles
            }
            filledCommandParts.add(commandPart);
        }
        log.info("filledCommandParts: {}", filledCommandParts);
        var command = filledCommandParts.get(0);
        filledCommandParts.remove(0);
        var output = new ProcBuilder(command, filledCommandParts.toArray(new String[filledCommandParts.size()]))
                .withNoTimeout().run();
        log.info("output {}", output);

    }

}