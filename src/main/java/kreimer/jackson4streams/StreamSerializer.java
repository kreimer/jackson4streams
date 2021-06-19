package kreimer.jackson4streams;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.Stream;



public class StreamSerializer
extends StdSerializer<Stream> {

    public StreamSerializer() {
        super(Stream.class);
    }



    @SuppressWarnings("unchecked")
    @Override
    public void serialize(
        Stream value,
        JsonGenerator gen,
        SerializerProvider provider
    ) throws IOException {
        gen.writeStartArray();
        value.forEach(i -> {
            try {
                provider.defaultSerializeValue(i, gen);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe.getMessage(), ioe);
            }
        });
        gen.writeEndArray();
        value.close();
    }
    
}