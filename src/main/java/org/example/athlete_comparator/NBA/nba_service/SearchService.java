package org.example.athlete_comparator.NBA.nba_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.NBA.nba_client.EspnNBASearchClient;
import org.example.athlete_comparator.NBA.nba_client.EspnNBAStatsClient;
import org.example.athlete_comparator.NBA.nba_dto.PlayerSearchResultDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final EspnNBASearchClient espnNBASearchClient;
    private final EspnNBAStatsClient espnNBAStatsClient;

    public SearchService(EspnNBASearchClient espnNBASearchClient, EspnNBAStatsClient espnNBAStatsClient) {
        this.espnNBASearchClient = espnNBASearchClient;
        this.espnNBAStatsClient = espnNBAStatsClient;
    }

    public List<PlayerSearchResultDTO> search(String searchText) {
        if (searchText == null || searchText.isBlank()) return List.of();
        
        List<PlayerSearchResultDTO> results = espnNBASearchClient.searchPlayers(searchText.trim());
        
        // Enrich results with position, height, and weight data from bio endpoint
        for (PlayerSearchResultDTO player : results) {
            try {
                JsonNode bio = espnNBAStatsClient.getAthleteInfo(player.getID());
                if (bio != null) {
                    String position = bio.path("position").path("abbreviation").asText("");
                    if (!position.isEmpty()) {
                        player.setPosition(position);
                    }
                    
                    String displayHeight = bio.path("displayHeight").asText("");
                    if (!displayHeight.isEmpty()) {
                        player.setDisplayHeight(displayHeight);
                    }
                    
                    String displayWeight = bio.path("displayWeight").asText("");
                    if (!displayWeight.isEmpty()) {
                        player.setDisplayWeight(displayWeight);
                    }
                }
            } catch (Exception e) {
                // Continue without position/height/weight if fetch fails
            }
        }
        
        return results;
    }
}
