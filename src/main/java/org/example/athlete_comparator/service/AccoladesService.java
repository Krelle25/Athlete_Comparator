package org.example.athlete_comparator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.client.EspnStatsClient;
import org.example.athlete_comparator.dto.AccoladesDTO;
import org.example.athlete_comparator.dto.AwardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccoladesService {

    private static final Logger log = LoggerFactory.getLogger(AccoladesService.class);
    private final EspnStatsClient espnStatsClient;

    public AccoladesService(EspnStatsClient espnStatsClient) {
        this.espnStatsClient = espnStatsClient;
    }

    /**
     * Assigns priority order to awards for sorting (lower number = higher priority)
     * @param awardTitle The title of the award
     * @return Priority value for sorting
     */
    private int getAwardPriority(String awardTitle) {
        String title = awardTitle.toLowerCase();
        
        // Championships and Finals
        if (title.contains("champion")) return 1;
        if (title.contains("finals mvp")) return 2;
        
        // Regular Season MVP
        if (title.contains("mvp") && !title.contains("finals") && !title.contains("all-star")) return 3;
        
        // All-NBA Teams (ordered by tier)
        if (title.contains("all-nba 1st")) return 4;
        if (title.contains("all-nba 2nd")) return 5;
        if (title.contains("all-nba 3rd")) return 6;
        
        // Defensive Teams
        if (title.contains("defensive player of the year")) return 7;
        if (title.contains("all-defensive 1st")) return 8;
        if (title.contains("all-defensive 2nd")) return 9;
        
        // All-Star
        if (title.contains("all-star mvp")) return 10;
        if (title.contains("all-star")) return 11;
        
        // Scoring and Statistical Leaders
        if (title.contains("scoring")) return 12;
        if (title.contains("assists")) return 13;
        if (title.contains("rebounds")) return 14;
        
        // Rookie Awards
        if (title.contains("rookie of the year")) return 15;
        if (title.contains("all-rookie 1st")) return 16;
        if (title.contains("all-rookie 2nd")) return 17;
        
        // Special Awards (6th Man, Most Improved, etc)
        if (title.contains("sixth man")) return 18;
        if (title.contains("most improved")) return 19;
        
        // NBA Cup / In-Season Tournament
        if (title.contains("nba cup") || title.contains("tournament")) return 20;
        
        // Other awards
        return 99;
    }

    /**
     * Fetches and parses athlete accolades from ESPN bio data
     * @param athleteId ESPN athlete ID
     * @return AccoladesDTO containing player name and list of awards
     */
    public AccoladesDTO getAccolades(long athleteId) {
        try {
            // Fetch bio data
            JsonNode bioData = espnStatsClient.getAthleteBio(athleteId);
            if (bioData == null) {
                log.warn("No bio data found for athlete {}", athleteId);
                return new AccoladesDTO("Unknown Player", List.of());
            }

            // Get player name from info endpoint
            String playerName = "Unknown Player";
            JsonNode athleteInfo = espnStatsClient.getAthleteInfo(athleteId);
            if (athleteInfo != null) {
                playerName = athleteInfo.path("displayName").asText("Unknown Player");
            }

            // Parse awards from bio data
            List<AwardDTO> awards = new ArrayList<>();
            JsonNode awardsNode = bioData.path("awards");
            
            if (awardsNode.isArray() && awardsNode.size() > 0) {
                for (JsonNode awardNode : awardsNode) {
                    // Get award name
                    String title = awardNode.path("name").asText("");
                    
                    // Get display count (e.g., "4x", "13x")
                    String displayCount = awardNode.path("displayCount").asText("");
                    if (!displayCount.isEmpty()) {
                        title = displayCount + " " + title;
                    }
                    
                    // Get seasons array and create description
                    JsonNode seasonsNode = awardNode.path("seasons");
                    String description = "";
                    if (seasonsNode.isArray() && seasonsNode.size() > 0) {
                        List<String> seasons = new ArrayList<>();
                        for (JsonNode seasonNode : seasonsNode) {
                            seasons.add(seasonNode.asText());
                        }
                        description = String.join(", ", seasons);
                    }
                    
                    // Use empty string for year since we're showing seasons in description
                    if (!title.isEmpty()) {
                        awards.add(new AwardDTO(title, "", description));
                    }
                }
            }

            // Sort awards by importance/prestige
            awards.sort((a1, a2) -> {
                int priority1 = getAwardPriority(a1.getTitle());
                int priority2 = getAwardPriority(a2.getTitle());
                return Integer.compare(priority1, priority2);
            });

            log.info("Parsed {} awards for athlete {}", awards.size(), athleteId);
            return new AccoladesDTO(playerName, awards);
        } catch (Exception e) {
            log.error("Error fetching accolades for athlete {}", athleteId, e);
            return new AccoladesDTO("Unknown Player", List.of());
        }
    }
}
