# Helidon gRPC Example Project

This project is a playground to experiment and test the use of various technologies 
 for gRPC streaming.  Since this project is experimental in nature, it should be considered 
a proof of concept and not all aspects of a production-ready application have been implemented.

### At this point in time, this project achieves the following prototype goals

 - Defines a protobuf schema and generates the required Java classes
 - Contains a Helidon gRPC service that accepts one input stream of messages
 - The Helidon gRPC service also accepts multiple stream consumers
 - The Helidon gRPC service efficiently broadcasts messages from the producer to the subscribers using either a ring buffer or a deque
 - Uses Dagger for dependency injection
 - Uses Java 21 Virtual Threads for all threads created during message processing
 - Contains a command line interface that acts as a producer, sending one message or a continuous stream of messages to the service
 - Contains a command line interface that acts as a consumer, subscribing to the service and receiving the broadcast stream. Multiple consumers are supported.

### Known issues at this time:
 - ~~Need to improve the handling of threads on producer, service, and consumer on shutdown~~
 - - When the service shuts down, consumer and producer streams are completed
 - Need to improve error handling
 - - Partially completed - Errors in service terminate streams. More comprehensive testing needed.
 - ~~Need to understand memory management in the CLI.  There may be a memory leak in the producer.~~
 - - Messages were being sent in a rapid loop, overwhelming the netty buffer. Added a configurable delay with a reasonable default.
 - - Still need to better understand what the threshold is and if this can be improved.
 - - Disabling Netty direct buffers seems to be difficult and tried properties does not appear to have the desired effect.
 - Need to add unit tests
 - Need to set up performance tests to evaluate the effect changes to the project have on throughput and memory

## Building the Project

Maven and a Java 21 JDK are required. Follow the respective instructions for your OS to
install these.

 - JDK 21 (one example): https://www.oracle.com/java/technologies/downloads/ 
 - Maven: https://maven.apache.org/

### Mac Tips
If you are on a Mac, here are some tips for installing some resources:
Use SDKMan to install both the JDK and Maven. https://sdkman.io/

Install SDKMan with:
```bash
curl -s "https://get.sdkman.io" | bash
```
Install Java 21 through SDKMan with:
```bash
sdk install java 21.0.6-graal
```
(or whatever flavor of Java 21 you want)

Install Maven through SDK Man with:
```bash
sdk install maven 3.9.0
```
You might also need to install Rosetta 2 to generate the protobuf Java classes:
```bash
softwareupdate --install-rosetta
```

### Build the project
To build the project, from the root of the project, run:
```bash
mvn clean install
```

This will build all three modules: proto, service, cli.

## Running The Example

To start the service, from the root of the project, run:
```bash
java -jar helidon-grpc-example-server/target/helidon-grpc-example-server-1.0.0-SNAPSHOT-exe.jar
```


To start a streaming producer, from the root of the project, run:
```bash
java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT-exe.jar produce stream
````

To start a simple consumer, from the root of the project, run:
```bash
java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT-exe.jar consume print
````

Multiple consumers can be started to demonstrate the service's multicast capabilities.

Please note that this is an experimental project and not production-ready.  See caveat and known issues above.

## Upcoming Experimental Features
### Apache Camel-like Enterprise Integration Pattern (EIP) Route Definition

The electricsam.helidon.grpc.example.server.experimental.eip package contains code
that will eventually allow for EIP style definitions for data flow.

Here is a partial example:

```java
protected void configure() {
        from(producerEndpoint)
                .errorHandler(producerRouteErrorHandler)
                .process(producerReplyProcessor)
                .to(disruptorRingBufferEndpoint);
        
        from(consumerRegistrationEndpoint)
                .errorHandler(consumerRegistrationErrorHandler)
                .process(consumerSubscriptionProcessor);
        
        from(disruptorRingBufferEndpoint)
                .errorHandler(consumerStreamErrorHandler)
                .process(consumerStreamingProcessor);
}
```

#### Update 2025/03/04

Experimental EIP routing has been added that just echoes back responses to the 
producer

```java
    protected void configure() {
        from(producerEcho)
                .process(logRequest)
                .process(setReply)
                .to(producerEcho);
    }
```

This can be invoked from the CLI with:

```bash
java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT-exe.jar produce experimental-eip --echo
```


#### Update 2025/03/05

A complete experimental route has been added. Testing and bug fixes still need to be completed.  There are known issues, but the 
basic case of a single producer and consumer does allow messages to flow from producer to consumer.

```java
    public void configure() {
        from(producerEcho)
                .errorHandler(producerErrorHandler)
                .process(logRequest)
                .process(setProducerReply)
                .to(producerEcho);

        from(producer)
                .errorHandler(producerErrorHandler)
                .process(logRequest)
                .process(setProducerReply)
                .to(producer)
                .filter(notCompleted)
                .process(prepareConsumerResponse)
                .to(ringBuffer);

        from(consumer)
                .errorHandler(consumerErrorHandler)
                .process(registerConsumerDisruptor);
    }
```

```java
    public void configure() {
        from(ringBuffer)
                .errorHandler(errorHandler)
                .process(exchange -> {
                    exchange.setProperty(RESPONSE_STREAM_OBSERVER, responseStream);
                    exchange.setProperty(COMPLETED, false);
                })
                .to(consumer);
    }
```

The corresponding CLI commands are:

```bash
java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT-exe.jar produce experimental-eip
```

```bash
java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT-exe.jar consume experimental-eip
```



