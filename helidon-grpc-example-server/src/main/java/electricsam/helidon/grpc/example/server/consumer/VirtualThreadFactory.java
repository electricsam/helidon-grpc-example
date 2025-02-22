package electricsam.helidon.grpc.example.server.consumer;

import java.util.concurrent.ThreadFactory;

public final class VirtualThreadFactory implements ThreadFactory {

    public static final VirtualThreadFactory INSTANCE = new VirtualThreadFactory();

    private VirtualThreadFactory() {

    }

    @Override
    public Thread newThread(Runnable r) {
        return Thread.ofVirtual().unstarted(r);
    }
}
