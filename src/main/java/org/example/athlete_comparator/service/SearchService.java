package org.example.athlete_comparator.service;

import org.example.athlete_comparator.client.EspnSearchClient;
import org.example.athlete_comparator.dto.PlayerSearchResultDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final EspnSearchClient espnSearchClient;

    public SearchService(EspnSearchClient espnSearchClient) {
        this.espnSearchClient = espnSearchClient;
    }

    public List<PlayerSearchResultDTO> search(String searchText) {
        if (searchText == null || searchText.isBlank()) return List.of();
        return espnSearchClient.searchPlayers(searchText.trim());
    }
}
