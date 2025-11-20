package org.example.athlete_comparator.NBA.nba_service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.athlete_comparator.NBA.nba_client.EspnNBAStatsClient;
import org.example.athlete_comparator.shared_client.OpenAiClient;
import org.example.athlete_comparator.shared_dto.CompareResultDTO;
import org.example.athlete_comparator.NBA.nba_dto.SeasonStatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);
    private final StatsService statsService;
    private final EspnNBAStatsClient espnNBAStatsClient;
    private final OpenAiClient openAiClient;

    public ComparisonService(StatsService statsService, EspnNBAStatsClient espnNBAStatsClient, OpenAiClient openAiClient) {
        this.statsService = statsService;
        this.espnNBAStatsClient = espnNBAStatsClient;
        this.openAiClient = openAiClient;
    }

    /**
     * Creates the system prompt that defines the AI's role and analysis criteria
     */
    final static String SYSTEM_PROMPT = """
                You are an expert NBA analyst with deep knowledge of basketball statistics, player performance, and game dynamics.
                Your task is to provide objective, data-driven comparisons between NBA players.
                
                IMPORTANT: Differentiate between "1v1 matchup" and "overall better player":
                
                For OVERALL WINNER, consider:
                - Career statistics and longevity
                - Efficiency metrics (TS%, eFG%, per-75 stats)
                - Consistency across seasons
                - Peak performance and career achievements
                - Playoff performance and championship success
                - Impact on winning and team success
                - All-around game vs specialized skills
                
                For 1v1 PREDICTION, focus on individual matchup factors:
                - Scoring ability (PPG, shooting percentages, scoring versatility)
                - Size, strength, and athleticism advantages
                - Isolation and one-on-one scoring skills
                - Ball handling and shot creation ability
                - Individual defense (steals, blocks)
                - Physical mismatch advantages
                - NOTE: Assists and playmaking are irrelevant in 1v1 (no teammates to pass to)
                
                The 1v1 winner and overall winner can be DIFFERENT players.
                Example: A bigger, more athletic player might win 1v1, but the smaller player could be greater overall due to championships, efficiency, and career impact.
                
                Provide balanced, factual analysis backed by the statistics provided.
                Be specific with numbers and avoid generic statements.
                """;

    /**
     * Fetches player's display name from ESPN API
     *
     * @param athleteId ESPN athlete ID
     * @return Player's full name or fallback to "Player {id}" if not found
     */
    private String getPlayerName(long athleteId) {
        try {
            JsonNode athleteInfo = espnNBAStatsClient.getAthleteInfo(athleteId);
            if (athleteInfo != null) {
                String fullName = athleteInfo.path("displayName").asText("");
                if (!fullName.isEmpty()) {
                    return fullName;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch player name for athlete {}", athleteId, e);
        }
        // Fallback if name cannot be retrieved
        return "Player " + athleteId;
    }

    /**
     * Appends a formatted career summary for a player to the prompt
     *
     * @param sb    StringBuilder to append to
     * @param stats List of season statistics for the player
     */
    private void appendPlayerSummary(StringBuilder sb, List<SeasonStatDTO> stats) {
        // Calculate career totals and averages
        int totalSeasons = stats.size();
        int totalGames = stats.stream().mapToInt(SeasonStatDTO::getGp).sum();

        double avgPts = stats.stream().mapToDouble(SeasonStatDTO::getPts).average().orElse(0);
        double avgAst = stats.stream().mapToDouble(SeasonStatDTO::getAst).average().orElse(0);
        double avgReb = stats.stream().mapToDouble(SeasonStatDTO::getReb).average().orElse(0);
        double avgMin = stats.stream().mapToDouble(SeasonStatDTO::getMin).average().orElse(0);

        // Calculate field goal percentage across all seasons
        double avgFgPct = stats.stream()
                .filter(s -> s.getFga() > 0)
                .mapToDouble(s -> s.getFgm() / s.getFga() * 100)
                .average().orElse(0);

        // Calculate true shooting percentage (efficiency metric)
        double avgTs = stats.stream()
                .filter(s -> s.getTs() != null)
                .mapToDouble(SeasonStatDTO::getTs)
                .average().orElse(0);

        // Append career overview
        sb.append(String.format("  Seasons: %d | Total Games: %d\n", totalSeasons, totalGames));
        sb.append(String.format("  Career Averages: %.1f PPG, %.1f APG, %.1f RPG, %.1f MPG\n",
                avgPts, avgAst, avgReb, avgMin));
        sb.append(String.format("  Shooting: %.1f%% FG, %.1f%% TS\n", avgFgPct, avgTs * 100));

        // Find and display peak scoring season
        SeasonStatDTO bestSeason = stats.stream()
                .max((s1, s2) -> Double.compare(s1.getPts(), s2.getPts()))
                .orElse(null);

        if (bestSeason != null) {
            sb.append(String.format("  Peak Season (%d): %.1f PPG, %.1f APG, %.1f RPG\n",
                    bestSeason.getSeason(), bestSeason.getPts(), bestSeason.getAst(), bestSeason.getReb()));
        }
    }

    /**
     * Builds the user prompt containing player statistics and comparison instructions
     *
     * @param player1Stats Career stats for first player
     * @param player2Stats Career stats for second player
     * @param player1Name  Name of first player
     * @param player2Name  Name of second player
     * @return Formatted prompt string for AI analysis
     */
    private String buildUserPrompt(List<SeasonStatDTO> player1Stats, List<SeasonStatDTO> player2Stats,
                                   String player1Name, String player2Name) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare these two NBA players based on their career statistics:\n\n");

        // Add first player's career summary
        prompt.append("PLAYER 1 (").append(player1Name).append("):\n");
        appendPlayerSummary(prompt, player1Stats);

        prompt.append("\n");

        // Add second player's career summary
        prompt.append("PLAYER 2 (").append(player2Name).append("):\n");
        appendPlayerSummary(prompt, player2Stats);

        // Define what analysis we want from the AI
        prompt.append("\n");
        prompt.append("Based on this data, provide:\n");
        prompt.append("1. Overall Winner: Who is the BETTER PLAYER OVERALL considering career achievements, efficiency, impact, and longevity?\n");
        prompt.append("2. 1v1 Prediction: Who would WIN A 1-ON-1 GAME based on scoring ability, size/athleticism, and individual defense? (Ignore assists/playmaking - no teammates in 1v1)\n");
        prompt.append("3. Player 1 Strengths: What are Player 1's main strengths?\n");
        prompt.append("4. Player 2 Strengths: What are Player 2's main strengths?\n");
        prompt.append("5. Conclusion: Final verdict with key differentiators.\n\n");

        // Specify exact format for parsing
        prompt.append("Format your response EXACTLY as follows:\n");
        prompt.append("OVERALL_WINNER: [Player 1 or Player 2]\n");
        prompt.append("ONE_VS_ONE: [Detailed prediction]\n");
        prompt.append("PLAYER1_STRENGTHS: [List of strengths]\n");
        prompt.append("PLAYER2_STRENGTHS: [List of strengths]\n");
        prompt.append("CONCLUSION: [Final analysis]");

        return prompt.toString();
    }

    /**
     * Helper method to set the appropriate field on the result object
     */
    private void setResultField(CompareResultDTO result, String key, String value) {
        switch (key) {
            case "OVERALL_WINNER" -> result.setOverallWinner(value);
            case "ONE_VS_ONE" -> result.setOneVsOnePrediction(value);
            case "PLAYER1_STRENGTHS" -> result.setAthlete1Strengths(value);
            case "PLAYER2_STRENGTHS" -> result.setAthlete2Strengths(value);
            case "CONCLUSION" -> result.setConclusion(value);
        }
    }

    /**
     * Parses the AI response into structured CompareResultDTO
     * @param response Raw text response from OpenAI
     * @param player1Name Name of first player
     * @param player2Name Name of second player
     * @return Structured comparison result
     */
    private CompareResultDTO parseAiResponse(String response, String player1Name, String player2Name) {
        CompareResultDTO result = new CompareResultDTO();
        result.setAthlete1Name(player1Name);
        result.setAthlete2Name(player2Name);

        try {
            // Parse each section, handling multi-line content
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
                } else if (line.startsWith("PLAYER1_STRENGTHS:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "PLAYER1_STRENGTHS";
                    currentSection = new StringBuilder(line.substring("PLAYER1_STRENGTHS:".length()).trim());
                } else if (line.startsWith("PLAYER2_STRENGTHS:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "PLAYER2_STRENGTHS";
                    currentSection = new StringBuilder(line.substring("PLAYER2_STRENGTHS:".length()).trim());
                } else if (line.startsWith("CONCLUSION:")) {
                    if (currentKey != null) {
                        setResultField(result, currentKey, currentSection.toString().trim());
                    }
                    currentKey = "CONCLUSION";
                    currentSection = new StringBuilder(line.substring("CONCLUSION:".length()).trim());
                } else if (currentKey != null && !line.trim().isEmpty()) {
                    // Continuation of current section
                    currentSection.append(" ").append(line.trim());
                }
            }
            
            // Set the last section
            if (currentKey != null) {
                setResultField(result, currentKey, currentSection.toString().trim());
            }

            // Store full analysis as well
            result.setAnalysis(response);
            
            log.debug("Parsed result - Winner: {}, 1v1: {}, P1 Strengths: {}, P2 Strengths: {}, Conclusion: {}",
                    result.getOverallWinner(), result.getOneVsOnePrediction(), 
                    result.getAthlete1Strengths(), result.getAthlete2Strengths(), result.getConclusion());
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            // If parsing fails, still return the raw analysis
            result.setAnalysis(response);
        }

        return result;
    }

    /**
     * Compares two NBA athletes using their career statistics and AI analysis
     *
     * @param athleteId1 ESPN ID for first player
     * @param athleteId2 ESPN ID for second player
     * @param type       Stats type (0 = all, 2 = regular season, 3 = playoffs)
     * @return CompareResultDTO with analysis and comparison results
     */
    public CompareResultDTO compareAthletes(long athleteId1, long athleteId2, Integer type) {
        // Default to type 0 (all stats) if not provided
        int statsType = type != null ? type : 0;

        // Fetch player names from ESPN API
        String player1Name = getPlayerName(athleteId1);
        String player2Name = getPlayerName(athleteId2);

        // Fetch career statistics for both players
        List<SeasonStatDTO> player1Stats = statsService.getSeasonStats(athleteId1, statsType);
        List<SeasonStatDTO> player2Stats = statsService.getSeasonStats(athleteId2, statsType);

        // Validate that we have stats for both players
        if (player1Stats.isEmpty() || player2Stats.isEmpty()) {
            CompareResultDTO errorResult = new CompareResultDTO();
            errorResult.setAnalysis("Unable to compare: One or both players have no available statistics.");
            return errorResult;
        }

        // Build AI prompts with player data
        String systemPrompt = SYSTEM_PROMPT;
        String userPrompt = buildUserPrompt(player1Stats, player2Stats, player1Name, player2Name);

        // Send to OpenAI for analysis
        String aiResponse = openAiClient.sendPrompt(systemPrompt, userPrompt);
        
        log.debug("OpenAI Response: {}", aiResponse);

        // Parse AI response into structured result
        return parseAiResponse(aiResponse, player1Name, player2Name);
    }
}