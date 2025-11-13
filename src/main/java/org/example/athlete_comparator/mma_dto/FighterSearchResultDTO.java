package org.example.athlete_comparator.mma_dto;
/**
 * Data Transfer Object for fighter search results.

 * Represents a single fighter in search results from the ESPN API.
 * Used when the user types a fighter name in the search box.

 * Example object:
 * {
 *   "ID": 3022677,
 *   "name": "Connor McGregor",
 *   "headshotUrl": "https://a.espncdn.com/i/headshots/mma/players/full/3022677.png"
 * }
 */

public class FighterSearchResultDTO {
    private Long ID;
    private String name;
    private String sport;
    private String weightClass;
    private String nickname;
    private String record;
    private String headshotUrl;

    public FighterSearchResultDTO() {
    }

    public FighterSearchResultDTO(Long ID, String name, String sport, String weightClass, String nickname, String record, String headshotUrl) {
        this.ID = ID;
        this.name = name;
        this.sport = sport;
        this.weightClass = weightClass;
        this.nickname = nickname;
        this.record = record;
        this.headshotUrl = headshotUrl;
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

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
    }
}
