package org.example.athlete_comparator.api;

import org.example.athlete_comparator.dto.CompareRequestDTO;
import org.example.athlete_comparator.dto.CompareResultDTO;
import org.example.athlete_comparator.service.ComparisonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompareController {

    private final ComparisonService comparisonService;

    public CompareController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
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
