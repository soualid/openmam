package openmam.worker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FFProbeResponse {

  public Format format;
  public List<Stream> streams;

  @Override
  public String toString() {
    return "FFProbeResponse{" +
            "format=" + format +
            ", streams=" + streams +
            '}';
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Stream {
    public int index;
    public String codecName;
    public String codecLongName;
    public String codecType;
    public String codecTagString;
    public long width;
    public long height;

    @Override
    public String toString() {
      return "Stream{" +
              "index=" + index +
              ", codecName='" + codecName + '\'' +
              ", codecLongName='" + codecLongName + '\'' +
              ", codecType='" + codecType + '\'' +
              ", codecTagString='" + codecTagString + '\'' +
              ", width=" + width +
              ", height=" + height +
              '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Format {
    public String filename;
    public double duration;

    @Override
    public String toString() {
      return "Format{" +
              "filename='" + filename + '\'' +
              ", duration=" + duration +
              '}';
    }
  }
}