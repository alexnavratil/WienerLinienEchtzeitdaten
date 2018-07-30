package at.alexnavratil.echtzeitdaten.model;

import java.util.List;
import java.util.Objects;

public class Haltestelle {
    private final int id;
    private final String name;
    private final List<Steig> steigList;

    public Haltestelle(int id, String name, List<Steig> steigList) {
        this.id = id;
        this.name = name;
        this.steigList = steigList;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Steig> getSteigList() {
        return steigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Haltestelle that = (Haltestelle) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Haltestelle{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", steigList=" + steigList +
                '}';
    }
}
