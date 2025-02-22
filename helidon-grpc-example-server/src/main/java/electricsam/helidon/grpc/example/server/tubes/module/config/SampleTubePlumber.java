package electricsam.helidon.grpc.example.server.tubes.module.config;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.server.tubes.core.TubePlumber;
import electricsam.helidon.grpc.example.server.tubes.module.disruptor.DisruptorQueue;
import electricsam.helidon.grpc.example.server.tubes.module.grcp.ConsumerService2;
import electricsam.helidon.grpc.example.server.tubes.module.grcp.ProducerServiceImpl2;

public class SampleTubePlumber extends TubePlumber {

    private final ProducerServiceImpl2 producerService;
    private final DisruptorQueue<ProducerRequest> producerQueue;
    private final ConsumerService2 consumerService2;

    public SampleTubePlumber(ProducerServiceImpl2 producerService, DisruptorQueue<ProducerRequest> producerQueue, ConsumerService2 consumerService2) {
        this.producerService = producerService;
        this.producerQueue = producerQueue;
        this.consumerService2 = consumerService2;
    }

    @Override
    public void routeTubes() {
        from(producerService).to(producerQueue);
        from(producerQueue).to(null);

        from(consumerService2).to(null);
    }
}
