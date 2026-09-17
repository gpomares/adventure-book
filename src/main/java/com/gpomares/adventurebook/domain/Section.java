package com.gpomares.adventurebook.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "section_number", nullable = false)
    private long sectionNumber;

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
        this.sectionNumber = builder.sectionNumber;
        this.text = builder.text;
        this.type = builder.type;
        builder.options.forEach(this::addOption);
    }

    public static class Builder {

        private long sectionNumber;
        private String text;
        private SectionType type;
        private final List<Option> options = new ArrayList<>();

        private Builder() {
        }

        public static Builder section() {
            return new Builder();
        }

        public Builder id(long id) {
            return sectionNumber(id);
        }

        public Builder sectionNumber(long sectionNumber) {
            this.sectionNumber = sectionNumber;
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

    public Long getId() {
        return id;
    }

    public long getSectionNumber() {
        return sectionNumber;
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
