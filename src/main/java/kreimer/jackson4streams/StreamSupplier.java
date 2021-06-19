package kreimer.jackson4streams;

import java.io.Closeable;
import java.util.function.Supplier;
import java.util.stream.Stream;



public interface StreamSupplier <T>
extends Supplier<Stream<T>>, Closeable {
    
}