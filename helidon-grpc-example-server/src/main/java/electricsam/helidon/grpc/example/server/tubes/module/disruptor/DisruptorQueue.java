package electricsam.helidon.grpc.example.server.tubes.module.disruptor;


import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.server.consumer.VirtualThreadFactory;
import electricsam.helidon.grpc.example.server.tubes.core.TubeExchange;
import electricsam.helidon.grpc.example.server.tubes.core.TubePipe;
import electricsam.helidon.grpc.example.server.tubes.core.TubeSink;
import electricsam.helidon.grpc.example.server.tubes.core.TubeSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DisruptorQueue<T> implements TubeSource, TubeSink, Runnable {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final RingBuffer<GenericEvent<T>> ringBuffer;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicReference<T> item = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private final BatchEventProcessor<GenericEvent<T>> batchEventProcessor;
    private TubePipe pipe;

    public DisruptorQueue(int bufferSize) {
        //TODO validate bufferSize is a power of 2
        Disruptor<GenericEvent<T>> disruptor = new Disruptor<>(GenericEvent::new, bufferSize, VirtualThreadFactory.INSTANCE);
        ringBuffer = disruptor.start();
        batchEventProcessor = new BatchEventProcessorBuilder()
                .build(ringBuffer, ringBuffer.newBarrier(), (event, l, b) -> onEvent(event));
    }

    private void onEvent(GenericEvent<T> event) {
        lock.lock();
        try {
            if (!item.compareAndSet(null, event.getValue())) {
                throw new IllegalStateException("Value overwritten in disruptor exchange");
            }
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void acceptPipe(TubePipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void start() {
        running.set(true);
        Thread.ofVirtual().start(this);
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
    }

    @Override
    public void stop() {
        batchEventProcessor.halt();
        ringBuffer.removeGatingSequence(batchEventProcessor.getSequence());
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
                T next = item.getAndSet(null);
                if (next != null) {
                    TubeExchange exchange = new TubeExchange();
                    exchange.setBody(next);
                    pipe.process(exchange);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Disruptor stream generation was interrupted", e);
            } finally {
                lock.unlock();
            }
        }
    }

}