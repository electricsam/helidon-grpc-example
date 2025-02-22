package electricsam.helidon.grpc.example.server.route;

import electricsam.helidon.grpc.example.server.tubes.core.TubePlumber;
import electricsam.helidon.grpc.example.server.tubes.module.grcp.ProducerServiceImpl2;

public class GrcpPlumbing extends TubePlumber {

    private final ProducerServiceImpl2 producerService;

    public GrcpPlumbing(ProducerServiceImpl2 producerService) {
        this.producerService = producerService;
    }

    @Override
    public void routeTubes() {
        from(producerService).to()
    }
}
