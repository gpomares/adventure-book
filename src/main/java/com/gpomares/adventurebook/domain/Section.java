package com.gpomares.adventurebook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SectionType type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "section_id", nullable = false)
    private List<Option> options = new ArrayList<>();

    protected Section() {
    }

    private Section(Builder builder) {
        this.id = builder.id;
        this.text = builder.text;
        this.type = builder.type;
        builder.options.forEach(this::addOption);
    }

    public static class Builder {

        private long id;
        private String text;
        private SectionType type;
        private final List<Option> options = new ArrayList<>();

        private Builder() {
        }

        public static Builder section() {
            return new Builder();
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder type(SectionType type) {
            this.type = type;
            return this;
        }

        public Builder options(List<Option> options) {
            this.options.clear();
            if (options != null) {
                this.options.addAll(options);
            }
            return this;
        }

        public Section build() {
            return new Section(this);
        }
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public SectionType getType() {
        return type;
    }

    public List<Option> getOptions() {
        return new ArrayList<>(options);
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public void removeOption(Option option) {
        options.remove(option);
    }
}
