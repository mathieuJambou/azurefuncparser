package com.function.mj.csvtoxmlparsing;

public class Score {
    
    @SuppressWarnings("unused")
    private String name, major, institution;
    @SuppressWarnings("unused")
    private Integer score;

    public Score(String name, String major, String institution, String score) {
        this.name = name;
        this.major = major;
        this.institution = institution;
        this.score = Integer.parseInt(score);
    }
}