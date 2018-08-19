package at.alexnavratil.echtzeitdaten;

import at.alexnavratil.echtzeitdaten.model.MonitorResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RealtimeDataCallback {
    protected Function<Map<Integer, List<MonitorResponse>>, Void> successFunction = (param) -> {return null;};
    protected Function<IOException, Void> failureFunction = (param) -> {return null;};

    public RealtimeDataCallback onSuccess(Function<Map<Integer, List<MonitorResponse>>, Void> successFunction) {
        if(successFunction != null) {
            this.successFunction = successFunction;
        } else {
            throw new NullPointerException("successFunction cannot be null");
        }
        return this;
    }

    public RealtimeDataCallback onFailure(Function<IOException, Void> failureFunction) {
        if(failureFunction != null) {
            this.failureFunction = failureFunction;
        } else {
            throw new NullPointerException("failureFunction cannot be null");
        }
        return this;
    }
}
