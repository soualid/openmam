package openmam.worker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutgestProfile {
    public String command;

    public Long numberOfAudioStreams;
    public Long numberOfVideoStreams;
    public Long numberOfSubtitlesStreams;
}
