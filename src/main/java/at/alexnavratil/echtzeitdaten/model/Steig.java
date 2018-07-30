package at.alexnavratil.echtzeitdaten.model;

import java.util.Objects;

public class Steig {
    private final int id;
    private final int haltestellenId;
    private final Linie linie;
    private final int rbl;

    public Steig(int id, int haltestellenId, Linie linie, int rbl) {
        this.id = id;
        this.haltestellenId = haltestellenId;
        this.linie = linie;
        this.rbl = rbl;
    }

    public int getId() {
        return id;
    }

    public int getHaltestellenId() {
        return haltestellenId;
    }

    public Linie getLinie() {
        return linie;
    }

    public int getRbl() {
        return rbl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Steig steig = (Steig) o;
        return id == steig.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Steig{" +
                "id=" + id +
                ", haltestellenId=" + haltestellenId +
                ", linie=" + linie +
                ", rbl=" + rbl +
                '}';
    }
}
