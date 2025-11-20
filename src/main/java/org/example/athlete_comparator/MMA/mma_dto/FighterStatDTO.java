package org.example.athlete_comparator.MMA.mma_dto;

public class FighterStatDTO {
    private double takedownAccuracy;
    private double strikeLPM;
    private double strikeAccuracy;
    private double takedownAvg;
    private double submissionAvg;
    private double koPercentage;
    private double tkoPercentage;
    private double decisionPercentage;

    public FighterStatDTO() {
    }

    public FighterStatDTO(double takedownAccuracy, double strikeLPM, double strikeAccuracy, double takedownAvg, double submissionAvg, double koPercentage, double tkoPercentage, double decisionPercentage) {
        this.takedownAccuracy = takedownAccuracy;
        this.strikeLPM = strikeLPM;
        this.strikeAccuracy = strikeAccuracy;
        this.takedownAvg = takedownAvg;
        this.submissionAvg = submissionAvg;
        this.koPercentage = koPercentage;
        this.tkoPercentage = tkoPercentage;
        this.decisionPercentage = decisionPercentage;
    }

    public double getTakedownAccuracy() {
        return takedownAccuracy;
    }

    public void setTakedownAccuracy(double takedownAccuracy) {
        this.takedownAccuracy = takedownAccuracy;
    }

    public double getStrikeLPM() {
        return strikeLPM;
    }

    public void setStrikeLPM(double strikeLPM) {
        this.strikeLPM = strikeLPM;
    }

    public double getStrikeAccuracy() {
        return strikeAccuracy;
    }

    public void setStrikeAccuracy(double strikeAccuracy) {
        this.strikeAccuracy = strikeAccuracy;
    }

    public double getTakedownAvg() {
        return takedownAvg;
    }

    public void setTakedownAvg(double takedownAvg) {
        this.takedownAvg = takedownAvg;
    }

    public double getSubmissionAvg() {
        return submissionAvg;
    }

    public void setSubmissionAvg(double submissionAvg) {
        this.submissionAvg = submissionAvg;
    }

    public double getKoPercentage() {
        return koPercentage;
    }

    public void setKoPercentage(double koPercentage) {
        this.koPercentage = koPercentage;
    }

    public double getTkoPercentage() {
        return tkoPercentage;
    }

    public void setTkoPercentage(double tkoPercentage) {
        this.tkoPercentage = tkoPercentage;
    }

    public double getDecisionPercentage() {
        return decisionPercentage;
    }

    public void setDecisionPercentage(double decisionPercentage) {
        this.decisionPercentage = decisionPercentage;
    }
}
