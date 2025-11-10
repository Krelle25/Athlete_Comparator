package org.example.athlete_comparator.dto;

public class PlayerSearchResultDTO {
    private long ID;
    private String name;
    private String league;
    private String team;
    private String position;
    private String headshotUrl;

    public PlayerSearchResultDTO() {
    }

    public PlayerSearchResultDTO(long ID, String name, String league, String team, String position, String headshotUrl) {
        this.ID = ID;
        this.name = name;
        this.league = league;
        this.team = team;
        this.position = position;
        this.headshotUrl = headshotUrl;
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

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
    }
}
