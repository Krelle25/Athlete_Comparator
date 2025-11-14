package org.example.athlete_comparator.mma_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.mma_client.EspnMMAStatsClient;
import org.example.athlete_comparator.mma_dto.FighterStatDTO;
import org.example.athlete_comparator.shared_dto.CompareResultDTO;
import org.example.athlete_comparator.shared_client.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MMAComparisonService {

    private static final Logger log = LoggerFactory.getLogger(MMAComparisonService.class);
    private final MMAStatsService mmaStatsService;
    private final EspnMMAStatsClient espnMMAStatsClient;
    private final OpenAiClient openAiClient;

    public MMAComparisonService(MMAStatsService mmaStatsService, EspnMMAStatsClient espnMMAStatsClient, OpenAiClient openAiClient) {
        this.mmaStatsService = mmaStatsService;
        this.espnMMAStatsClient = espnMMAStatsClient;
        this.openAiClient = openAiClient;
    }

    final static String SYSTEM_PROMPT = """
            You are an expert MMA analyst with deep knowledge of mixed martial arts, fighting styles, and fighter performance metrics.
            Your task is to provide objective, data-driven comparisons between MMA fighters.
            
            CRITICAL: Weight class differences are PARAMOUNT in MMA. A size/weight advantage of more than one weight class is nearly impossible to overcome.
            
            WEIGHT CLASS HIERARCHY (from lightest to heaviest):
            - Flyweight (125 lbs) → Bantamweight (135) → Featherweight (145) → Lightweight (155) → Welterweight (170)
            - Middleweight (185) → Light Heavyweight (205) → Heavyweight (265)
            
            For OVERALL WINNER (who is the better fighter career-wise), consider:
            - Career statistics and fight record
            - Striking efficiency and accuracy
            - Takedown ability and grappling skills
            - Finishing ability (KO, TKO, submission rates)
            - Championship achievements and title defenses
            - Level of competition faced
            - Career longevity and consistency
            
            For 1v1 FIGHT PREDICTION (who would actually win in a real fight), WEIGHT CLASS IS THE PRIMARY FACTOR:
            - If fighters are in DIFFERENT weight classes (2+ classes apart): The HEAVIER fighter almost always wins due to size, strength, and power advantage
            - Even the most skilled smaller fighter cannot overcome a 30+ pound weight disadvantage in a real fight
            - Examples: A Featherweight (145) vs Heavyweight (265) = Heavyweight wins 99.9% of the time regardless of skill
            - Only if weight classes are similar (same or adjacent) should you analyze:
              * Striking vs grappling style matchup
              * Reach advantages
              * Takedown offense vs takedown defense
              * Knockout power and chin durability
              * Submission skills and ground game
              * Fighting style compatibility
            
            The 1v1 winner and overall winner are often DIFFERENT.
            Example: Fighter A (Featherweight, 30-0 record) is better overall than Fighter B (Heavyweight, 15-5 record), 
            BUT Fighter B would win a 1v1 fight due to 120-pound weight advantage.
            
            When there's a significant weight difference, state clearly: "While [lighter fighter] is the superior fighter overall, 
            in a real fight [heavier fighter] would win due to overwhelming size and strength advantage."
            
            Provide balanced, factual analysis backed by the statistics provided.
            Be specific with numbers and metrics.
            """;

    private String getFighterName(long fighterID) {
        try {
            JsonNode fighterInfo = espnMMAStatsClient.getFighterInfo(fighterID);
            if (fighterInfo != null) {
                String fullName = fighterInfo.path("displayName").asText("");
                if (!fullName.isEmpty()) {
                    return fullName;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch the Fighter name for fighter {}", fighterID, e);
        }

        return "Fighter " + fighterID;
    }

    private void appendFighterSummary(StringBuilder sb, FighterStatDTO stats) {
        sb.append(String.format("  Takedown Accuracy: %.1f%%\n", stats.getTakedownAccuracy()));
        sb.append(String.format("  Striking: %.2f strikes/min, %.1f%% accuracy\n", 
                stats.getStrikeLPM(), stats.getStrikeAccuracy()));
        sb.append(String.format("  Takedown Average: %.2f per 15min\n", stats.getTakedownAvg()));
        sb.append(String.format("  Submission Average: %.2f per 15min\n", stats.getSubmissionAvg()));
        sb.append(String.format("  Finish Rates: %.1f%% KO, %.1f%% TKO, %.1f%% Decision\n",
                stats.getKoPercentage(), stats.getTkoPercentage(), stats.getDecisionPercentage()));
    }

    private void appendFighterInfo(StringBuilder sb, long fighterID) {
        try {
            JsonNode fighterInfo = espnMMAStatsClient.getFighterInfo(fighterID);
            JsonNode recordsInfo = espnMMAStatsClient.getFightRecords(fighterID);
            
            if (fighterInfo != null) {
                String height = fighterInfo.path("displayHeight").asText("");
                String weight = fighterInfo.path("displayWeight").asText("");
                String reach = "";
                
                JsonNode measurements = fighterInfo.path("displayMeasurements");
                if (measurements.isArray()) {
                    for (JsonNode m : measurements) {
                        if ("reach".equalsIgnoreCase(m.path("type").asText(""))) {
                            reach = m.path("displayValue").asText("");
                            break;
                        }
                    }
                }
                
                if (!height.isEmpty()) sb.append(String.format("  Height: %s\n", height));
                if (!weight.isEmpty()) sb.append(String.format("  Weight: %s\n", weight));
                if (!reach.isEmpty()) sb.append(String.format("  Reach: %s\n", reach));
            }
            
            if (recordsInfo != null && recordsInfo.has("items")) {
                JsonNode items = recordsInfo.path("items");
                if (items.isArray() && items.size() > 0) {
                    String record = items.get(0).path("summary").asText("");
                    if (!record.isEmpty()) {
                        sb.append(String.format("  Record: %s\n", record));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch additional fighter info for {}", fighterID, e);
        }
    }

    private String buildUserPrompt(FighterStatDTO fighter1Stats, FighterStatDTO fighter2Stats,
                                    String fighter1Name, String fighter2Name,
                                    long fighter1ID, long fighter2ID) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare these two MMA fighters based on their career statistics:\n\n");

        prompt.append("FIGHTER 1 (").append(fighter1Name).append("):\n");
        appendFighterInfo(prompt, fighter1ID);
        appendFighterSummary(prompt, fighter1Stats);

        prompt.append("\n");

        prompt.append("FIGHTER 2 (").append(fighter2Name).append("):\n");
        appendFighterInfo(prompt, fighter2ID);
        appendFighterSummary(prompt, fighter2Stats);

        prompt.append("\n");
        prompt.append("Based on this data, provide:\n");
        prompt.append("1. Overall Winner: Who is the BETTER FIGHTER OVERALL considering achievements, skills, and career impact?\n");
        prompt.append("2. 1v1 Fight Prediction: Who would WIN A FIGHT between these two based on styles, matchup, and skills?\n");
        prompt.append("   - Consider size/reach advantages\n");
        prompt.append("   - Consider striking vs grappling matchup\n");
        prompt.append("   - Consider their records and finishing ability\n");
        prompt.append("3. Fighter 1 Strengths: What are Fighter 1's main strengths?\n");
        prompt.append("4. Fighter 2 Strengths: What are Fighter 2's main strengths?\n");
        prompt.append("5. Conclusion: Final verdict with key differentiators.\n\n");

        prompt.append("Format your response EXACTLY as follows:\n");
        prompt.append("OVERALL_WINNER: [Fighter 1 or Fighter 2]\n");
        prompt.append("ONE_VS_ONE: [Detailed fight prediction]\n");
        prompt.append("FIGHTER1_STRENGTHS: [List of strengths]\n");
        prompt.append("FIGHTER2_STRENGTHS: [List of strengths]\n");
        prompt.append("CONCLUSION: [Final analysis]");

        return prompt.toString();
    }

    private void setResultField(CompareResultDTO result, String key, String value) {
        switch (key) {
            case "OVERALL_WINNER" -> result.setOverallWinner(value);
            case "ONE_VS_ONE" -> result.setOneVsOnePrediction(value);
            case "FIGHTER1_STRENGTHS" -> result.setAthlete1Strengths(value);
            case "FIGHTER2_STRENGTHS" -> result.setAthlete2Strengths(value);
            case "CONCLUSION" -> result.setConclusion(value);
        }
    }

    private CompareResultDTO parseAiResponse(String response, String fighter1Name, String fighter2Name) {
        CompareResultDTO result = new CompareResultDTO();
        result.setAthlete1Name(fighter1Name);
        result.setAthlete2Name(fighter2Name);

        try {
            StringBuilder currentSection = new StringBuilder();
            String currentKey = null;

            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("OVERALL_WINNER:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "OVERALL_WINNER";
                    currentSection = new StringBuilder(line.substring("OVERALL_WINNER:".length()).trim());
                } else if (line.startsWith("ONE_VS_ONE:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "ONE_VS_ONE";
                    currentSection = new StringBuilder(line.substring("ONE_VS_ONE:".length()).trim());
                } else if (line.startsWith("FIGHTER1_STRENGTHS:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "FIGHTER1_STRENGTHS";
                    currentSection = new StringBuilder(line.substring("FIGHTER1_STRENGTHS:".length()).trim());
                } else if (line.startsWith("FIGHTER2_STRENGTHS:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "FIGHTER2_STRENGTHS";
                    currentSection = new StringBuilder(line.substring("FIGHTER2_STRENGTHS:".length()).trim());
                } else if (line.startsWith("CONCLUSION:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "CONCLUSION";
                    currentSection = new StringBuilder(line.substring("CONCLUSION:".length()).trim());
                } else if (currentKey != null && !line.trim().isEmpty()) {
                    currentSection.append(" ").append(line.trim());
                }
            }

            if (currentKey != null) {
                setResultField(result, currentKey, currentSection.toString().trim());
            }

            result.setAnalysis(response);

            log.debug("Parsed result - Winner: {}, 1v1: {}, F1 Strengths: {}, F2 Strengths: {}, Conclusion: {}",
                    result.getOverallWinner(), result.getOneVsOnePrediction(),
                    result.getAthlete1Strengths(), result.getAthlete2Strengths(), result.getConclusion());
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            result.setAnalysis(response);
        }

        return result;
    }

    public CompareResultDTO compareFighters(long fighter1ID, long fighter2ID) {
        String fighter1Name = getFighterName(fighter1ID);
        String fighter2Name = getFighterName(fighter2ID);

        FighterStatDTO fighter1Stats = mmaStatsService.getTotalStats(fighter1ID);
        FighterStatDTO fighter2Stats = mmaStatsService.getTotalStats(fighter2ID);

        if (fighter1Stats == null || fighter2Stats == null) {
            CompareResultDTO errorResult = new CompareResultDTO();
            errorResult.setAnalysis("Unable to compare: One or both fighters have no available statistics.");
            return errorResult;
        }

        String systemPrompt = SYSTEM_PROMPT;
        String userPrompt = buildUserPrompt(fighter1Stats, fighter2Stats, fighter1Name, fighter2Name, fighter1ID, fighter2ID);

        String aiResponse = openAiClient.sendPrompt(systemPrompt, userPrompt);

        log.debug("OpenAI Response: {}", aiResponse);

        return parseAiResponse(aiResponse, fighter1Name, fighter2Name);
    }
}

