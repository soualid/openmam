package openmam.worker.dto;

import javax.print.attribute.standard.Media;

public class Task {
    public long id;
    public MediaStream mediaStream;
    public MediaElement mediaElement;
    public Media media;
    public Location destinationLocation;

    public enum Type {
        AUDIO, VIDEO, SUBTITLE
    }

    public static class MediaStream {

        public long id;
        public int streamIndex;

        public Type type;

    }

    public static class MediaElement {

        public long id;
        public String filename;
        public Location location;

    }

    public static class Media {

        public long id;

    }

    public static class Location {

        public long id;
        public String path;

    }
}


