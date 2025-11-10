package org.example.athlete_comparator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.client.EspnStatsClient;
import org.example.athlete_comparator.dto.SeasonStatDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StatsService {

    private final EspnStatsClient espnStatsClient;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StatsService.class);
    private static final Pattern TAIL_INT = Pattern.compile(".*/(\\d+)(?:\\?.*)?$");
    private static final Pattern TYPES_INT = Pattern.compile(".*/types/(\\d+)/.*");

    public StatsService(EspnStatsClient espnStatsClient) {
        this.espnStatsClient = espnStatsClient;
    }

    /**
     * @param type 2 = Regular | 3 = Playoffs | 0 = begge
     */

    public List<SeasonStatDTO> getSeasonStats(long athleteID, int type) {
        JsonNode root = espnStatsClient.getStatisticsLog(athleteID);
        
        if (root == null) {
            log.warn("Failed to fetch statisticslog for athlete {}", athleteID);
            return List.of();
        }
        
        JsonNode entries = root.path("entries");

        if (!entries.isArray() || entries.isEmpty()) {
            log.warn("No entries for athlete {}", athleteID);
            return List.of();
        }

        List<SeasonStatDTO> out = new ArrayList<>();
        
        // Collect unique seasons
        Set<Integer> seasons = new HashSet<>();
        for (JsonNode entry : entries) {
            int season = tail(entry.path("season"));
            if (season > 0) {
                seasons.add(season);
            }
        }
        
        // Fetch stats for each season based on requested type
        if (type == 0) {
            // Fetch both regular season and playoffs
            for (int season : seasons) {
                // Try regular season (type 2)
                try {
                    JsonNode regularStats = espnStatsClient.getSeasonAverage(athleteID, season, 2);
                    if (regularStats != null && !regularStats.path("splits").isMissingNode()) {
                        out.add(mapAveragesToDto(regularStats, season, 2));
                    }
                } catch (Exception e) {
                    log.debug("No regular season stats for season {}: {}", season, e.getMessage());
                }
                
                // Try playoffs (type 3)
                try {
                    JsonNode playoffStats = espnStatsClient.getSeasonAverage(athleteID, season, 3);
                    if (playoffStats != null && !playoffStats.path("splits").isMissingNode()) {
                        out.add(mapAveragesToDto(playoffStats, season, 3));
                    }
                } catch (Exception e) {
                    log.debug("No playoff stats for season {}: {}", season, e.getMessage());
                }
            }
        } else {
            // Fetch specific type (2 or 3) for all seasons
            for (int season : seasons) {
                try {
                    JsonNode stats = espnStatsClient.getSeasonAverage(athleteID, season, type);
                    if (stats != null && !stats.path("splits").isMissingNode()) {
                        out.add(mapAveragesToDto(stats, season, type));
                    }
                } catch (Exception e) {
                    log.debug("No type {} stats for season {}: {}", type, season, e.getMessage());
                }
            }
        }

        out.sort(Comparator.comparingInt(SeasonStatDTO::getSeason));
        return out;
    }

    private static int tail(JsonNode n) {
        String href = n.path("$ref").asText(n.path("href").asText(""));
        Matcher m = TAIL_INT.matcher(href);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static int typeFromRef(String href) {
        var m = TYPES_INT.matcher(href == null ? "" : href);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private SeasonStatDTO mapAveragesToDto(JsonNode avg, int season, int type) {
        SeasonStatDTO dto = new SeasonStatDTO();
        dto.setSeason(season);
        dto.setType(type);

        dto.setGp((int) r(stat(avg, "gamesPlayed")));
        dto.setMin(stat(avg, "avgMinutes"));
        dto.setPts(stat(avg, "avgPoints"));
        dto.setAst(stat(avg, "avgAssists"));
        dto.setReb(stat(avg, "avgRebounds"));
        dto.setStl(stat(avg, "avgSteals"));
        dto.setBlk(stat(avg, "avgBlocks"));
        dto.setTov(stat(avg, "avgTurnovers"));

        dto.setFgm(stat(avg, "avgFieldGoalsMade"));
        dto.setFga(stat(avg, "avgFieldGoalsAttempted"));
        dto.setTpm(stat(avg, "avgThreePointFieldGoalsMade"));
        dto.setTpa(stat(avg, "avgThreePointFieldGoalsAttempted"));
        dto.setFtm(stat(avg, "avgFreeThrowsMade"));
        dto.setFta(stat(avg, "avgFreeThrowsAttempted"));

        // afledte
        double fga = nz(dto.getFga()), fta = nz(dto.getFta());
        double fgm = nz(dto.getFgm()), tpm = nz(dto.getTpm());
        double pts = nz(dto.getPts()), mp  = nz(dto.getMin());

        dto.setTs((fga + 0.44 * fta) > 0 ? pts / (2.0 * (fga + 0.44 * fta)) : null);
        dto.setEfg(fga > 0 ? (fgm + 0.5 * tpm) / fga : null);

        if (mp > 0) {
            dto.setPer75Pts(pts / mp * 75.0);
            dto.setPer75Ast(nz(dto.getAst()) / mp * 75.0);
            dto.setPer75Reb(nz(dto.getReb()) / mp * 75.0);
        }
        
        return dto;
    }

    private static double stat(JsonNode root, String name) {
        JsonNode categories = root.path("splits").path("categories");
        if (categories.isMissingNode()) {
            categories = root.path("categories");
        }
        
        for (JsonNode cat : categories) {
            for (JsonNode st : cat.path("stats")) {
                if (name.equalsIgnoreCase(st.path("name").asText(""))) {
                    JsonNode v = st.path("value");
                    if (v.isNumber()) return v.asDouble();
                    try { return Double.parseDouble(v.asText("0")); } catch (Exception ignored) {}
                }
            }
        }
        return 0.0;
    }

    private static double nz(Double d){ return d == null ? 0.0 : d; }
    private static double r(double v){ return Math.rint(v); }
}