package kreimer.jackson4streams;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Builder
public class TempFileCache<T> 
implements StreamSupplier<T> {
    
    protected File cache;

    protected JavaType elementType;

    protected ObjectMapper mapper;

    protected boolean reuseElement;

    protected boolean deleteFileOnClose;

    

    @Override
    public Stream<T> get() {
        if(cache==null) {
            throw new RuntimeException("Cache was closed and deleted!");
        }
        Iterator iterator;
        if(reuseElement) {
            try {
                iterator = new JsonArrayReuseElementIterator(
                    new FileInputStream(cache),
                    mapper,
                    elementType
                );
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            try {
                iterator = new JsonArrayIterator(
                    new FileInputStream(cache),
                    mapper,
                    elementType
                );
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        Stream s = StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL),
            false
        );
        s.onClose(() -> {
            try {
                if(deleteFileOnClose) {
                    log.debug("deleting cache....");
                    cache.delete();
                    cache = null;
                }
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return s;
    }

    
    
    @Override
    public void close() throws IOException {
        try {
            log.debug("deleting cache....");
            cache.delete();
            cache = null;
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    
    

    @Slf4j
    public static class JsonArrayIterator
    implements Iterator<Object> {

        protected InputStream input;

        protected Object next;

        protected JsonParser parser;

        protected JavaType elementType;

        protected ObjectMapper mapper;

        JsonToken token = null;

        protected int arrayDepth = 0;



        public JsonArrayIterator(
            InputStream input,
            ObjectMapper mapper,
            JavaType elementType
        ) throws IOException {
            this.input = input;
            this.mapper = mapper.copy();
            this.elementType = elementType;
            JsonFactory factory = new JsonFactory();
            parser = factory.createParser(input);
        }



        @Override
        public boolean hasNext() {
            if(next==null) {
                fetchNext();
            }
            if(next==null) {
                freeResources();
            }
            return next!=null;
        }



        @Override
        public Object next() {
            if(next!=null) {
                Object res = next;
                next = null;
                return res;
            } else {
                throw new NoSuchElementException();
            }
        }



        protected void fetchNext() {
            try {
                next = null;
                if(token==null) {
                    token = parser.nextToken();
                }
                while(
                    next==null
                    &&  token!=null
                ) {
                    switch(token) {
                        case START_ARRAY:
                            arrayDepth++;
                            token = parser.nextToken();
                            break;
                        case END_ARRAY:
                            arrayDepth--;
                            token = parser.nextToken();
                            break;
                        case START_OBJECT:
                            next = mapper.readValue(parser, elementType);
                            token = parser.currentToken();
                            break;
                        default:
                            token = parser.nextToken();
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }



        protected void freeResources() {
            try {
                parser.close();
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                input.close();
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }



    @Slf4j
    public static class CachingInstanceValueInstantiator
    extends StdValueInstantiator {

        protected Object cachedInstance = null;



        public CachingInstanceValueInstantiator(
            DeserializationConfig config,
            JavaType valueType
        ) {
            super(config, valueType);
        }



        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }



        @Override
        public Object createUsingDefault(DeserializationContext ctxt)
        throws IOException {
            if(cachedInstance==null) {
                try {
                    cachedInstance =
                        this.getValueClass()
                            .getDeclaredConstructor().newInstance();
                } catch (
                    InstantiationException |
                    IllegalAccessException |
                    NoSuchMethodException |
                    SecurityException |
                    IllegalArgumentException |
                    InvocationTargetException e
                ) {
                    log.error(e.getMessage(), e);
                }
            }
            return cachedInstance;
        }

    }



    @Slf4j
    public static class JsonArrayReuseElementIterator
    extends JsonArrayIterator {

        public JsonArrayReuseElementIterator(
            InputStream input,
            ObjectMapper mapper,
            JavaType elementType
        ) throws IOException {
            super(input, mapper, elementType);
            SimpleModule module = new SimpleModule();
            module.addValueInstantiator(
                elementType.getRawClass(),
                new CachingInstanceValueInstantiator(
                    this.mapper.getDeserializationConfig(),
                    elementType
                )
            );
            this.mapper.registerModule(module);
        }

    }
    
}
