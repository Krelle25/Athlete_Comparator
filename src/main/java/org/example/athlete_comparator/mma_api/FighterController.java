package org.example.athlete_comparator.mma_api;

import org.example.athlete_comparator.mma_dto.FighterInfoDTO;
import org.example.athlete_comparator.mma_dto.FighterSearchResultDTO;
import org.example.athlete_comparator.mma_dto.FighterStatDTO;
import org.example.athlete_comparator.mma_service.MMARecordsService;
import org.example.athlete_comparator.mma_service.MMASearchService;
import org.example.athlete_comparator.mma_service.MMAStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mma")
public class FighterController {

    private final MMASearchService mmaSearchService;
    private final MMAStatsService mmaStatsService;
    private final MMARecordsService mmaRecordsService;

    public FighterController(MMASearchService mmaSearchService, MMAStatsService  mmaStatsService, MMARecordsService mmaRecordsService) {
        this.mmaSearchService = mmaSearchService;
        this.mmaStatsService = mmaStatsService;
        this.mmaRecordsService = mmaRecordsService;
    }

    @GetMapping("/search")
    public List<FighterSearchResultDTO> searchResultDTOS(@RequestParam("q") String searchText) {
        return mmaSearchService.search(searchText);
    }

    @GetMapping("/fighters/{id}/record")
    public org.example.athlete_comparator.mma_dto.FighterRecordDTO getFighterRecord(@PathVariable("id") long fighterID) {
        return mmaRecordsService.getFighterRecord(fighterID);
    }

    @GetMapping("/fighters/{id}/info")
    public FighterInfoDTO getFighterInfo(@PathVariable("id") long fighterID) {
        return mmaStatsService.getFighterInfo(fighterID);
    }

    @GetMapping("/fighters/{id}/stats")
    public FighterStatDTO getStatistics(@PathVariable("id") long fighterID) {
        return mmaStatsService.getStatistics(fighterID);
    }
}
