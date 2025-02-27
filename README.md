# Helidon gRPC Example Project

This project is a playground to experiment and test the use of various technologies 
 for gRPC streaming.  Since this project is experimental in nature, it should be considered 
a proof of concept and not all aspects of a production-ready application have been implemented.

### At this point in time this project achieves the following prototype goals

- Defines a protobuf schema and generates the required Java code
- Contains a Helidon gRPC service that accepts one input stream of messages
- Contains a Helidon gRPC service that accepts multiple stream consumers
- The gRPC service efficiently broadcasts messages from the producer to the subscribers using either a ring buffer or a deque
- Uses Dagger for dependency injection
- Uses Java 21 Virtual Threads for all threads created during message processing
- Contains a command line interface that acts as a producer, sending one message or a continuous stream of messages to the service
- Contains a command line interface that acts as a consumer, subscribing to the service and receiving the broadcast stream. Multiple consumers are supported.

### Known issues at this time:
- Need to improve the handling of threads on producer, service, and consumer shutdown
- Need to improve error handling
- Need to understand memory management in the CLI.  There may be a memory leak in the producer.


