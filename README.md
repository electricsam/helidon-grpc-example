# Helidon gRPC Example Project

This project is a playground to experiment and test the use of various technologies 
 for gRPC streaming.  Since this project is experimental in nature, it should be considered 
a proof of concept and not all aspects of a production-ready application have been implemented.

### At this point in time this project achieves the following prototype goals

 - Defines a protobuf schema and generates the required Java classes
 - Contains a Helidon gRPC service that accepts one input stream of messages
 - The Helidon gRPC service also accepts multiple stream consumers
 - The Helidon gRPC service efficiently broadcasts messages from the producer to the subscribers using either a ring buffer or a deque
 - Uses Dagger for dependency injection
 - Uses Java 21 Virtual Threads for all threads created during message processing
 - Contains a command line interface that acts as a producer, sending one message or a continuous stream of messages to the service
 - Contains a command line interface that acts as a consumer, subscribing to the service and receiving the broadcast stream. Multiple consumers are supported.

### Known issues at this time:
 - Need to improve the handling of threads on producer, service, and consumer on shutdown
 - Need to improve error handling
 - Need to understand memory management in the CLI.  There may be a memory leak in the producer.
 - Need to add unit tests
 - Need to set up performance tests to evaluate the effect changes to the project have on throughput and memory

## Building the Project

Maven and a Java 21 JDK are required. Follow the respective instructions for your OS to
install these.

JDK 21 (one example): https://www.oracle.com/java/technologies/downloads/ 
Maven: https://maven.apache.org/

If you are on a Mac, here are some tips for installing some resources:
 - Use SDKMan to install both the JDK and Maven. https://sdkman.io/
 - - Install SDKMan with 'curl -s "https://get.sdkman.io" | bash'
 - - Install Java 21 through SDK Man with 'sdk install java 21.0.6-graal' (or whatever flavor of Java 21 you want)
 - - Install Maven through SDK Man with 'sdk install maven 3.9.0'
 - You might also need to install Rosetta 2 to generate the protobuf Java classes:
 - - softwareupdate --install-rosetta

To build the project, from the root of the project run:
 - mvn clean install

This will build all three modules: proto, service, cli.

## Running an Example

To start the service, from the root of the project, run:
 - java -jar helidon-grpc-example-server/target/helidon-grpc-example-server-1.0.0-SNAPSHOT.jar


To start a streaming producer, from the root of the project, run:
 - java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT.jar produce stream


To start a simple consumer, from the root of the project, run:
 - java -jar helidon-grpc-example-cli/target/helidon-grpc-example-cli-1.0.0-SNAPSHOT.jar consume print

Please note that this is an experimental project and not production-ready.  See caveat and known issues above.



