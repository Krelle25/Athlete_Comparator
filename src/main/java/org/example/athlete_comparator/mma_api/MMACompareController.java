package org.example.athlete_comparator.mma_api;

import org.example.athlete_comparator.shared_dto.CompareResultDTO;
import org.example.athlete_comparator.mma_service.MMAComparisonService;
import org.example.athlete_comparator.shared_dto.CompareRequestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mma")
public class MMACompareController {

    private final MMAComparisonService mmaComparisonService;

    public MMACompareController(MMAComparisonService mmaComparisonService) {
        this.mmaComparisonService = mmaComparisonService;
    }

    @PostMapping("/compare")
    public CompareResultDTO compareFighters(@RequestBody CompareRequestDTO compareRequestDTO) {
        return mmaComparisonService.compareFighters(
                compareRequestDTO.getaID(),
                compareRequestDTO.getbID()
        );
    }
}
