package org.example.athlete_comparator.nba_api;

import org.example.athlete_comparator.nba_dto.AccoladesDTO;
import org.example.athlete_comparator.nba_dto.PlayerSearchResultDTO;
import org.example.athlete_comparator.nba_dto.SeasonStatDTO;
import org.example.athlete_comparator.nba_service.AccoladesService;
import org.example.athlete_comparator.nba_service.SearchService;
import org.example.athlete_comparator.nba_service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for athlete-related endpoints.

 * Handles HTTP requests for:
 * - Searching for NBA players
 * - Retrieving individual player statistics

 * All endpoints are prefixed with /api (defined by @RequestMapping)
 */
@RestController
@RequestMapping("/api/nba")  // All endpoints in this controller start with /api/nba
public class AthleteController {

    private final SearchService searchService;
    private final StatsService statsService;
    private final AccoladesService accoladesService;

    public AthleteController(SearchService searchService, StatsService statsService, AccoladesService accoladesService) {
        this.searchService = searchService;
        this.statsService = statsService;
        this.accoladesService = accoladesService;
    }

    /**
     * Search for NBA players by name.

     * Endpoint: GET /api/search?q={searchText}
     * Example: GET /api/search?q=LeBron

     * @param searchText The player name to search for (from query parameter 'q')
     * @return List of matching players with their basic info and headshot URLs
     */
    @GetMapping("/search")
    public List<PlayerSearchResultDTO> search(@RequestParam("q") String searchText) {
        return searchService.search(searchText);
    }

    /**
     * Get season-by-season statistics for a specific athlete.

     * Endpoint: GET /api/athletes/{id}/season-stats?type={type}
     * Example: GET /api/athletes/1966/season-stats?type=2

     * @param athleteID The ESPN athlete ID
     * @param type Stats type: 0 = all stats | 2 = regular season only | 3 = playoffs only (default: 2)
     * @return List of statistics for each season the player has data
     */
    @GetMapping("/athletes/{athleteID}/season-stats")
    public List<SeasonStatDTO> getSeasonStats(@PathVariable long athleteID,
                                              @RequestParam(defaultValue = "2") int type) {
        return statsService.getSeasonStats(athleteID, type);
    }

    /**
     * Get accolades and awards for a specific athlete.

     * Endpoint: GET /api/athletes/{id}/accolades
     * Example: GET /api/athletes/1966/accolades

     * @param id The ESPN athlete ID
     * @return AccoladesDTO containing player name and list of awards/accolades
     */
    @GetMapping("/athletes/{id}/accolades")
    public AccoladesDTO getAccolades(@PathVariable long id) {
        return accoladesService.getAccolades(id);
    }
}
