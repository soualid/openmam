package openmam.worker.dto;


import java.util.ArrayList;
import java.util.List;

public class FFMpegOutgestJobsInput {

    public List<FFMpegOutgestJobsInputStream> videoStreams = new ArrayList<>();
    public List<FFMpegOutgestJobsInputStream> audioStreams = new ArrayList<>();
    public List<FFMpegOutgestJobsInputStream> subtitleStreams = new ArrayList<>();
    public OutgestProfile outgestProfile;

    public static class FFMpegOutgestJobsInputStream {
        public Task.MediaStream stream;
        public Task.MediaElement element;
    }
}
