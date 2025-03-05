package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

import java.util.function.Predicate;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;

public class NotCompletedFilter implements Predicate<Exchange> {
    @Override
    public boolean test(Exchange exchange) {
        return !exchange.getProperty(COMPLETED, Boolean.class);
    }
}
