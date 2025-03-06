package electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;

import electricsam.helidon.grpc.example.server.experimental.eip.core.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class DisruptorRingBufferEndpointTest {

    @Test
    void testProcessStaticRoute() throws Exception {
        final AtomicReference<Throwable> errorRef = new AtomicReference<>();
        final CompletableFuture<Exchange> responseFuture = new CompletableFuture<>();

        final RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
        routeDefinition.process(responseFuture::complete);

        final ErrorHandler errorHandler = (t, exchange) -> errorRef.set(t);

        final DisruptorRingBufferEndpoint endpoint = new DisruptorRingBufferEndpoint(32);
        endpoint.start();
        endpoint.addRouteDefinition(routeDefinition);

        Thread.ofVirtual().start(() -> {
            Exchange exchange = new ExchangeImpl();
            exchange.setBody("Hello World");
            endpoint.process(exchange, errorHandler);
        });

        Exchange response = responseFuture.get(1, TimeUnit.SECONDS);

        assertEquals("Hello World", response.getBody(String.class));
        assertNull(errorRef.get());

    }

    @Test
    void testProcessDynamicRoute() throws Exception {
        final AtomicReference<Throwable> errorRef = new AtomicReference<>();

        final ErrorHandler errorHandler = (t, exchange) -> errorRef.set(t);

        final DisruptorRingBufferEndpoint endpoint = new DisruptorRingBufferEndpoint(32);
        endpoint.start();

        CompletableFuture<Exchange> responseFuture1 = new CompletableFuture<>();
        CompletableFuture<Exchange> responseFuture2 = new CompletableFuture<>();
        CompletableFuture<Exchange> responseFuture3 = new CompletableFuture<>();

        CompletableFuture<RouteDefinitionInternal> routeFuture1 = CompletableFuture.supplyAsync(() -> {
            RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
            routeDefinition.process(responseFuture1::complete);
            endpoint.addRouteDefinition(routeDefinition);
            return routeDefinition;
        }, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture<RouteDefinitionInternal> routeFuture2 = CompletableFuture.supplyAsync(() -> {
            RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
            routeDefinition.process(responseFuture2::complete);
            endpoint.addRouteDefinition(routeDefinition);
            return routeDefinition;
        }, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture<RouteDefinitionInternal> routeFuture3 = CompletableFuture.supplyAsync(() -> {
            RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
            routeDefinition.process(responseFuture3::complete);
            endpoint.addRouteDefinition(routeDefinition);
            return routeDefinition;
        }, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture.allOf(routeFuture1, routeFuture2, routeFuture3).get(1, TimeUnit.SECONDS);

        Thread.ofVirtual().start(() -> {
            Exchange exchange = new ExchangeImpl();
            exchange.setBody("Hello World");
            endpoint.process(exchange, errorHandler);
        });

        Exchange response1 = responseFuture1.get(1, TimeUnit.SECONDS);
        Exchange response2 = responseFuture2.get(1, TimeUnit.SECONDS);
        Exchange response3 = responseFuture3.get(1, TimeUnit.SECONDS);

        assertEquals("Hello World", response1.getBody(String.class));
        assertEquals("Hello World", response2.getBody(String.class));
        assertEquals("Hello World", response3.getBody(String.class));
        assertNull(errorRef.get());

    }

    @Test
    void testProcessDynamicRouteUnregister() throws Exception {
        final AtomicReference<Throwable> errorRef = new AtomicReference<>();

        final ErrorHandler errorHandler = (t, exchange) -> errorRef.set(t);

        final DisruptorRingBufferEndpoint endpoint = new DisruptorRingBufferEndpoint(32);
        endpoint.start();

        CompletableFuture<Exchange> responseFuture1 = new CompletableFuture<>();
        CompletableFuture<Exchange> responseFuture2 = new CompletableFuture<>();
        CompletableFuture<Exchange> responseFuture3 = new CompletableFuture<>();

        CompletableFuture<RouteDefinitionInternal> routeFuture1 = CompletableFuture.supplyAsync(() -> {
            RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
            routeDefinition.process(responseFuture1::complete);
            endpoint.addRouteDefinition(routeDefinition);
            return routeDefinition;
        }, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture<RouteDefinitionInternal> routeFuture2 = CompletableFuture.supplyAsync(() -> {
            RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
            routeDefinition.process(responseFuture2::complete);
            endpoint.addRouteDefinition(routeDefinition);
            return routeDefinition;
        }, Executors.newVirtualThreadPerTaskExecutor());

        CompletableFuture<Void> routeFuture3 = CompletableFuture.supplyAsync(() -> {
                    RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
                    routeDefinition.process(responseFuture3::complete);
                    endpoint.addRouteDefinition(routeDefinition);
                    return routeDefinition;
                }, Executors.newVirtualThreadPerTaskExecutor())
                .thenAcceptAsync(routeDefinition -> {
                    endpoint.removeRouteDefinition(routeDefinition.getRouteId());
                }, Executors.newVirtualThreadPerTaskExecutor());


        CompletableFuture.allOf(routeFuture1, routeFuture2, routeFuture3).get(1, TimeUnit.SECONDS);

        Thread.ofVirtual().start(() -> {
            Exchange exchange = new ExchangeImpl();
            exchange.setBody("Hello World");
            endpoint.process(exchange, errorHandler);
        });

        Exchange response1 = responseFuture1.get(1, TimeUnit.SECONDS);
        Exchange response2 = responseFuture2.get(1, TimeUnit.SECONDS);
        assertThrows(TimeoutException.class, () -> responseFuture3.get(1, TimeUnit.SECONDS));

        assertEquals("Hello World", response1.getBody(String.class));
        assertEquals("Hello World", response2.getBody(String.class));
        assertNull(errorRef.get());

    }
}