package org.example.athlete_comparator.mma_dto;

public class FighterInfoDTO {
    private String name;
    private String nickname;
    private String headshotUrl;
    private String weightClass;
    private String height;
    private String weight;
    private String reach;
    private String country;
    private int age;
    private String gender;
    private String styles;
    private String accolades;
    private String stance;

    public FighterInfoDTO() {
    }

    public FighterInfoDTO(String name, String nickname, String headshotUrl, String weightClass, String height, String weight, String reach, String country, int age, String gender, String styles, String accolades, String stance) {
        this.name = name;
        this.nickname = nickname;
        this.headshotUrl = headshotUrl;
        this.weightClass = weightClass;
        this.height = height;
        this.weight = weight;
        this.reach = reach;
        this.country = country;
        this.age = age;
        this.gender = gender;
        this.styles = styles;
        this.accolades = accolades;
        this.stance = stance;
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

    public String getHeadshotUrl() {
        return headshotUrl;
    }

    public void setHeadshotUrl(String headshotUrl) {
        this.headshotUrl = headshotUrl;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getReach() {
        return reach;
    }

    public void setReach(String reach) {
        this.reach = reach;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public String getAccolades() {
        return accolades;
    }

    public void setAccolades(String accolades) {
        this.accolades = accolades;
    }

    public String getStance() {
        return stance;
    }

    public void setStance(String stance) {
        this.stance = stance;
    }
}