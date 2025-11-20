package org.example.athlete_comparator.MMA.mma_dto;

public class FighterRecordDTO {
    private int wins;
    private int losses;
    private int draws;
    private String record;
    private double winRate;

    public FighterRecordDTO() {
    }

    public FighterRecordDTO(int wins, int losses, int draws, String record, double winRate) {
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.record = record;
        this.winRate = winRate;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
}
