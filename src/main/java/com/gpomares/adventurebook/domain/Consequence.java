package com.gpomares.adventurebook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Consequence {

    @Enumerated(EnumType.STRING)
    @Column(name = "consequence_type", length = 20)
    private ConsequenceType type;

    @Column(name = "consequence_value")
    private int value;

    @Column(name = "consequence_text", columnDefinition = "TEXT")
    private String text;

    protected Consequence() {
    }

    private Consequence(Builder builder) {
        this.type = builder.type;
        this.value = builder.value;
        this.text = builder.text;
    }

    public static class Builder {

        private ConsequenceType type;
        private int value;
        private String text;

        private Builder() {
        }

        public static Builder consequence() {
            return new Builder();
        }

        public Builder type(ConsequenceType type) {
            this.type = type;
            return this;
        }

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Consequence build() {
            return new Consequence(this);
        }
    }

    public ConsequenceType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

}
