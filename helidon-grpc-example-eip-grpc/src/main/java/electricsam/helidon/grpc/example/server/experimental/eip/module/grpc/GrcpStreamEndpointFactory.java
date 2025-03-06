package electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;

import com.google.protobuf.Descriptors.FileDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class GrcpStreamEndpointFactory {

    private final Map<String, GrpcStreamEndpoint> endpoints = new HashMap<>();
    private final GrcpStreamService service;

    public GrcpStreamEndpointFactory(String serviceName, FileDescriptor fileDescriptor, Collection<String> methodNames) {
        for (String methodName : methodNames) {
            endpoints.put(methodName, new GrpcStreamEndpoint(methodName, fileDescriptor));
        }
        service = new GrcpStreamService(serviceName, endpoints.values());
    }

    public GrpcStreamEndpoint getEndpoint(String methodName) {
        return endpoints.get(methodName);
    }

    public GrcpStreamService getService() {
        return service;
    }

}
