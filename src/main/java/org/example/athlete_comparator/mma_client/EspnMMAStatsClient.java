package org.example.athlete_comparator.mma_client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EspnMMAStatsClient {

    private final RestClient restClient;
    private final String coreBase;
    private final String ufcBase;

    public EspnMMAStatsClient(@Value("${espn.mma.api.base}") String coreBase,
                              @Value("${espn.mma.api.league}") String ufcBase,
                              @Value("${espn.api.timeout:5000}") int timeout) {
        if (coreBase == null || coreBase.isBlank()) {
            throw new IllegalStateException("espn.mma.api.base is not set");
        }
        if (ufcBase == null || ufcBase.isBlank()) {
            throw new IllegalStateException("espn.mma.api.league is not set");
        }

        this.coreBase = coreBase;
        this.ufcBase = ufcBase;

        var reqFactory = new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(timeout);
        reqFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .requestFactory(reqFactory)
                .build();
    }

    public JsonNode getFightRecords(long athleteID) {
        String url = ufcBase + "/athletes/" + athleteID + "/records?lang=en&region=us";
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        }  catch (Exception e) {
            return  null;
        }
    }

    public JsonNode getFighterInfo(long fighterID) {
        String url = coreBase + "/athletes/" + fighterID;
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            return null;
        }
    }
}
