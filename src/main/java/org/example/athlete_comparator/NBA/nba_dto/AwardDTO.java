package org.example.athlete_comparator.NBA.nba_dto;

public class AwardDTO {
    private String title;
    private String year;
    private String description;

    public AwardDTO() {
    }

    public AwardDTO(String title, String year, String description) {
        this.title = title;
        this.year = year;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
