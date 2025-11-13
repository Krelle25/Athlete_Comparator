package org.example.athlete_comparator.nba_dto;

import java.util.List;

public class AccoladesDTO {
    private String playerName;
    private List<AwardDTO> awards;

    public AccoladesDTO() {
    }

    public AccoladesDTO(String playerName, List<AwardDTO> awards) {
        this.playerName = playerName;
        this.awards = awards;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<AwardDTO> getAwards() {
        return awards;
    }

    public void setAwards(List<AwardDTO> awards) {
        this.awards = awards;
    }
}
