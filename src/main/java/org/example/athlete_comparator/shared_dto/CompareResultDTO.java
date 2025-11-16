package org.example.athlete_comparator.shared_dto;

public class CompareResultDTO {
    private String athlete1Name;
    private String athlete2Name;
    private String overallWinner;
    private String oneVsOnePrediction;
    private String analysis;
    private String athlete1Strengths;
    private String athlete2Strengths;
    private String conclusion;

    public CompareResultDTO() {
    }

    public CompareResultDTO(String athlete1Name, String athlete2Name, String overallWinner, 
                           String oneVsOnePrediction, String analysis, String athlete1Strengths, 
                           String athlete2Strengths, String conclusion) {
        this.athlete1Name = athlete1Name;
        this.athlete2Name = athlete2Name;
        this.overallWinner = overallWinner;
        this.oneVsOnePrediction = oneVsOnePrediction;
        this.analysis = analysis;
        this.athlete1Strengths = athlete1Strengths;
        this.athlete2Strengths = athlete2Strengths;
        this.conclusion = conclusion;
    }

    public String getAthlete1Name() {
        return athlete1Name;
    }

    public void setAthlete1Name(String athlete1Name) {
        this.athlete1Name = athlete1Name;
    }

    public String getAthlete2Name() {
        return athlete2Name;
    }

    public void setAthlete2Name(String athlete2Name) {
        this.athlete2Name = athlete2Name;
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

    public String getAthlete1Strengths() {
        return athlete1Strengths;
    }

    public void setAthlete1Strengths(String athlete1Strengths) {
        this.athlete1Strengths = athlete1Strengths;
    }

    public String getAthlete2Strengths() {
        return athlete2Strengths;
    }

    public void setAthlete2Strengths(String athlete2Strengths) {
        this.athlete2Strengths = athlete2Strengths;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
}
