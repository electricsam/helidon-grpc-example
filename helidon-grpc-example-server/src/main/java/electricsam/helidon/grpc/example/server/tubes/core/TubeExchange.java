package electricsam.helidon.grpc.example.server.tubes.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TubeExchange {

    private Object body;
    private Throwable error;
    private final Map<String, Object> properties = new HashMap<>();

    public void setError(Throwable error) {
        this.error = error;
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public <T> T getBody(Class<T> clazz) {
        return clazz.cast(body);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public <T> T getProperty(String propertyName, Class<T> clazz) {
        Object value = properties.get(propertyName);
        if (value == null) {
            return null;
        }
        return clazz.cast(properties.get(propertyName));
    }
}
