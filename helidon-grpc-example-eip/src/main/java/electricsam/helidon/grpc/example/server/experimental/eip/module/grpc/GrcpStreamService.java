package electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;

import io.helidon.grpc.server.GrpcService;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

import java.util.Collection;

public class GrcpStreamService implements GrpcService {

    private final String serviceName;
    private final Collection<GrpcStreamEndpoint> endpoints;

    GrcpStreamService(String serviceName, Collection<GrpcStreamEndpoint> endpoints) {
        this.serviceName = serviceName;
        this.endpoints = endpoints;
    }

    @Override
    public void update(Rules rules) {
        endpoints.forEach(endpoint -> endpoint.update(rules));
    }

    @Override
    public String name() {
        return serviceName;
    }
}
