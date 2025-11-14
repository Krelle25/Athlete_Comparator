package org.example.athlete_comparator.mma_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.mma_client.EspnMMAStatsClient;
import org.example.athlete_comparator.mma_dto.FighterInfoDTO;
import org.example.athlete_comparator.mma_dto.FighterRecordDTO;
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

        if (fighterData == null) {
            log.warn("Failed to fetch fighter info for {}", fighterID);
            return null;
        }

        FighterInfoDTO info = new FighterInfoDTO();

        // Basic info
        info.setGender(fighterData.path("gender").asText(""));
        info.setStyles(fighterData.path("styles").asText(""));
        info.setStance(fighterData.path("stance").asText(""));
        info.setName(fighterData.path("displayName").asText(""));
        info.setNickname(fighterData.path("nickname").asText(""));
        info.setHeight(fighterData.path("displayHeight").asText(""));
        info.setWeight(fighterData.path("displayWeight").asText(""));
        info.setAge(fighterData.path("age").asInt(0));

        // Weight class - direct from weightClass.text
        JsonNode weightClassNode = fighterData.path("weightClass");
        if (!weightClassNode.isMissingNode()) {
            info.setWeightClass(weightClassNode.path("text").asText(""));
        }

        // Country - direct string, not array
        String citizenship = fighterData.path("citizenship").asText("");
        if (!citizenship.isEmpty()) {
            info.setCountry(citizenship);
        }

        // Headshot
        JsonNode headshot = fighterData.path("headshot");
        if (headshot.has("href")) {
            info.setHeadshotUrl(headshot.path("href").asText(""));
        }

        // Reach - direct from displayReach at root level
        String reach = fighterData.path("displayReach").asText("");
        if (!reach.isEmpty()) {
            info.setReach(reach);
        }

        String accolades = fighterData.path("accolades").asText("");
        if (!accolades.isEmpty()) {
            info.setAccolades(accolades);
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

