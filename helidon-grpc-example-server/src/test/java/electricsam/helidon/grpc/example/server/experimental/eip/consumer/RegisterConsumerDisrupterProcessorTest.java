package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ExchangeImpl;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilderFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class RegisterConsumerDisrupterProcessorTest {

    @Test
    void testRegisterUnregister() throws Exception {
        final Endpoint ringBuffer = mock(Endpoint.class);
        final Endpoint consumer = mock(Endpoint.class);
        final ErrorHandler errorHandler = mock(ErrorHandler.class);
        final String streamObserverId = UUID.randomUUID().toString();
        final RingBufferRouteBuilder ringBufferRouteBuilder = mock(RingBufferRouteBuilder.class);

        final RingBufferRouteBuilderFactory ringBufferRouteBuilderFactory = mock(RingBufferRouteBuilderFactory.class);
        when(ringBufferRouteBuilderFactory.create(streamObserverId, ringBuffer, consumer, errorHandler)).thenReturn(ringBufferRouteBuilder);

        final RegisterConsumerDisrupterProcessor processor = new RegisterConsumerDisrupterProcessor(ringBuffer, consumer, errorHandler, ringBufferRouteBuilderFactory);

        final String registrationId = UUID.randomUUID().toString();

        final Exchange registerExchange = new ExchangeImpl();
        registerExchange.setBody(ConsumerRegistration.newBuilder().setId(registrationId).setStart(true).build());
        registerExchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, streamObserverId);

        processor.process(registerExchange);

        verify(ringBufferRouteBuilder, times(1)).configure();
        verifyNoMoreInteractions(ringBufferRouteBuilder);
        reset(ringBufferRouteBuilder);

        final Exchange unRegisterExchange = new ExchangeImpl();
        unRegisterExchange.setBody(ConsumerRegistration.newBuilder().setId(registrationId).setStart(false).build());
        unRegisterExchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, streamObserverId);

        processor.process(unRegisterExchange);

        verify(ringBufferRouteBuilder, times(1)).unConfigure();
        verifyNoMoreInteractions(ringBufferRouteBuilder);
    }
}