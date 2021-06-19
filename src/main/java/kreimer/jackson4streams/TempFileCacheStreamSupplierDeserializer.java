package kreimer.jackson4streams;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@Scope(scopeName = "prototype")
public class TempFileCacheStreamSupplierDeserializer
extends StdDeserializer<StreamSupplier>
implements ContextualDeserializer, ApplicationContextAware {

    protected ThreadLocal<JavaType> contentType = new InheritableThreadLocal<>();

    protected boolean reuseElement;

    protected ApplicationContext applicationContext;



    public TempFileCacheStreamSupplierDeserializer() {
        super(StreamSupplier.class);
    }



    public TempFileCacheStreamSupplierDeserializer(boolean reuseElement) {
        this();
        this.reuseElement = reuseElement;
    }



    @Override
    public StreamSupplier deserialize(
        JsonParser p,
        DeserializationContext ctx
    ) throws IOException, JsonProcessingException {
        return null;
    }



    @Override
    public JsonDeserializer<?> createContextual(
        DeserializationContext ctx,
        BeanProperty prop
    ) throws JsonMappingException {
        JavaType type = ctx.getContextualType() != null
            ? ctx.getContextualType()
            : prop.getMember().getType();
        JsonDeserializer<?> deserializer;
        if(type.getRawClass().isAssignableFrom(StreamSupplier.class)) {
            deserializer = new CtxDeserializer(type.containedType(0));
        } else {
            deserializer = ctx.findRootValueDeserializer(type);
        }
        return deserializer;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
        this.applicationContext = applicationContext;
    }



    class CtxDeserializer
    extends JsonDeserializer<StreamSupplier> {

        JavaType contentType;

        CtxDeserializer(JavaType contentType) {
            this.contentType = contentType;
        }



        @Override
        public StreamSupplier deserialize(
            JsonParser p,
            DeserializationContext ctx
        ) throws IOException, JsonProcessingException {
            if (p.currentToken() == JsonToken.START_ARRAY) {
                File cache = File.createTempFile("stream_", null);
                cache.deleteOnExit();
                FileOutputStream output = new FileOutputStream(cache);
                JsonFactory jfactory = new JsonFactory();
                JsonGenerator cacheGenerator = jfactory.createGenerator(
                    output, JsonEncoding.UTF8
                );
                cacheGenerator.writeStartArray();
                JsonToken token;
                int arrayDepth = 1;
                while(
                    (token = p.nextToken())!=null
                    && (
                        token!=JsonToken.END_ARRAY
                        || arrayDepth>0
                    )
                ) {
                    switch(token) {
                        case START_ARRAY:
                            arrayDepth++;
                            if(arrayDepth>1) {
                                cacheGenerator.writeStartArray();
                            }
                            break;
                        case START_OBJECT:
                                cacheGenerator.writeStartObject();
                            break;
                        case FIELD_NAME:
                            cacheGenerator.writeFieldName(p.currentName());
                            break;
                        case VALUE_FALSE:
                            cacheGenerator.writeBoolean(false);
                            break;
                        case VALUE_TRUE:
                            cacheGenerator.writeBoolean(true);
                            break;
                        case VALUE_STRING:
                            cacheGenerator.writeString(p.getValueAsString());
                            break;
                        case VALUE_NUMBER_INT:
                            cacheGenerator.writeNumber(p.getIntValue());
                            break;
                        case VALUE_NUMBER_FLOAT:
                            cacheGenerator.writeNumber(new BigDecimal(p.getText()));
                            break;
                        case VALUE_NULL:
                            cacheGenerator.writeNull();
                            break;
                        case END_OBJECT:
                            if(arrayDepth>0) {
                                cacheGenerator.writeEndObject();
                            }
                            break;
                        case END_ARRAY:
                            if(arrayDepth>1) {
                                cacheGenerator.writeEndArray();
                            }
                            arrayDepth--;
                            break;
                        default:
                            break;
                    }
                }
                cacheGenerator.writeEndArray();
                cacheGenerator.flush();
                cacheGenerator.close();
                final JavaType type = contentType;
                return new TempFileCache(
                    cache,
                    type,
                    applicationContext.getBean(ObjectMapper.class),
                    reuseElement,
                    false
                );
            }
            return null;
        }



        @Override
        public boolean isCachable() {
            return false;
        }

    }

}