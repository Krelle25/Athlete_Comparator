package org.example.athlete_comparator.controller_healthz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/healthz")
    public Map<String,String> health() {
        return Map.of(
                "status","UP",
                "service", "athlete-comparator"
        );
    }
}
