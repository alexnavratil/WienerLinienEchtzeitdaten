package at.alexnavratil.echtzeitdaten;

import at.alexnavratil.echtzeitdaten.model.*;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class EchtzeitdatenApi {
    private boolean readStaticData = false;
    private List<Haltestelle> haltestellenList = new ArrayList<>();
    private List<Linie> linienList = new ArrayList<>();
    private List<Steig> steigList = new ArrayList<>();

    private Map<Integer, Linie> linieIndexMap = new HashMap<>();
    private Map<Integer, List<Steig>> steigHaltestellenIndexMap = new HashMap<>();

    private String senderId = null;

    public EchtzeitdatenApi() {
    }

    public EchtzeitdatenApi(String senderId) {
        this.senderId = senderId;
    }

    public void readStaticData() throws IOException {
        InputStreamReader linien = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("wienerlinien-ogd-linien.csv"));
        InputStreamReader haltestellen = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("wienerlinien-ogd-haltestellen.csv"));
        InputStreamReader steige = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("wienerlinien-ogd-steige.csv"));

        CsvReader csvReader = new CsvReader();
        csvReader.setFieldSeparator(';');
        csvReader.setContainsHeader(true);

        /**
         * LINIEN
         */
        CsvContainer csvLinien = csvReader.read(linien);
        for (CsvRow row : csvLinien.getRows()) {
            final Linie l = new Linie(
                    Integer.parseInt(row.getField("LINIEN_ID")),
                    row.getField("BEZEICHNUNG"),
                    parseType(row.getField("VERKEHRSMITTEL"))
            );

            linienList.add(l);
            linieIndexMap.put(l.getId(), l);
        }

        /**
         * STEIGE
         */
        CsvContainer csvSteige = csvReader.read(steige);
        for (CsvRow row : csvSteige.getRows()) {
            if(!row.getField("RBL_NUMMER").isEmpty()) {
                final Steig s = new Steig(
                        Integer.parseInt(row.getField("STEIG_ID")),
                        Integer.parseInt(row.getField("FK_HALTESTELLEN_ID")),
                        linieIndexMap.get(Integer.parseInt(row.getField("FK_LINIEN_ID"))),
                        Integer.parseInt(row.getField("RBL_NUMMER")),
                        Integer.parseInt(row.getField("REIHENFOLGE")),
                        parseRichtung(row.getField("RICHTUNG")));
                this.steigList.add(s);
                if (steigHaltestellenIndexMap.containsKey(s.getHaltestellenId())) {
                    steigHaltestellenIndexMap.get(s.getHaltestellenId()).add(s);
                } else {
                    final List<Steig> steigList = new ArrayList<>();
                    steigList.add(s);
                    steigHaltestellenIndexMap.put(s.getHaltestellenId(), steigList);
                }
            }
        }

        // prepare for GC
        linieIndexMap = null;

        /**
         * HALTESTELLEN
         */
        CsvContainer csvHaltestellen = csvReader.read(haltestellen);
        for (CsvRow row : csvHaltestellen.getRows()) {
            int haltestellenId = Integer.parseInt(row.getField("HALTESTELLEN_ID"));
            final Haltestelle h = new Haltestelle(
                    haltestellenId,
                    row.getField("NAME"),
                    steigHaltestellenIndexMap.get(haltestellenId)
            );
            haltestellenList.add(h);
        }

        // prepare for GC
        steigHaltestellenIndexMap = null;

        readStaticData = true;
    }

    private TransferType parseType(String type){
        switch(type) {
            case "ptTram":
                return TransferType.TRAM;
            case "ptBusNight":
                return TransferType.BUS_NIGHT;
            case "ptTrainS":
                return TransferType.TRAIN_S;
            case "ptMetro":
                return TransferType.METRO;
            case "ptTramVRT":
                return TransferType.TRAM_VRT;
            case "ptTramWLB":
                return TransferType.TRAM_WLB;
            case "ptBusCity":
                return TransferType.BUS_CITY;
            default:
                return null;
        }
    }

    private Richtung parseRichtung(String richtung) {
        return richtung.equals("H") ? Richtung.H : Richtung.R;
    }

    public List<Haltestelle> listHaltestellen(){
        if(!readStaticData) {
            throw new IllegalStateException("Please call readStaticData before calling any static data method");
        }
        return haltestellenList;
    }

    public List<Steig> listSteige(){
        if(!readStaticData) {
            throw new IllegalStateException("Please call readStaticData before calling any static data method");
        }
        return steigList;
    }

    public List<Linie> listLinien(){
        if(!readStaticData) {
            throw new IllegalStateException("Please call readStaticData before calling any static data method");
        }
        return linienList;
    }

    public Map<Integer, List<MonitorResponse>> listAbfahrtszeiten(List<Integer> rblList) throws IOException {
        if(senderId == null){
            throw new IllegalStateException("no senderId set");
        }
        final OkHttpClient client = new OkHttpClient();

        StringBuilder rblQueryParam = new StringBuilder();
        rblList.forEach(num -> rblQueryParam.append("&rbl="+num));

        Request request = new Request.Builder()
                .url("http://www.wienerlinien.at/ogd_realtime/monitor?sender="+this.senderId+rblQueryParam.toString())
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Map<String, Any> jsonBody = JsonIterator.deserialize(response.body().string()).asMap();
            if(jsonBody.get("message").get("messageCode").as(Integer.class) == 1){
                Map<Integer, List<MonitorResponse>> responseMap = new HashMap<>();
                List<Any> monitorsList = jsonBody.get("data").get("monitors").asList();
                monitorsList.forEach(monitor -> {
                    final Integer currentRbl = monitor.get("locationStop").get("properties").get("attributes").get("rbl").as(Integer.class);
                    final List<Any> lineList = monitor.get("lines").asList();
                    final List<MonitorResponse> monitorResponseList = new ArrayList<>(lineList.size());
                    lineList.forEach(line -> {
                        final String lineName = line.get("name").as(String.class);
                        final String towards = line.get("towards").as(String.class);
                        final List<Any> jsonDepartureList = line.get("departures").get("departure").asList();
                        final List<Integer> departureList = new ArrayList<>(jsonDepartureList.size());
                        jsonDepartureList.forEach(departure -> departureList.add(departure.get("departureTime").get("countdown").as(Integer.class)));
                        monitorResponseList.add(new MonitorResponse(lineName, towards, departureList));
                    });
                    responseMap.put(currentRbl, monitorResponseList);
                });
                return responseMap;
            } else {
                throw new IOException("Unexpected response " + jsonBody.get("message").get("messageCode").as(Integer.class) + " " + jsonBody.get("message").get("value").as(String.class));
            }
        }
    }

    public List<Haltestelle> listEndpointsOfLine(int linienId) {
        List<Haltestelle> haltestellenList = listHaltestellen().stream()
                .filter(h -> h.getSteigList() != null)
                .filter(h -> h.getSteigList().stream().anyMatch(steig -> steig.getLinie().getId() == linienId))
                .collect(Collectors.toList());

        List<Haltestelle> endpointList = new ArrayList<>(2);

        final int max = haltestellenList.stream()
                .filter(haltestelle -> haltestelle.getSteigList() != null)
                .map(haltestelle -> haltestelle.getSteigList()
                        .stream()
                        .filter(steig -> steig.getLinie().getId() == linienId)
                        .map(Steig::getReihenfolge)
                        .max(Comparator.comparingInt(o -> o)).get())
                .max(Comparator.comparingInt(o -> o)).get();

        haltestellenList.stream()
                .filter(haltestelle -> haltestelle.getSteigList() != null)
                .filter(haltestelle -> haltestelle.getSteigList().stream()
                        .filter(steig -> steig.getLinie().getId() == linienId)
                        .anyMatch(steig -> steig.getReihenfolge() == 1 || steig.getReihenfolge() == max))
                .distinct()
                .forEach(endpointList::add);

        return endpointList;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
