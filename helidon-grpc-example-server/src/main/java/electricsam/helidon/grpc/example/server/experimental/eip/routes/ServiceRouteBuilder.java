package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;

public class ServiceRouteBuilder extends RouteBuilder {

    private final Endpoint producerEcho;
    private final Processor logRequest;
    private final Processor setReply;

    public ServiceRouteBuilder(Endpoint producerEcho, Processor logRequest, Processor setReply) {
        this.producerEcho = producerEcho;
        this.logRequest = logRequest;
        this.setReply = setReply;
    }

    @Override
    protected void configure() {
        from(producerEcho)
                .process(logRequest)
                .process(setReply)
                .to(producerEcho);
    }
}
