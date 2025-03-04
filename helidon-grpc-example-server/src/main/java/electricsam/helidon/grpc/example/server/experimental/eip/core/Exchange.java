package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface Exchange {

    <T> T getBody(Class<T> type);

    void setBody(Object body);

    void setProperty(String key, Object value);

    <T> T getProperty(String key, Class<T> type);

}
