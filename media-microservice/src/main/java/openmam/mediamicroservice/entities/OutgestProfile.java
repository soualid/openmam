package openmam.mediamicroservice.entities;

import jakarta.persistence.*;

@Entity
public class OutgestProfile {

    public enum Type {
        FFMPEG, VANTAGE
    }

    private @Id
    @GeneratedValue Long id;

    private Long numberOfAudioStreams;
    private Long numberOfVideoStreams;
    private Long numberOfSubtitlesStreams;

    private String name;
    private String command;


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumberOfAudioStreams() {
        return numberOfAudioStreams;
    }

    public void setNumberOfAudioStreams(Long numberOfAudioStreams) {
        this.numberOfAudioStreams = numberOfAudioStreams;
    }

    public Long getNumberOfVideoStreams() {
        return numberOfVideoStreams;
    }

    public void setNumberOfVideoStreams(Long numberOfVideoStreams) {
        this.numberOfVideoStreams = numberOfVideoStreams;
    }

    public Long getNumberOfSubtitlesStreams() {
        return numberOfSubtitlesStreams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfSubtitlesStreams(Long numberOfSubtitlesStreams) {
        this.numberOfSubtitlesStreams = numberOfSubtitlesStreams;
    }
}
