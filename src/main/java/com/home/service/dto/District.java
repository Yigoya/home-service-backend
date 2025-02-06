package com.home.service.dto;

public class District {
    private final String englishName;
    private final String amharicName;
    private final String oromoName;
    private final int numberOfWeredas;

    public District(String englishName, String amharicName, String oromoName, int numberOfWeredas) {
        this.englishName = englishName;
        this.amharicName = amharicName;
        this.oromoName = oromoName;
        this.numberOfWeredas = numberOfWeredas;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getAmharicName() {
        return amharicName;
    }

    public String getOromoName() {
        return oromoName;
    }

    public int getNumberOfWeredas() {
        return numberOfWeredas;
    }
}