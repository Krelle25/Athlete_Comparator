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
            
            ⚠️ CRITICAL ANTI-BIAS RULES ⚠️
            1. DO NOT let fighter fame, popularity, or legacy influence your 1v1 fight prediction
            2. Base your analysis ONLY on the provided statistics and data
            3. Do NOT rely on your pre-existing knowledge about these fighters
            4. Ignore historical hype, media narratives, or fan opinions
            5. A famous fighter with a legendary career can still LOSE a 1v1 fight to a lesser-known fighter with physical advantages
            
            IMPORTANT: You must evaluate TWO SEPARATE THINGS:
            1. OVERALL WINNER = Who had the better CAREER (achievements, skill level, legacy)
            2. 1v1 FIGHT = Who would WIN in an ACTUAL FIGHT (considering physical advantages)
            
            These are often DIFFERENT fighters. A smaller fighter with an amazing career (30-0 record, multiple titles) 
            is the OVERALL WINNER, but would LOSE the 1v1 fight to a much larger fighter due to weight/size.
            
            WEIGHT CLASS HIERARCHY (from lightest to heaviest):
            - Flyweight (125 lbs) → Bantamweight (135) → Featherweight (145) → Lightweight (155) → Welterweight (170)
            - Middleweight (185) → Light Heavyweight (205) → Heavyweight (265)
            
            For OVERALL WINNER (who is the better fighter career-wise):
            - Career statistics and fight record (W-L-D record)
            - Title defenses and championship reign
            - Striking efficiency and accuracy
            - Takedown ability and grappling skills
            - Finishing ability (KO, TKO, submission rates)
            - Level of competition faced
            - Career longevity and consistency
            
            🚨 For 1v1 FIGHT PREDICTION (who would actually win in a real fight) 🚨
            STEP 1: MANDATORY WEIGHT CLASS CHECK:
            - IF DIFFERENT WEIGHT CLASSES (2+ classes apart): The HEAVIER fighter wins. Period. No exceptions.
              * Weight/size difference is insurmountable in combat sports
              * 20-40 lbs advantage = massive strength, power, and durability gap
              * Technical skill CANNOT overcome this physical disparity
              * Example: A lightweight (155) CANNOT beat a middleweight (185), even if more skilled
            - IF SAME or ADJACENT weight class (within 1 class): Then analyze fighting styles and matchups below.
            
            STEP 2: Only if same/adjacent weight class, analyze these factors:
              * Recent record and momentum
              * Striking vs grappling style matchup
              * Reach advantages  
              * Takedown offense vs takedown defense
              * Knockout power and finishing ability
              * Submission skills and ground game
              * Fighting style compatibility
            
            REMEMBER: 
            - Weight class difference of 2+ classes = heavier fighter wins the 1v1 automatically
            - If fighters are in the SAME weight class, do NOT mention weight as an advantage
            - The 1v1 winner and overall winner are often DIFFERENT fighters
            - Fame, legacy, and past accomplishments do NOT matter for the 1v1 prediction
            - Use ONLY the provided data - ignore any prior knowledge about these fighters
            
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

    private String appendFighterInfo(StringBuilder sb, long fighterID) {
        String weightClass = null;
        try {
            JsonNode fighterInfo = espnMMAStatsClient.getFighterInfo(fighterID);
            JsonNode recordsInfo = espnMMAStatsClient.getFightRecords(fighterID);
            
            if (fighterInfo != null) {
                // Weight Class
                JsonNode weightClassNode = fighterInfo.path("weightClass");
                if (!weightClassNode.isMissingNode()) {
                    weightClass = weightClassNode.path("text").asText("");
                    if (!weightClass.isEmpty()) {
                        sb.append(String.format("  Weight Class: %s\n", weightClass));
                    }
                }
                
                String height = fighterInfo.path("displayHeight").asText("");
                String weight = fighterInfo.path("displayWeight").asText("");
                String reach = fighterInfo.path("displayReach").asText("");
                
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
        return weightClass;
    }

    private String buildUserPrompt(FighterStatDTO fighter1Stats, FighterStatDTO fighter2Stats,
                                    String fighter1Name, String fighter2Name,
                                    long fighter1ID, long fighter2ID) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare these two MMA fighters based ONLY on the data provided below.\n");
        prompt.append("IGNORE any prior knowledge you have about these fighters. Use ONLY this current data.\n\n");

        prompt.append("FIGHTER 1 (").append(fighter1Name).append("):\n");
        String weightClass1 = appendFighterInfo(prompt, fighter1ID);
        appendFighterSummary(prompt, fighter1Stats);

        prompt.append("\n");

        prompt.append("FIGHTER 2 (").append(fighter2Name).append("):\n");
        String weightClass2 = appendFighterInfo(prompt, fighter2ID);
        appendFighterSummary(prompt, fighter2Stats);

        prompt.append("\n");
        
        // Explicitly state if same weight class
        if (weightClass1 != null && weightClass2 != null && weightClass1.equals(weightClass2)) {
            prompt.append("⚠️ WEIGHT CLASS CHECK: Both fighters compete in the SAME weight class (").append(weightClass1).append(").\n");
            prompt.append("Therefore, weight is NOT an advantage for either fighter in the 1v1 prediction.\n");
            prompt.append("Analyze the matchup based on fighting styles, stats, and techniques only.\n\n");
        } else if (weightClass1 != null && weightClass2 != null) {
            prompt.append("🚨 WEIGHT CLASS CHECK: These fighters are in DIFFERENT weight classes:\n");
            prompt.append("  - ").append(fighter1Name).append(": ").append(weightClass1).append("\n");
            prompt.append("  - ").append(fighter2Name).append(": ").append(weightClass2).append("\n");
            prompt.append("🚨 CRITICAL: The heavier fighter has an insurmountable physical advantage.\n");
            prompt.append("🚨 For the 1v1 prediction, the HEAVIER fighter wins. Do NOT let fame or legacy override this.\n\n");
        }
        
        prompt.append("Based ONLY on the data above, provide:\n");
        prompt.append("1. Overall Winner: Who is the BETTER FIGHTER OVERALL considering achievements, skills, and career impact?\n");
        prompt.append("   - Look at their RECORDS (W-L-D)\n");
        prompt.append("   - Consider title defenses, finish rates, and quality of competition\n");
        prompt.append("   (This is about who had the better career, NOT who would win in a fight)\n\n");
        prompt.append("2. 1v1 Fight Prediction: Who would WIN A HYPOTHETICAL FIGHT between these two?\n");
        prompt.append("   🚨 MANDATORY FIRST STEP: Check the weight classes above.\n");
        prompt.append("   - If DIFFERENT weight classes (2+ apart): The HEAVIER fighter wins. State this clearly.\n");
        prompt.append("   - If SAME weight class: Then analyze fighting styles, striking vs grappling, reach, records.\n");
        prompt.append("   🚨 DO NOT let fighter fame, popularity, or legacy influence the 1v1 prediction.\n");
        prompt.append("   🚨 Physical advantages (size/weight) override technical skill in cross-weight matchups.\n\n");
        prompt.append("3. Fighter 1 Strengths: What are Fighter 1's main strengths based on the data?\n");
        prompt.append("4. Fighter 2 Strengths: What are Fighter 2's main strengths based on the data?\n");
        prompt.append("5. Conclusion: Summarize WHO is better overall AND WHO would win the fight (these may be different fighters).\n\n");

        prompt.append("Format your response EXACTLY as follows:\n");
        prompt.append("OVERALL_WINNER: [Fighter Name] - Better career and accomplishments\n");
        prompt.append("ONE_VS_ONE: [Fighter Name] would win the fight. [Explain why based on stats and matchup]\n");
        prompt.append("FIGHTER1_STRENGTHS: [List of strengths]\n");
        prompt.append("FIGHTER2_STRENGTHS: [List of strengths]\n");
        prompt.append("CONCLUSION: [Summary that clearly states: X is the better fighter overall, but Y would win in an actual fight because...]");

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

        FighterStatDTO fighter1Stats = mmaStatsService.getStatistics(fighter1ID);
        FighterStatDTO fighter2Stats = mmaStatsService.getStatistics(fighter2ID);

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

