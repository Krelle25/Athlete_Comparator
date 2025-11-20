package org.example.athlete_comparator.NBA.nba_api;

import org.example.athlete_comparator.shared_dto.CompareRequestDTO;
import org.example.athlete_comparator.shared_dto.CompareResultDTO;
import org.example.athlete_comparator.NBA.nba_service.ComparisonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for player comparison functionality.

 * This is the main endpoint that:
 * 1. Receives two player IDs and a stats type from the frontend
 * 2. Fetches statistics for both players from ESPN API
 * 3. Sends the data to OpenAI for AI-powered analysis
 * 4. Returns structured comparison results to the frontend
 */
@RestController
@RequestMapping("/api/nba")  // All endpoints start with /api/nba
public class NBACompareController {

    private final ComparisonService comparisonService;

    public NBACompareController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    /**
     * Compare two NBA athletes using AI analysis.

     * Endpoint: POST /api/compare
     * Request body example:
     * {
     *   "aID": 1966,     // LeBron James' ESPN ID
     *   "bID": 3975,     // Stephen Curry's ESPN ID  
     *   "type": 0        // 0 = all stats | 2 = regular season | 3 = playoffs
     * }

     * @param request Contains the two athlete IDs and stats type preference
     * @return Structured comparison result with AI analysis, strengths, and winner determination
     */
    @PostMapping("/compare")
    public CompareResultDTO compareAthletes(@RequestBody CompareRequestDTO request) {
        return comparisonService.compareAthletes(
                request.getaID(),   // Player A's ESPN ID
                request.getbID(),   // Player B's ESPN ID
                request.getType()   // Stats type (0, 2, or 3)
        );
    }
}
