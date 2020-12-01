package io.javabrains.coronavirustracker.services;


import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.logging.Slf4JLoggingSystem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String virusDataURL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    /**
     * @PostConstruct tells the Spring when instance of this class is created, method with this annotation has to be executed.
     * @throws IOException
     * @throws InterruptedException
     */
    @PostConstruct
    @Scheduled(cron="* * 1 * * *")
   public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
       HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(virusDataURL))
               .build();
       HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
       System.out.println(httpResponse.body());

        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
//            System.out.println(record.size());
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setLatestTotalCases(Integer.parseInt(record.get(record.size()-1)));
            locationStats.setPreviousTotalCases(Integer.parseInt(record.get(record.size()-2)));
            locationStats.setDiffFromPreviousDay(locationStats.getLatestTotalCases()-locationStats.getPreviousTotalCases());

//            System.out.println(locationStats);
            newStats.add(locationStats);
        }
        this.allStats = newStats;
   }
}
