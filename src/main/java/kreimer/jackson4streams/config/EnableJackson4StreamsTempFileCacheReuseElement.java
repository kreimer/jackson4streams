package kreimer.jackson4streams.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kreimer.jackson4streams.TempFileCacheStreamDeserializer;
import kreimer.jackson4streams.TempFileCacheStreamSupplierDeserializer;
import org.springframework.context.annotation.Import;



@Import({
    TempFileReuseElementConfig.class,
    TempFileCacheStreamDeserializer.class,
    TempFileCacheStreamSupplierDeserializer.class,
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableJackson4StreamsTempFileCacheReuseElement {
    
}
