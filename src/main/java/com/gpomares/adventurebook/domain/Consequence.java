package com.gpomares.adventurebook.domain;

public class Consequence {

    private ConsequenceType type;
    private int value;
    private String text;

    public Consequence() {
    }

    public Consequence(ConsequenceType type, int value, String text) {
        this.type = type;
        this.value = value;
        this.text = text;
    }

    public ConsequenceType getType() {
        return type;
    }

    public void setType(ConsequenceType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
