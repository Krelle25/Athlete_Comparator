package org.example.athlete_comparator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.client.EspnStatsClient;
import org.example.athlete_comparator.dto.SeasonStatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StatsService {

    private final EspnStatsClient espnStatsClient;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StatsService.class);
    private static final Pattern TAIL_INT = Pattern.compile(".*/(\\d+)$");
    private static final Pattern TYPES_INT = Pattern.compile(".*/types/(\\d+)/.*");

    public StatsService(EspnStatsClient espnStatsClient) {
        this.espnStatsClient = espnStatsClient;
    }

    /**
     * @param type 2 = Regular | 3 = Playoffs | 0 = begge
     */

    public List<SeasonStatDTO> getSeasonStats(long athleteID, int type) {
        JsonNode root = espnStatsClient.getStatisticsLog(athleteID);
        JsonNode entries = root.path("entries");

        if (!entries.isArray() || entries.isEmpty()) {
            log.warn("No entries for athlete {}", athleteID);
            return List.of();
        }

        log.info("entries.size={}", entries.size());
        List<SeasonStatDTO> out = new ArrayList<>();

        for (JsonNode entry : entries) {
            int season = tail(entry.path("season"));
            if (season <= 0) { log.debug("skip: bad season ref {}", entry.path("season")); continue; }

            JsonNode statsBlocks = entry.path("statistics");
            if (!statsBlocks.isArray()) { log.debug("skip: no statistics array for season {}", season); continue; }

            for (JsonNode block : statsBlocks) {
                String blockType = block.path("type").asText("");
                if (!"total".equalsIgnoreCase(blockType)) { log.debug("skip: block.type={} (want total)", blockType); continue; }

                String ref = block.path("statistics").path("$ref")
                        .asText(block.path("statistics").path("href").asText(""));
                if (ref.isBlank()) { log.debug("skip: empty ref for season {}", season); continue; }

                int seasonType = typeFromRef(ref); // matcher .../types/{n}/...
                if (type != 0 && seasonType != type) {
                    log.debug("skip: filtered by type (got {}, want {})", seasonType, type);
                    continue;
                }

                log.info("Fetching averages: season={}, type={}, ref={}", season, seasonType, ref);
                JsonNode avg = espnStatsClient.getByAbsoluteUrl(ref);
                if (avg == null) { log.debug("skip: avg null (ref={})", ref); continue; }

                out.add(mapAveragesToDto(avg, season, seasonType));
            }
        }

        out.sort(Comparator.comparingInt(SeasonStatDTO::getSeason));
        log.info("Returning {} rows for athlete {}", out.size(), athleteID);
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
        dto.setMin(stat(avg, "minutesPerGame"));
        dto.setPts(stat(avg, "pointsPerGame"));
        dto.setAst(stat(avg, "assistsPerGame"));
        dto.setReb(stat(avg, "reboundsPerGame"));
        dto.setStl(stat(avg, "stealsPerGame"));
        dto.setBlk(stat(avg, "blocksPerGame"));
        dto.setTov(stat(avg, "turnoversPerGame"));

        dto.setFgm(stat(avg, "fieldGoalsMadePerGame"));
        dto.setFga(stat(avg, "fieldGoalsAttemptedPerGame"));
        dto.setTpm(stat(avg, "threePointFieldGoalsMadePerGame"));
        dto.setTpa(stat(avg, "threePointFieldGoalsAttemptedPerGame"));
        dto.setFtm(stat(avg, "freeThrowsMadePerGame"));
        dto.setFta(stat(avg, "freeThrowsAttemptedPerGame"));

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
        for (JsonNode cat : root.path("categories")) {
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