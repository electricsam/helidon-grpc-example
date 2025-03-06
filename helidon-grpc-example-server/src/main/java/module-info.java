module electricsam.helidon.grpc.example.server {
    requires electricsam.helidon.grpc.example.proto;
    requires dagger;
    requires javax.inject;
    requires electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;
    requires electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;
}