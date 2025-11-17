package org.example.athlete_comparator.mma_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.mma_client.EspnMMASearchClient;
import org.example.athlete_comparator.mma_client.EspnMMAStatsClient;
import org.example.athlete_comparator.mma_dto.FighterSearchResultDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MMASearchService {

    private final EspnMMASearchClient espnMMASearchClient;
    private final EspnMMAStatsClient espnMMAStatsClient;

    public MMASearchService(EspnMMASearchClient espnMMASearchClient, EspnMMAStatsClient espnMMAStatsClient) {
        this.espnMMASearchClient = espnMMASearchClient;
        this.espnMMAStatsClient = espnMMAStatsClient;
    }

    public List<FighterSearchResultDTO> search(String searchText) {
        if (searchText == null || searchText.isBlank()) return List.of();

        List<FighterSearchResultDTO> results = espnMMASearchClient.searchFighters(searchText.trim());

        for (FighterSearchResultDTO fighter : results) {
            try {
                JsonNode bio = espnMMAStatsClient.getFighterInfo(fighter.getID());
                if (bio != null) {
                    String nickname = bio.path("nickname").asText("");
                    if (!nickname.isEmpty()) {
                        fighter.setNickname(nickname);
                    }
                }
            } catch (Exception e) {
                // Continue without extra info if the fetch fails
            }
        }

        return results;
    }
}
