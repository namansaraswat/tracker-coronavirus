package com.prep.coronavirustracker.services;


import com.prep.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {

        List<LocationStats> newStats = new ArrayList<>();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        //System.out.println(httpResponse.body());

        StringReader cvsBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(cvsBodyReader);
        for (CSVRecord record : records) {

            LocationStats locationStat = new LocationStats();
            String state = record.get("Province/State");
            locationStat.setState(state);
            String country = record.get("Country/Region");
            locationStat.setCountry(country);
            String latitude = record.get("Lat");
            locationStat.setLatitude(latitude);
            String longitude = record.get("Long");
            locationStat.setLongitude(longitude);
            int latestDayCases = Integer.parseInt(record.get(record.size() - 1));
            locationStat.setLatestTotalCases(latestDayCases);
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setDiffFromPrevDay(latestDayCases - prevDayCases);
            //System.out.println(locationStat);
            newStats.add(locationStat);
        }

        this.allStats = newStats;

    }


}
