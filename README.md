# jackson4streams

You are a java rest developer.
You use spring mvc.
Some of your endpoints are required to accept stupidly huge requests.
Or deliver equally insane responses.
Or both.
You hate ```OutOfMemoryError```.
Now you have serialization for your dto's  ``` java.util.stream.Stream ``` properties.
It is aJackson ```ObjectMapper``` Module that once imported and configured will make you (and your app) to breath a bit.

## Installation

## Configuration

## Examples

##### Using Stream
```java
@Data
public class MyDto {

    protected Stream<Item> items;

}
```
##### Using StreamSupplier

A StreamSupplier class is provided, implementing both ```Supplier<Stream> ``` and ```Closeable```.
Useful when you need to iterate the collection more than once.
Calling close() method (or making use of it in a try-with-resouces block) will release the undelying cache resource (mem, or temp file. See Configuration). As you may guess, once the StreamSupplier is closed, it can no longer supply Streams.

```java
@Data
public class MyDto {

    protected StreamSupplier<Item> items;

}

```
## Contributors

- Bernardo A. Buffa Colomé <kreimerkreimer@gmail.com>
