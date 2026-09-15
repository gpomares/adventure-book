package com.gpomares.adventurebook.domain;

public class Option {

    private String description;
    private int gotoId;
    private Consequence consequence;

    public Option() {
    }

    public Option(String description, int gotoId, Consequence consequence) {
        this.description = description;
        this.gotoId = gotoId;
        this.consequence = consequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGotoId() {
        return gotoId;
    }

    public void setGotoId(int gotoId) {
        this.gotoId = gotoId;
    }

    public Consequence getConsequence() {
        return consequence;
    }

    public void setConsequence(Consequence consequence) {
        this.consequence = consequence;
    }
}
