package org.example.athlete_comparator.nba_dto;

public class CompareResultDTO {
    private String player1Name;
    private String player2Name;
    private String overallWinner;
    private String oneVsOnePrediction;
    private String analysis;
    private String player1Strengths;
    private String player2Strengths;
    private String conclusion;

    public CompareResultDTO() {
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getOverallWinner() {
        return overallWinner;
    }

    public void setOverallWinner(String overallWinner) {
        this.overallWinner = overallWinner;
    }

    public String getOneVsOnePrediction() {
        return oneVsOnePrediction;
    }

    public void setOneVsOnePrediction(String oneVsOnePrediction) {
        this.oneVsOnePrediction = oneVsOnePrediction;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getPlayer1Strengths() {
        return player1Strengths;
    }

    public void setPlayer1Strengths(String player1Strengths) {
        this.player1Strengths = player1Strengths;
    }

    public String getPlayer2Strengths() {
        return player2Strengths;
    }

    public void setPlayer2Strengths(String player2Strengths) {
        this.player2Strengths = player2Strengths;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
}
