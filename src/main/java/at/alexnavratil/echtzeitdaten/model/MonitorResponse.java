package at.alexnavratil.echtzeitdaten.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MonitorResponse {
    private final String lineName;
    private final String towards;
    private final List<Integer> departures;

    public MonitorResponse(String lineName, String towards, List<Integer> departures) {
        this.lineName = lineName;
        this.towards = towards;
        this.departures = departures;
    }

    public String getLineName() {
        return lineName;
    }

    public String getTowards() {
        return towards;
    }

    public List<Integer> getDepartures() {
        return departures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitorResponse that = (MonitorResponse) o;
        return Objects.equals(lineName, that.lineName) &&
                Objects.equals(towards, that.towards) &&
                Objects.equals(departures, that.departures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineName, towards, departures);
    }

    @Override
    public String toString() {
        return "MonitorResponse{" +
                "lineName='" + lineName + '\'' +
                ", towards='" + towards + '\'' +
                ", departures=" + departures +
                '}';
    }
}
