package electricsam.helidon.grpc.example.cli;

import java.util.Objects;

public class ConsumeStreamExecutorConfiguration {

    public static Builder builder() {
        return new Builder();
    }

    private final String host;
    private final int port;
    private final ServiceName serviceName;

    private ConsumeStreamExecutorConfiguration(String host, int port, ServiceName serviceName) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 1408;
        private ServiceName serviceName;

        private Builder() {

        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setServiceName(ServiceName serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ConsumeStreamExecutorConfiguration build() {
            return new ConsumeStreamExecutorConfiguration(
                    host,
                    port,
                    Objects.requireNonNull(serviceName, "serviceName is required")
            );
        }
    }
}
