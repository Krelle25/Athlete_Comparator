package org.example.athlete_comparator.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EspnStatsClient {

    private final RestClient restClient;
    private final String coreBase;

    public EspnStatsClient(@Value("${espn.api.base}") String coreBase,
                           @Value("${espn.api.timeout:5000}") int timeout) {
        if (coreBase == null || coreBase.isBlank()) {
            throw new IllegalStateException("espn.api.base is not set");
        }

        this.coreBase = coreBase;

        var reqFactory = new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(timeout);
        reqFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .requestFactory(reqFactory)
                .build();
    }

    public JsonNode getStatisticsLog(long athleteID) {
        String url = coreBase + "/athletes/" + athleteID + "/statisticslog?region=us&lang=en";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getSeasonAverage(long athleteID, int season, int type) {
        String url = coreBase + "/seasons/" + season + "/types/" + type
                + "/athletes/" + athleteID + "/statistics/0?region=us&lang=en";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getByAbsoluteUrl(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }
}
