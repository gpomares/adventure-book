package com.gpomares.adventurebook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "goto_id", nullable = false)
    private long gotoId;

    @Embedded
    private Consequence consequence;

    protected Option() {
    }

    private Option(Builder builder) {
        this.description = builder.description;
        this.gotoId = builder.gotoId;
        this.consequence = builder.consequence;
    }

    public static class Builder {

        private String description;
        private long gotoId;
        private Consequence consequence;

        private Builder() {
        }

        public static Builder option() {
            return new Builder();
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder gotoId(long gotoId) {
            this.gotoId = gotoId;
            return this;
        }

        public Builder consequence(Consequence consequence) {
            this.consequence = consequence;
            return this;
        }

        public Option build() {
            return new Option(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getGotoId() {
        return gotoId;
    }

    public Consequence getConsequence() {
        return consequence;
    }

}
