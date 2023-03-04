package openmam.worker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import javax.print.attribute.standard.Media;
import java.util.List;

public class Task {
    public long id;
    public MediaStream mediaStream;
    public MediaElement mediaElement;
    public Media media;
    public Location destinationLocation;
    public TaskType type;

    public JsonNode additionalJobInputs;

    public enum MediaStreamType {
        AUDIO, VIDEO, SUBTITLE
    }
    public enum LocationType {
        LOCAL, S3
    }
    public enum TaskType {
        SCAN,
        GENERATE_VARIANTS,
        INGEST_PARTNER_UPLOAD,
        MOVE_ASSET,
        FFMPEG_OUTGEST
    }
    public enum MediaStreamStatus {
        LOCKED
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaStream {

        public long id;
        public int streamIndex;
        public MediaStreamStatus status;
        public MediaStreamType type;

        public String codecLongName;
        public String codecName;
        public String codecTagString;
        public String codecType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaElement {

        public long id;
        public String filename;
        public Location location;

        public JsonNode fullMetadatas;
    }

    public static class Media {

        public long id;
        public List<MediaElement> elements;

        public long streamCount;
        public Long videoStreamId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {

        public long id;
        public String path;
        public String s3accessKey;
        public String s3accessSecret;
        public String s3region;
        public String s3bucketName;
        public LocationType type;
    }
}


