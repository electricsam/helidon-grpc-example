package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;

abstract class ConsumePrintBaseCommand implements Runnable {

    protected abstract ConsumeStreamExecutorConfiguration configure();

    @Override
    public void run() {

        ConsumeStreamExecutorConfiguration configuration = configure();
        ConsumeStreamExecutor executor = new ConsumeStreamExecutor(configuration);
        executor.addVisitor(new ConsumeStreamExecutorVisitor() {
            @Override
            public void onReceiveResponse(ConsumerResponse response) {
                System.out.println("Received " + response.getMessage());
            }

            @Override
            public void onResponseError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponseCompleted() {

            }
        });

        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(executor::stop));

        executor.run();

    }

}
