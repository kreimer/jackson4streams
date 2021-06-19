package kreimer.jackson4streams.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kreimer.jackson4streams.InMemoryStreamDeserializer;
import kreimer.jackson4streams.InMemoryStreamSupplierDeserializer;
import org.springframework.context.annotation.Import;



@Import({
    InMemoryConfig.class,
    InMemoryStreamDeserializer.class,
    InMemoryStreamSupplierDeserializer.class
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableJackson4StreamsInMemory {
    
}
