package org.example.athlete_comparator.nba_client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EspnNBAStatsClient {

    private final RestClient restClient;
    private final String coreBase;
    private final String webBase;

    public EspnNBAStatsClient(@Value("${espn.nba.api.base}") String coreBase,
                              @Value("${espn.nba.api.web}") String webBase,
                              @Value("${espn.api.timeout:5000}") int timeout) {
        if (coreBase == null || coreBase.isBlank()) {
            throw new IllegalStateException("espn.api.base is not set");
        }
        if (webBase == null || webBase.isBlank()) {
            throw new IllegalStateException("espn.api.web is not set");
        }

        this.coreBase = coreBase;
        this.webBase = webBase;

        var reqFactory = new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(timeout);
        reqFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .requestFactory(reqFactory)
                .build();
    }

    public JsonNode getStatisticsLog(long athleteID) {
        String url = coreBase + "/athletes/" + athleteID + "/statisticslog?region=us&lang=en";
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            return null;
        }
    }

    public JsonNode getSeasonAverage(long athleteID, int season, int type) {
        String url = coreBase + "/seasons/" + season + "/types/" + type
                + "/athletes/" + athleteID + "/statistics/0?region=us&lang=en";
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetches athlete information including name, team, position, and other details
     * @param athleteID The unique ESPN athlete ID
     * @return JsonNode containing athlete information, or null if request fails
     */
    public JsonNode getAthleteInfo(long athleteID) {
        String url = coreBase + "/athletes/" + athleteID + "?region=us&lang=dk";
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetches athlete bio data including awards and accolades
     * @param athleteID The unique ESPN athlete ID
     * @return JsonNode containing athlete bio information, or null if request fails
     */
    public JsonNode getAthleteBio(long athleteID) {
        String url = webBase + "/athletes/" + athleteID + "/bio";
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
