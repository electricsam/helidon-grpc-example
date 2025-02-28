package electricsam.helidon.grpc.example.cli;

public class ConsumeStreamExecutorConfiguration {

    public static Builder builder() {
        return new Builder();
    }

    private final String host;
    private final int port;

    private ConsumeStreamExecutorConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 1408;

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

        public ConsumeStreamExecutorConfiguration build() {
            return new ConsumeStreamExecutorConfiguration(host, port);
        }
    }
}
