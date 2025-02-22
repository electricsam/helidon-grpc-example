package electricsam.helidon.grpc.example.server.tubes.core;

import java.util.ArrayList;
import java.util.List;

public class TubeConfigurator {


    private final List<TubeSourcePipe> sourcePipes = new ArrayList<>();

    public void configure() {
        sourcePipes.forEach(
                sourcePipe -> sourcePipe.getSource().acceptPipe(new TubePipeImpl(sourcePipe.getProcessors()))
        );
        sourcePipes.forEach(sourcePipe -> sourcePipe.getSource().start());
    }
}
