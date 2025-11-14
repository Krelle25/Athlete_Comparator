package org.example.athlete_comparator.mma_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.mma_client.EspnMMAStatsClient;
import org.example.athlete_comparator.mma_dto.FighterInfoDTO;
import org.example.athlete_comparator.mma_dto.FighterStatDTO;
import org.springframework.stereotype.Service;

@Service
public class MMAStatsService {

    private final EspnMMAStatsClient espnMMAStatsClient;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MMAStatsService.class);

    public MMAStatsService(EspnMMAStatsClient espnMMAStatsClient) {
        this.espnMMAStatsClient = espnMMAStatsClient;
    }

    public FighterStatDTO getTotalStats(long fighterID) {
        JsonNode stats = espnMMAStatsClient.getStatistics(fighterID);

        if (stats == null) {
            log.warn("Failed to fetch statistics for fighter {}", fighterID);
            return null;
        }

        return mapToDto(stats);
    }

    public FighterInfoDTO getFighterInfo(long fighterID) {
        JsonNode fighterData = espnMMAStatsClient.getFighterInfo(fighterID);
        JsonNode recordsData = espnMMAStatsClient.getFightRecords(fighterID);

        if (fighterData == null) {
            log.warn("Failed to fetch fighter info for {}", fighterID);
            return null;
        }

        FighterInfoDTO info = new FighterInfoDTO();

        // Basic info
        info.setName(fighterData.path("displayName").asText(""));
        info.setNickname(fighterData.path("nickname").asText(""));
        info.setWeightClass(fighterData.path("position").path("name").asText(""));
        info.setHeight(fighterData.path("displayHeight").asText(""));
        info.setWeight(fighterData.path("displayWeight").asText(""));
        info.setAge(fighterData.path("age").asInt(0));
        
        // Country
        JsonNode citizenship = fighterData.path("citizenship");
        if (citizenship.isArray() && citizenship.size() > 0) {
            info.setCountry(citizenship.get(0).asText(""));
        }

        // Headshot
        JsonNode headshot = fighterData.path("headshot");
        if (headshot.has("href")) {
            info.setHeadshotUrl(headshot.path("href").asText(""));
        }

        // Reach stats
        JsonNode displayMeasurements = fighterData.path("displayMeasurements");
        if (displayMeasurements.isArray()) {
            for (JsonNode measurement : displayMeasurements) {
                String type = measurement.path("type").asText("");
                String value = measurement.path("displayValue").asText("");
                
                if ("reach".equalsIgnoreCase(type)) {
                    info.setReach(value);
                } else if ("legReach".equalsIgnoreCase(type)) {
                    info.setLegReach(value);
                }
            }
        }

        // Records - extract wins/losses/draws
        if (recordsData != null && recordsData.has("items")) {
            JsonNode items = recordsData.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String summary = item.path("summary").asText("");
                    if (!summary.isEmpty()) {
                        info.setRecord(summary);
                        // Parse "X-Y-Z" format
                        String[] parts = summary.split("-");
                        if (parts.length >= 2) {
                            try {
                                info.setWins(Integer.parseInt(parts[0].trim()));
                                info.setLosses(Integer.parseInt(parts[1].trim()));
                                if (parts.length >= 3) {
                                    info.setDraws(Integer.parseInt(parts[2].trim()));
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse record: {}", summary);
                            }
                        }
                        break;
                    }
                }
            }
        }

        return info;
    }

    private FighterStatDTO mapToDto(JsonNode root) {
        FighterStatDTO dto = new FighterStatDTO();

        JsonNode categories = root.path("splits").path("categories");
        if (!categories.isArray() || categories.isEmpty()) {
            log.warn("No categories found in statistics");
            return dto;
        }

        for (JsonNode category : categories) {
            for (JsonNode stat : category.path("stats")) {
                String name = stat.path("name").asText("");
                double value = stat.path("value").asDouble(0.0);

                switch (name) {
                    case "takedownAccuracy":
                        dto.setTakedownAccuracy(value);
                        break;
                    case "strikeLPM":
                        dto.setStrikeLPM(value);
                        break;
                    case "strikeAccuracy":
                        dto.setStrikeAccuracy(value);
                        break;
                    case "takedownAvg":
                        dto.setTakedownAvg(value);
                        break;
                    case "submissionAvg":
                        dto.setSubmissionAvg(value);
                        break;
                    case "koPercentage":
                        dto.setKoPercentage(value);
                        break;
                    case "tkoPercentage":
                        dto.setTkoPercentage(value);
                        break;
                    case "decisionPercentage":
                        dto.setDecisionPercentage(value);
                        break;
                }
            }
        }

        return dto;
    }
}

