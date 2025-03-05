package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ProducerTemplate;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;

public class ProducerRouteErrorHandler implements ErrorHandler {

    private final Endpoint producerEcho;
    private final ProducerTemplate producerTemplate;

    public ProducerRouteErrorHandler(Endpoint producerEcho, ProducerTemplate producerTemplate) {
        this.producerEcho = producerEcho;
        this.producerTemplate = producerTemplate;
    }

    private static boolean isStreamClosed(Throwable t) {
        return t instanceof StatusRuntimeException && Status.CANCELLED == ((StatusRuntimeException) t).getStatus();
    }

    @Override
    public void handleError(Throwable t, Exchange exchange) {
        if (!isStreamClosed(t)) {
            // Exchange should already have a reference to the response observer
            exchange.setProperty(COMPLETED, true);
            exchange.setBody(null);
            producerTemplate.sendAsync(exchange, producerEcho, (t1, exchange1) -> {
                // TODO use logging
                System.out.println("An error occurred when sending completion response to producer" + t1.getMessage());
                t1.printStackTrace();
            });
        }
    }
}
