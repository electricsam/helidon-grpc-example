package electricsam.helidon.grpc.example.cli;


import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;

abstract class ProduceStreamBaseCommand implements Runnable {


    protected abstract ProduceStreamExecutorConfiguration configure();

    @Override
    public void run() {

        ProduceStreamExecutorConfiguration configuration = configure();

        //TODO figure out the Netty options to disable direct buffers to debug memory
        // The below options do not seem to work
        /*
        Server error. Stopping. INTERNAL: Connection closed after GOAWAY. HTTP/2 error code: INTERNAL_ERROR, debug data: Cannot reserve 2097152 bytes of direct buffer memory (allocated: 4292870424, limit: 4294967296)
         */
        if (configuration.isNoDirectBuffers()) {
            // Disable direct buffers in order to better analyze memory usage. May reduce performance.
            System.setProperty("io.netty.noUnsafe", "true");
            System.setProperty("io.netty.noPreferDirect", "true");
            System.setProperty("io.netty.maxDirectMemory", "0");
            System.setProperty("io.netty.allocator.numDirectArenas", "0");
            throw new UnsupportedOperationException("No direct buffers not yet supported");
        }

        ProduceStreamExecutor executor = new ProduceStreamExecutor(configuration);
        executor.addVisitor(new ProduceStreamExecutorVisitor() {
            @Override
            public void beforeSendRequest(ProducerRequest request) {
                System.out.println("Sent " + request.getMessage());
            }

            @Override
            public void afterSendRequest(ProducerRequest request) {

            }

            @Override
            public void onReceiveResponse(ProducerResponse response) {
                System.out.println("Received " + response.getMessage());
            }

            @Override
            public void onResponseError(Throwable throwable) {
                System.out.println("Server error. Stopping. " + throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onResponseCompleted() {
                System.out.println("Response stream completed");
            }
        });

        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(executor::stop));

        executor.run();

    }


}
