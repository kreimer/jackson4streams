package kreimer.jackson4streams;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.Stream;



public class StreamSupplierSerializer
extends StdSerializer<StreamSupplier> {

    public StreamSupplierSerializer() {
        super(StreamSupplier.class);
    }



    @SuppressWarnings("unchecked")
    @Override
    public void serialize(
        StreamSupplier value,
        JsonGenerator gen,
        SerializerProvider provider
    ) throws IOException {
        gen.writeStartArray();
        try(Stream stream = ((StreamSupplier<?>)value).get()) {
            stream.forEach(i -> {
                try {
                    provider.defaultSerializeValue(i, gen);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe.getMessage(), ioe);
                }
            });
            gen.writeEndArray();
        }
    }

}