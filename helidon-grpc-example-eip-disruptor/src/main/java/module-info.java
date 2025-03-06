module electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor {
    requires transitive electricsam.helidon.grpc.example.server.experimental.eip.core;
    requires transitive com.lmax.disruptor;
    exports electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;
}