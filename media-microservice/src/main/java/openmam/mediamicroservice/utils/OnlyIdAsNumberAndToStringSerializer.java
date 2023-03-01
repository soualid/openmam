package openmam.mediamicroservice.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class OnlyIdAsNumberAndToStringSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object object,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
                          throws IOException, JsonProcessingException {
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor("id", object.getClass());
            var getter = pd.getReadMethod();
            var value = getter.invoke(object);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("id");
            jsonGenerator.writeNumber((long)value);
            jsonGenerator.writeFieldName("value");
            jsonGenerator.writeString(object.toString());
            jsonGenerator.writeEndObject();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}