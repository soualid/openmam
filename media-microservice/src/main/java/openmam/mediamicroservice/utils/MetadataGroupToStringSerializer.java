package openmam.mediamicroservice.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import openmam.mediamicroservice.entities.MetadataGroup;

import java.io.IOException;

public class MetadataGroupToStringSerializer extends JsonSerializer<MetadataGroup> {

    @Override
    public void serialize(MetadataGroup tmpMetadataGroup,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
                          throws IOException, JsonProcessingException {
        jsonGenerator.writeString(tmpMetadataGroup.getName());
    }
}