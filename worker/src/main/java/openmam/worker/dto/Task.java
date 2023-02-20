package openmam.worker.dto;

import javax.print.attribute.standard.Media;
import java.util.List;

public class Task {
    public long id;
    public MediaStream mediaStream;
    public MediaElement mediaElement;
    public Media media;
    public Location destinationLocation;
    public TaskType type;

    public enum MediaStreamType {
        AUDIO, VIDEO, SUBTITLE
    }
    public enum LocationType {
        LOCAL, S3
    }
    public enum TaskType {
        SCAN,
        GENERATE_VARIANTS,
        MOVE_ASSET
    }

    public static class MediaStream {

        public long id;
        public int streamIndex;

        public MediaStreamType type;

    }

    public static class MediaElement {

        public long id;
        public String filename;
        public Location location;

    }

    public static class Media {

        public long id;
        public List<MediaElement> elements;

    }

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


