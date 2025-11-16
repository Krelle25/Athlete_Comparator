package org.example.athlete_comparator.mma_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.mma_client.EspnMMAStatsClient;
import org.example.athlete_comparator.mma_dto.FighterRecordDTO;
import org.springframework.stereotype.Service;

@Service
public class MMARecordsService {

    private final EspnMMAStatsClient espnMMAStatsClient;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MMAStatsService.class);

    public MMARecordsService(EspnMMAStatsClient espnMMAStatsClient) {
        this.espnMMAStatsClient = espnMMAStatsClient;
    }

    public FighterRecordDTO getFighterRecord(long fighterID) {
        JsonNode recordsData = espnMMAStatsClient.getFightRecords(fighterID);

        FighterRecordDTO record = new FighterRecordDTO();
        
        if (recordsData == null || !recordsData.has("items")) {
            log.warn("Failed to fetch records for fighter {}", fighterID);
            return record; // Return empty record instead of null
        }

        JsonNode items = recordsData.path("items");

        if (items.isArray() && items.size() > 0) {
            String summary = items.get(0).path("summary").asText("");
            if (!summary.isEmpty()) {
                record.setRecord(summary);

                // Parse "X-Y-Z" format
                String[] parts = summary.split("-");
                if (parts.length >= 2) {
                    try {
                        int wins = Integer.parseInt(parts[0].trim());
                        int losses = Integer.parseInt(parts[1].trim());
                        int draws = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : 0;

                        record.setWins(wins);
                        record.setLosses(losses);
                        record.setDraws(draws);

                        // Calculate win rate
                        int totalFights = wins + losses + draws;
                        if (totalFights > 0) {
                            double winRate = (wins * 100.0) / totalFights;
                            record.setWinRate(winRate);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse record: {}", summary);
                    }
                }
            }
        }

        return record;
    }
}
