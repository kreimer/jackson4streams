package kreimer.jackson4streams.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.stream.Stream;
import kreimer.jackson4streams.InMemoryStreamDeserializer;
import kreimer.jackson4streams.InMemoryStreamSupplierDeserializer;
import kreimer.jackson4streams.StreamSerializer;
import kreimer.jackson4streams.StreamSupplier;
import kreimer.jackson4streams.StreamSupplierSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Slf4j
@Configuration
public class InMemoryConfig
implements ApplicationContextAware {
    
    protected ApplicationContext applicationContext;

    
    
    @Bean("Jackson4StreamsInMemoryStreamModule")
    protected  SimpleModule getInMemoryStreamModule() {
        log.info("jackson4stream config impl: in-memory");
        SimpleModule module = new SimpleModule();
        module.addSerializer(Stream.class, new StreamSerializer());
        module.addSerializer(StreamSupplier.class, new StreamSupplierSerializer());
        module.addDeserializer(
            Stream.class, 
            applicationContext.getBean(InMemoryStreamDeserializer.class)
        );
        module.addDeserializer(
            StreamSupplier.class, 
            applicationContext.getBean(InMemoryStreamSupplierDeserializer.class)
        );
        return module;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
        this.applicationContext = applicationContext;
    }
    
}