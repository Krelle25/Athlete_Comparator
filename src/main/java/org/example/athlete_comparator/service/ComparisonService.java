package org.example.athlete_comparator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.athlete_comparator.client.EspnStatsClient;
import org.example.athlete_comparator.client.OpenAiClient;
import org.example.athlete_comparator.dto.CompareResultDTO;
import org.example.athlete_comparator.dto.SeasonStatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);
    private final StatsService statsService;
    private final EspnStatsClient espnStatsClient;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public ComparisonService(StatsService statsService, EspnStatsClient espnStatsClient, OpenAiClient openAiClient) {
        this.statsService = statsService;
        this.espnStatsClient = espnStatsClient;
        this.openAiClient = openAiClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompareResultDTO compareAthletes(long athleteId1, long athleteId2, Integer type) {
        // Default to type 0 (all stats) if not provided
        int statsType = type != null ? type : 0;

        // Fetch player names
        String player1Name = getPlayerName(athleteId1);
        String player2Name = getPlayerName(athleteId2);

        // Fetch stats for both players
        List<SeasonStatDTO> player1Stats = statsService.getSeasonStats(athleteId1, statsType);
        List<SeasonStatDTO> player2Stats = statsService.getSeasonStats(athleteId2, statsType);

        if (player1Stats.isEmpty() || player2Stats.isEmpty()) {
            CompareResultDTO errorResult = new CompareResultDTO();
            errorResult.setAnalysis("Unable to compare: One or both players have no available statistics.");
            return errorResult;
        }

        // Build the prompt for AI
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(player1Stats, player2Stats, player1Name, player2Name);

        // Get AI analysis
        String aiResponse = openAiClient.sendPrompt(systemPrompt, userPrompt);

        // Parse and structure the response
        return parseAiResponse(aiResponse, player1Name, player2Name);
    }

    private String getPlayerName(long athleteId) {
        try {
            JsonNode athleteInfo = espnStatsClient.getAthleteInfo(athleteId);
            if (athleteInfo != null) {
                String fullName = athleteInfo.path("displayName").asText("");
                if (!fullName.isEmpty()) {
                    return fullName;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch player name for athlete {}", athleteId, e);
        }
        return "Player " + athleteId;
    }

    private String buildSystemPrompt() {
        return """
                You are an expert NBA analyst with deep knowledge of basketball statistics, player performance, and game dynamics.
                Your task is to provide objective, data-driven comparisons between NBA players.
                
                When comparing players, consider:
                - Career statistics (points, assists, rebounds, shooting percentages)
                - Efficiency metrics (TS%, eFG%, per-75 stats)
                - Consistency across seasons
                - Peak performance vs career longevity
                - Playoff performance vs regular season
                - All-around game vs specialized skills
                
                Provide balanced, factual analysis backed by the statistics provided.
                Be specific with numbers and avoid generic statements.
                """;
    }

    private String buildUserPrompt(List<SeasonStatDTO> player1Stats, List<SeasonStatDTO> player2Stats, 
                                    String player1Name, String player2Name) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare these two NBA players based on their career statistics:\n\n");

        // Player 1 summary
        prompt.append("PLAYER 1 (").append(player1Name).append("):\n");
        appendPlayerSummary(prompt, player1Stats);

        prompt.append("\n");

        // Player 2 summary
        prompt.append("PLAYER 2 (").append(player2Name).append("):\n");
        appendPlayerSummary(prompt, player2Stats);

        prompt.append("\n");
        prompt.append("Based on this data, provide:\n");
        prompt.append("1. Overall Winner: Who is the better player overall and why?\n");
        prompt.append("2. 1v1 Prediction: Who would win in a 1-on-1 game and why?\n");
        prompt.append("3. Player 1 Strengths: What are Player 1's main strengths?\n");
        prompt.append("4. Player 2 Strengths: What are Player 2's main strengths?\n");
        prompt.append("5. Conclusion: Final verdict with key differentiators.\n\n");
        prompt.append("Format your response EXACTLY as follows:\n");
        prompt.append("OVERALL_WINNER: [Player 1 or Player 2]\n");
        prompt.append("ONE_VS_ONE: [Detailed prediction]\n");
        prompt.append("PLAYER1_STRENGTHS: [List of strengths]\n");
        prompt.append("PLAYER2_STRENGTHS: [List of strengths]\n");
        prompt.append("CONCLUSION: [Final analysis]");

        return prompt.toString();
    }

    private void appendPlayerSummary(StringBuilder sb, List<SeasonStatDTO> stats) {
        // Career totals/averages
        int totalSeasons = stats.size();
        int totalGames = stats.stream().mapToInt(SeasonStatDTO::getGp).sum();
        
        double avgPts = stats.stream().mapToDouble(SeasonStatDTO::getPts).average().orElse(0);
        double avgAst = stats.stream().mapToDouble(SeasonStatDTO::getAst).average().orElse(0);
        double avgReb = stats.stream().mapToDouble(SeasonStatDTO::getReb).average().orElse(0);
        double avgMin = stats.stream().mapToDouble(SeasonStatDTO::getMin).average().orElse(0);
        double avgFgPct = stats.stream()
                .filter(s -> s.getFga() > 0)
                .mapToDouble(s -> s.getFgm() / s.getFga() * 100)
                .average().orElse(0);
        double avgTs = stats.stream()
                .filter(s -> s.getTs() != null)
                .mapToDouble(SeasonStatDTO::getTs)
                .average().orElse(0);

        sb.append(String.format("  Seasons: %d | Total Games: %d\n", totalSeasons, totalGames));
        sb.append(String.format("  Career Averages: %.1f PPG, %.1f APG, %.1f RPG, %.1f MPG\n", 
                avgPts, avgAst, avgReb, avgMin));
        sb.append(String.format("  Shooting: %.1f%% FG, %.1f%% TS\n", avgFgPct, avgTs * 100));

        // Peak season
        SeasonStatDTO bestSeason = stats.stream()
                .max((s1, s2) -> Double.compare(s1.getPts(), s2.getPts()))
                .orElse(null);
        
        if (bestSeason != null) {
            sb.append(String.format("  Peak Season (%d): %.1f PPG, %.1f APG, %.1f RPG\n",
                    bestSeason.getSeason(), bestSeason.getPts(), bestSeason.getAst(), bestSeason.getReb()));
        }
    }

    private CompareResultDTO parseAiResponse(String response, String player1Name, String player2Name) {
        CompareResultDTO result = new CompareResultDTO();
        result.setPlayer1Name(player1Name);
        result.setPlayer2Name(player2Name);
        
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("OVERALL_WINNER:")) {
                    result.setOverallWinner(line.substring("OVERALL_WINNER:".length()).trim());
                } else if (line.startsWith("ONE_VS_ONE:")) {
                    result.setOneVsOnePrediction(line.substring("ONE_VS_ONE:".length()).trim());
                } else if (line.startsWith("PLAYER1_STRENGTHS:")) {
                    result.setPlayer1Strengths(line.substring("PLAYER1_STRENGTHS:".length()).trim());
                } else if (line.startsWith("PLAYER2_STRENGTHS:")) {
                    result.setPlayer2Strengths(line.substring("PLAYER2_STRENGTHS:".length()).trim());
                } else if (line.startsWith("CONCLUSION:")) {
                    result.setConclusion(line.substring("CONCLUSION:".length()).trim());
                }
            }
            
            result.setAnalysis(response);
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            result.setAnalysis(response);
        }

        return result;
    }
}

