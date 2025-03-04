package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ConsumerServiceGrpc;
import electricsam.helidon.grpc.example.proto.EipProducerServiceGrpc;
import electricsam.helidon.grpc.example.proto.ProducerServiceGrpc;
import io.grpc.ServiceDescriptor;

public enum ServiceName {
    ProducerService(ProducerServiceGrpc.getServiceDescriptor()),
    EipProducerService(EipProducerServiceGrpc.getServiceDescriptor()),
    ConsumerService(ConsumerServiceGrpc.getServiceDescriptor());

    private final ServiceDescriptor serviceDescriptor;

    ServiceName(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }
}
