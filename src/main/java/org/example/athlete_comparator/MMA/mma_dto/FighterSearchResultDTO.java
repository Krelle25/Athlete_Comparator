package org.example.athlete_comparator.MMA.mma_dto;

/**
 * Data Transfer Object for fighter search results.
 * Represents a single fighter in search results from the ESPN API.
 */
public class FighterSearchResultDTO {
    private Long ID;
    private String name;
    private String nickname;
    private String weightClass;
    private String headshotUrl;

    public FighterSearchResultDTO() {
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
    }
}