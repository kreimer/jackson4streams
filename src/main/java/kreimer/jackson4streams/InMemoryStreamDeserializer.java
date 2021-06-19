package kreimer.jackson4streams;

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
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.util.List;
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
public class InMemoryStreamDeserializer
extends StdDeserializer<Stream>
implements ContextualDeserializer, ApplicationContextAware {

    protected ApplicationContext applicationContext;



    public InMemoryStreamDeserializer() {
        super(Stream.class);
    }



    @Override
    public Stream deserialize(
        JsonParser p,
        DeserializationContext ctx
    ) throws IOException, JsonProcessingException {
        return null;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
        this.applicationContext = applicationContext;
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
        if(type.getRawClass().isAssignableFrom(Stream.class)) {
            deserializer = new CtxDeserializer(type.containedType(0));
        } else {
            deserializer = ctx.findRootValueDeserializer(type);
        }
        return deserializer;
    }



    class CtxDeserializer
    extends JsonDeserializer<Stream> {

        JavaType contentType;

        CtxDeserializer(JavaType contentType) {
            this.contentType = contentType;
        }



        @Override
        public Stream deserialize(
            JsonParser p,
            DeserializationContext ctx
        ) throws IOException, JsonProcessingException {
            ObjectMapper mapper = applicationContext
                .getBean(ObjectMapper.class);
            if (p.currentToken() == JsonToken.START_ARRAY) {
                CollectionType collectionType = mapper
                    .getDeserializationConfig()
                    .getTypeFactory()
                    .constructCollectionType(
                        List.class,
                        contentType
                    );
                List results= mapper.readValue(p, collectionType);
                return results.stream();
            }
            return null;
        }



        @Override
        public boolean isCachable() {
            return false;
        }

    }

}