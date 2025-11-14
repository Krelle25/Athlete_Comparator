package org.example.athlete_comparator.mma_api;

import org.example.athlete_comparator.mma_dto.FighterInfoDTO;
import org.example.athlete_comparator.mma_dto.FighterSearchResultDTO;
import org.example.athlete_comparator.mma_dto.FighterStatDTO;
import org.example.athlete_comparator.mma_service.MMASearchService;
import org.example.athlete_comparator.mma_service.MMAStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mma")
public class FighterController {

    private final MMASearchService mmaSearchService;
    private final MMAStatsService mmaStatsService;

    public FighterController(MMASearchService mmaSearchService, MMAStatsService  mmaStatsService) {
        this.mmaSearchService = mmaSearchService;
        this.mmaStatsService = mmaStatsService;
    }

    @GetMapping("/search")
    public List<FighterSearchResultDTO> searchResultDTOS(@RequestParam("q") String searchText) {
        return mmaSearchService.search(searchText);
    }

    @GetMapping("/fighters/{id}/stats")
    public FighterStatDTO getTotalStats(@PathVariable("id") long fighterID) {
        return mmaStatsService.getTotalStats(fighterID);
    }

    @GetMapping("/fighters/{id}/info")
    public FighterInfoDTO getFighterInfo(@PathVariable("id") long fighterID) {
        return mmaStatsService.getFighterInfo(fighterID);
    }
}
