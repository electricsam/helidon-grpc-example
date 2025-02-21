package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConsumerServiceImpl implements ConsumerService, Runnable {

    private final BlockingQueue<ProducerRequest> producerQueue = new ArrayBlockingQueue<>(100);
    private final Set<StreamObserver<ConsumerResponse>> observers = new HashSet<>();
    private final Thread consumer;


    public ConsumerServiceImpl() {
        // TODO virtual thread
        consumer = new Thread(this);
        consumer.start();
    }

    @Override
    public String name() {
        return "ConsumerService";
    }

    @Override
    public void update(Rules rules) {
        rules.proto(ExampleGrpc.getDescriptor()).bidirectional("RegisterConsumer", this::register);
    }

    private StreamObserver<ConsumerRegistration> register(StreamObserver<ConsumerResponse> observer) {

        return new StreamObserver<>() {
            public void onNext(ConsumerRegistration registration) {
                if (registration.getStart()) {
                    synchronized (observers) {
                        if (observers.add(observer)) {
                            System.out.println("Registering consumer: " + registration.getId());
                        }
                    }
                } else {
                    synchronized (observers) {
                        if (observers.remove(observer)) {
                            System.out.println("Unregistering consumer: " + registration.getId());
                        }
                    }
                }
            }

            public void onError(Throwable t) {
                t.printStackTrace();
                synchronized (observers) {
                    if (observers.remove(observer)) {
                        System.out.println("Unregistering consumer");
                    }
                }
                observer.onError(t);
            }

            public void onCompleted() {
                synchronized (observers) {
                    if (observers.remove(observer)) {
                        System.out.println("Unregistering consumer");
                    }
                }
                observer.onCompleted();
            }
        };
    }

    @Override
    public void sendToConsumers(ProducerRequest request) {
        try {
            producerQueue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Send to consumer thread was interrupted", e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                final ProducerRequest request = producerQueue.take();
                final ConsumerResponse response = ConsumerResponse.newBuilder().setMessage(request.getMessage()).build();
                synchronized (observers) {
                    for (StreamObserver<ConsumerResponse> observer : observers) {
                        observer.onNext(response);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consumer response processing thread was interrupted", e);
        }
    }

}
