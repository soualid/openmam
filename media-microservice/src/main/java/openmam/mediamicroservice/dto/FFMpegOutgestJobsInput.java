package openmam.mediamicroservice.dto;

import openmam.mediamicroservice.entities.MediaElement;
import openmam.mediamicroservice.entities.MediaStream;
import openmam.mediamicroservice.entities.OutgestProfile;

import java.util.ArrayList;
import java.util.List;

public class FFMpegOutgestJobsInput {

    public List<FFMpegOutgestJobsInputStream> videoStreams = new ArrayList<>();
    public List<FFMpegOutgestJobsInputStream> audioStreams = new ArrayList<>();
    public List<FFMpegOutgestJobsInputStream> subtitleStreams = new ArrayList<>();
    public OutgestProfile outgestProfile;

    public static class FFMpegOutgestJobsInputStream {
        public MediaStream stream;
        public MediaElement element;
    }
}
