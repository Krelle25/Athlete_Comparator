package org.example.athlete_comparator.dto;

public class CompareRequestDTO {
    private long aID;
    private long bID;
    Integer type;

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
