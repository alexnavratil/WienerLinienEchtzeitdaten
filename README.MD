[![](https://jitpack.io/v/alexnavratil/WienerLinienEchtzeitdaten.svg)](https://jitpack.io/#alexnavratil/WienerLinienEchtzeitdaten)

# Wiener Linien Echtzeitdaten Java API (Work In Progress)
Datenquelle: Stadt Wien – data.wien.gv.at

## Gradle
```
allprojects {
    repositories {
	    ...
	    maven { url 'https://jitpack.io' }
	}
}

dependencies {
    implementation 'com.github.alexnavratil:WienerLinienEchtzeitdaten:v0.1'
}
```

## Example
The following code lists the departure time in minutes from the station Palffygasse:
```
public static void main(String[] args) throws IOException {
       EchtzeitdatenApi api = new EchtzeitdatenApi("SENDER-ID");
       api.readStaticData();
       List<Haltestelle> haltestellenList = api.listHaltestellen();

       final String stationName = "Palffygasse";
       List<Integer> rblList = new ArrayList<>();
       Optional<Haltestelle> station = haltestellenList.stream().filter(haltestelle -> haltestelle.getName().equals(stationName)).findFirst();
       if(station.isPresent()){
           if(station.get().getSteigList() != null) {
               station.get().getSteigList().forEach(steig -> rblList.add(steig.getRbl()));
           }

           Map<Integer, List<MonitorResponse>> response = api.listAbfahrtszeiten(rblList);
           response.entrySet().forEach(entry -> {
               System.out.println("RBL: " + entry.getKey());
               entry.getValue().forEach(monitor -> {
                   System.out.println("    Line: " + monitor.getLineName());
                   System.out.println("    Towards: " + monitor.getTowards());
                   System.out.println("    Depatures: " + monitor.getDepartures()
                           .stream()
                           .map(Object::toString)
                           .reduce((o, o2) -> o+","+o2).get());
                   System.out.println();
               });
               System.out.println("==========================================================");
           });
       } else {
           System.err.println("Station not found!");
       }
    }
```