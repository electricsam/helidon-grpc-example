package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.function.Predicate;

public class Filter implements Processable {

    private final Predicate<Exchange> predicate;

    public Filter(Predicate<Exchange> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        return predicate.test(exchange);
    }
}
