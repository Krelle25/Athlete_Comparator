package org.example.athlete_comparator.nba_dto;

public class CareerSummaryDTO {
    private long ID;
    private String name;
    private String team;
    private String headshotUrl;
    private Double pts;
    private Double ast;
    private Double ts;
    private Double efg;

    public CareerSummaryDTO() {
    }

    public CareerSummaryDTO(long ID, String name, String team, String headshotUrl, Double pts, Double ast, Double ts, Double efg) {
        this.ID = ID;
        this.name = name;
        this.team = team;
        this.headshotUrl = headshotUrl;
        this.pts = pts;
        this.ast = ast;
        this.ts = ts;
        this.efg = efg;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
    }

    public Double getPts() {
        return pts;
    }

    public void setPts(Double pts) {
        this.pts = pts;
    }

    public Double getAst() {
        return ast;
    }

    public void setAst(Double ast) {
        this.ast = ast;
    }

    public Double getTs() {
        return ts;
    }

    public void setTs(Double ts) {
        this.ts = ts;
    }

    public Double getEfg() {
        return efg;
    }

    public void setEfg(Double efg) {
        this.efg = efg;
    }
}