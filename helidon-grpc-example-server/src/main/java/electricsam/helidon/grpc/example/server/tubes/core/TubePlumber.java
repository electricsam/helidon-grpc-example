package electricsam.helidon.grpc.example.server.tubes.core;

import java.util.ArrayList;
import java.util.List;

public abstract class TubePlumber {

    private final List<TubeSource> sources = new ArrayList<>();



    protected Tube from(TubeSource source) {
        return null;
    }

    public abstract void routeTubes();
}
