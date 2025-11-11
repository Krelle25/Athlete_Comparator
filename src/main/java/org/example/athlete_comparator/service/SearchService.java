package org.example.athlete_comparator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.client.EspnSearchClient;
import org.example.athlete_comparator.client.EspnStatsClient;
import org.example.athlete_comparator.dto.PlayerSearchResultDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final EspnSearchClient espnSearchClient;
    private final EspnStatsClient espnStatsClient;

    public SearchService(EspnSearchClient espnSearchClient, EspnStatsClient espnStatsClient) {
        this.espnSearchClient = espnSearchClient;
        this.espnStatsClient = espnStatsClient;
    }

    public List<PlayerSearchResultDTO> search(String searchText) {
        if (searchText == null || searchText.isBlank()) return List.of();
        
        List<PlayerSearchResultDTO> results = espnSearchClient.searchPlayers(searchText.trim());
        
        // Enrich results with position data from bio endpoint
        for (PlayerSearchResultDTO player : results) {
            try {
                JsonNode bio = espnStatsClient.getAthleteBio(player.getID());
                if (bio != null) {
                    String position = bio.path("position").path("abbreviation").asText("");
                    if (!position.isEmpty()) {
                        player.setPosition(position);
                    }
                }
            } catch (Exception e) {
                // Continue without position if fetch fails
            }
        }
        
        return results;
    }
}
