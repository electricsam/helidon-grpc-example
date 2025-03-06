module electricsam.helidon.grpc.example.proto {
    requires com.google.protobuf;
    requires io.grpc.protobuf;
    requires io.grpc;
    requires io.grpc.stub;
    requires com.google.common;
    requires static annotations.api;
    exports electricsam.helidon.grpc.example.proto;
}