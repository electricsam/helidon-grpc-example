package electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

public class DisruptorRingBufferEvent {
    private Exchange exchange;

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }
}
