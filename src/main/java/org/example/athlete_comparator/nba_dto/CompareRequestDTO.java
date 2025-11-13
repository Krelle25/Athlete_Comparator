package org.example.athlete_comparator.nba_dto;

/**
 * Data Transfer Object (DTO) for comparison requests from the frontend.

 * This class represents the JSON data sent when comparing two players.
 * Example frontend request:
 * {
 *   "aID": 1966,    // First player's ESPN ID
 *   "bID": 3975,    // Second player's ESPN ID
 *   "type": 0       // Stats type to compare
 * }
 */
public class CompareRequestDTO {
    private long aID;     // Player A's ESPN athlete ID
    private long bID;     // Player B's ESPN athlete ID
    private Integer type; // 0 = all stats, 2 = regular season, 3 = playoffs

    public CompareRequestDTO() {
    }

    public CompareRequestDTO(long aID, long bID, Integer type) {
        this.aID = aID;
        this.bID = bID;
        this.type = type;
    }
    
    public long getaID() {
        return aID;
    }

    public void setaID(long aID) {
        this.aID = aID;
    }

    public long getbID() {
        return bID;
    }

    public void setbID(long bID) {
        this.bID = bID;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
