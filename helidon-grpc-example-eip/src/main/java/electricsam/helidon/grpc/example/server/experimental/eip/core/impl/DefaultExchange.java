package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultExchange implements Exchange {

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

    @Override
    public void clearProperties() {
        properties.clear();
    }

    @Override
    public Set<String> getPropertyKeys() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public Exchange shallowCopy() {
        Exchange copy = new DefaultExchange();
        copy.setBody(body);
        properties.forEach(copy::setProperty);
        return copy;
    }
}
