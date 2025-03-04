package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.HashMap;
import java.util.Map;

public class ExchangeImpl implements Exchange {

    private final Map<String, Object> properties = new HashMap<>();
    private Object body;

    @Override
    public <T> T getBody(Class<T> type) {
        return type.cast(body);
    }

    @Override
    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        return type.cast(properties.get(key));
    }
}
