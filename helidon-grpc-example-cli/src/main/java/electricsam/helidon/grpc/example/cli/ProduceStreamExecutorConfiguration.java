package electricsam.helidon.grpc.example.cli;

import java.util.Objects;

public class ProduceStreamExecutorConfiguration {

    public static Builder builder() {
        return new Builder();
    }

    private final double delay;
    private final String host;
    private final int port;
    private final ServiceName serviceName;
    private final ServiceMethodName methodName;
    private final boolean noDirectBuffers;

    private ProduceStreamExecutorConfiguration(double delay, String host, int port, ServiceName serviceName, ServiceMethodName methodName, boolean noDirectBuffers) {
        this.delay = delay;
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.noDirectBuffers = noDirectBuffers;
    }

    public double getDelay() {
        return delay;
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

    public ServiceMethodName getMethodName() {
        return methodName;
    }

    public boolean isNoDirectBuffers() {
        return noDirectBuffers;
    }

    public static class Builder {
        private double delay = 1.0;
        private String host = "localhost";
        private int port = 1408;
        private ServiceName serviceName;
        private ServiceMethodName methodName;
        private boolean noDirectBuffers = false;

        private Builder() {

        }

        public Builder setDelay(double delay) {
            this.delay = delay;
            return this;
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

        public Builder setMethodName(ServiceMethodName methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder setNoDirectBuffers(boolean noDirectBuffers) {
            this.noDirectBuffers = noDirectBuffers;
            return this;
        }

        public ProduceStreamExecutorConfiguration build() {
            return new ProduceStreamExecutorConfiguration(
                    delay,
                    host,
                    port,
                    Objects.requireNonNull(serviceName, "serviceName is required"),
                    Objects.requireNonNull(methodName, "methodName is required"),
                    noDirectBuffers
            );
        }
    }
}
