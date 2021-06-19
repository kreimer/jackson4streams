package kreimer.jackson4streams.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.stream.Stream;
import kreimer.jackson4streams.StreamSerializer;
import kreimer.jackson4streams.StreamSupplier;
import kreimer.jackson4streams.StreamSupplierSerializer;
import kreimer.jackson4streams.TempFileCacheStreamDeserializer;
import kreimer.jackson4streams.TempFileCacheStreamSupplierDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




@Slf4j
@Configuration
public class TempFileConfig
implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    
    
    @Bean("Jackson4StreamsTempFileStreamModule")
    protected SimpleModule getTempFileStreamModule() {
        log.info("jackson4stream config impl: temp-file");
        SimpleModule module = new SimpleModule();
        module.addSerializer(Stream.class, new StreamSerializer());
        module.addSerializer(StreamSupplier.class, new StreamSupplierSerializer());
        module.addDeserializer(
            Stream.class, 
            applicationContext.getBean(
                TempFileCacheStreamDeserializer.class, 
                Boolean.FALSE
            )
        );
        module.addDeserializer(
            StreamSupplier.class, 
            applicationContext.getBean(
                TempFileCacheStreamSupplierDeserializer.class,
                Boolean.FALSE
            )
        );
        return module;
    }
    
    
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
        this.applicationContext = applicationContext;
    }

}
