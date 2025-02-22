package electricsam.helidon.grpc.example.server.tubes.module.grcp;


import electricsam.helidon.grpc.example.server.tubes.core.TubeExchange;
import electricsam.helidon.grpc.example.server.tubes.core.TubePipe;
import electricsam.helidon.grpc.example.server.tubes.core.TubeSource;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class GrcpSource<TReq, TResp> implements TubeSource, Runnable {

    public static final String GRCP_NAME_KEY = "GRCP_NAME";
    public static final String OBSERVER_KEY = "OBSERVER";
    public static final String COMPLETED_KEY = "COMPLETED";

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicReference<TubeExchange> item = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean();
    protected TubePipe pipe;

    private void notifyExchange(TubeExchange exchange) {
        lock.lock();
        try {
            item.compareAndSet(null, exchange);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    protected StreamObserver<TReq> bidi(String name, StreamObserver<TResp> observer) {
        return new StreamObserver<>() {
            public void onNext(TReq request) {
                TubeExchange exchange = new TubeExchange();
                exchange.setBody(request);
                exchange.setProperty(GRCP_NAME_KEY, name);
                exchange.setProperty(OBSERVER_KEY, observer);
                notifyExchange(exchange);
            }

            public void onError(Throwable t) {
                TubeExchange exchange = new TubeExchange();
                exchange.setError(t);
                exchange.setProperty(GRCP_NAME_KEY, name);
                exchange.setProperty(OBSERVER_KEY, observer);
                notifyExchange(exchange);
            }

            public void onCompleted() {
                TubeExchange exchange = new TubeExchange();
                exchange.setProperty(GRCP_NAME_KEY, name);
                exchange.setProperty(OBSERVER_KEY, observer);
                exchange.setProperty(COMPLETED_KEY, Boolean.TRUE);
                notifyExchange(exchange);
            }
        };
    }

    @Override
    public void acceptPipe(TubePipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void start() {
        running.set(true);
        Thread.ofVirtual().start(this);
    }

    @Override
    public void stop() {
        running.set(false);
        lock.lock();
        try {
            item.set(null);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        while (running.get()) {
            lock.lock();
            try {
                condition.await();
                TubeExchange exchange = item.getAndSet(null);
                if (exchange != null) {
                    pipe.process(exchange);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("GrcpSource stream generation was interrupted", e);
            } finally {
                lock.unlock();
            }
        }
    }
}
