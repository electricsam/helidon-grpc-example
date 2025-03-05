package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;

import static electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerSetReplyProcessor.PRODUCER_REQUEST;

public class PrepareConsumerResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        exchange.setBody(exchange.getProperty(PRODUCER_REQUEST, ProducerRequest.class));
        exchange.clearProperties();
    }

}
