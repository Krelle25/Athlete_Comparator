package org.example.athlete_comparator.nba_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.athlete_comparator.nba_dto.PlayerSearchResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EspnNBASearchClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String searchBase;
    private static final Pattern UID_ATHLETE = Pattern.compile("a:(\\d+)");

    public EspnNBASearchClient(@Value("${espn.nba.api.search}") String searchBase,
                               @Value("${espn.api.timeout:5000}") int timeout,
                               ObjectMapper objectMapper)
    {
        this.searchBase = searchBase;
        this.objectMapper = objectMapper;

        var reqFactory = new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(timeout);
        reqFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .requestFactory(reqFactory)
                .build();
    }

    private static long parseAthleteID(String uid) {
        Matcher matcher = UID_ATHLETE.matcher(uid);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : -1;
    }

    private static String encode(String q) {
        try {
            return java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return q;
        }
    }

    public List<PlayerSearchResultDTO> searchPlayers(String query) {
        String url = searchBase + "?limit=20&query=" + encode(query);

        JsonNode root = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
        if (root == null) return List.of();

        List<PlayerSearchResultDTO> out = new ArrayList<>();

        JsonNode results = root.path("results");
        for (JsonNode block : results) {
            if (!"player".equalsIgnoreCase(block.path("type").asText())) continue;

            JsonNode contents = block.path("contents");
            for (JsonNode content : contents) {
                String sport = content.path("sport").asText("");
                String leagueName = content.path("defaultLeagueSlug").asText("");
                if (!"basketball".equalsIgnoreCase(sport) && !"nba".equalsIgnoreCase(leagueName)) continue;

                long athleteID = parseAthleteID(content.path("uid").asText(""));
                if (athleteID <= 0) continue;

                PlayerSearchResultDTO dto = new PlayerSearchResultDTO();
                dto.setID(athleteID);
                dto.setName(content.path("displayName").asText(""));
                dto.setLeague("nba");
                dto.setTeam(content.path("subtitle").asText(""));
                dto.setPosition("");
                dto.setHeadshotUrl(content.path("image").path("default").asText(""));

                out.add(dto);
            }
        }
        return out;
    }
}