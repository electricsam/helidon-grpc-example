package electricsam.helidon.grpc.example.cli;

public class ProduceStreamExecutorConfiguration {

    public static Builder builder() {
        return new Builder();
    }

    private final double delay;
    private final String host;
    private final int port;

    private ProduceStreamExecutorConfiguration(double delay, String host, int port) {
        this.delay = delay;
        this.host = host;
        this.port = port;
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

    public static class Builder {
        private double delay = 0.1;
        private String host = "localhost";
        private int port = 1408;

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

        public ProduceStreamExecutorConfiguration build() {
            return new ProduceStreamExecutorConfiguration(delay, host, port);
        }
    }
}
