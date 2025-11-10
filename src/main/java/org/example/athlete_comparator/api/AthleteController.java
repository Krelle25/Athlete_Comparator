package org.example.athlete_comparator.api;

import org.example.athlete_comparator.dto.CompareRequestDTO;
import org.example.athlete_comparator.dto.CompareResultDTO;
import org.example.athlete_comparator.dto.PlayerSearchResultDTO;
import org.example.athlete_comparator.dto.SeasonStatDTO;
import org.example.athlete_comparator.service.ComparisonService;
import org.example.athlete_comparator.service.SearchService;
import org.example.athlete_comparator.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AthleteController {

    private final SearchService searchService;
    private final StatsService statsService;
    private final ComparisonService comparisonService;

    public AthleteController(SearchService searchService, StatsService statsService, 
                            ComparisonService comparisonService) {
        this.searchService = searchService;
        this.statsService = statsService;
        this.comparisonService = comparisonService;
    }

    @GetMapping("/search")
    public List<PlayerSearchResultDTO> search(@RequestParam("q") String searchText) {
        return searchService.search(searchText);
    }

    @GetMapping("/athletes/{id}/season-stats")
    public List<SeasonStatDTO> getSeasonStats(@PathVariable long id,
                                              @RequestParam(defaultValue = "2") int type) {
        return statsService.getSeasonStats(id, type);
    }

    @PostMapping("/compare")
    public CompareResultDTO compareAthletes(@RequestBody CompareRequestDTO request) {
        return comparisonService.compareAthletes(
            request.getaID(), 
            request.getbID(), 
            request.getType()
        );
    }
}
