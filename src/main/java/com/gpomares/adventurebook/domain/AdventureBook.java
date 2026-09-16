package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.InvalidAdventureBookException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.*;

@Entity
@Table(name = "adventure_books")
public class AdventureBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category", nullable = false, length = 100)
    private Set<String> categories = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "book_id", nullable = false)
    private List<Section> sections = new ArrayList<>();

    protected AdventureBook() {
    }

    private AdventureBook(Builder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.difficulty = builder.difficulty;
        builder.categories.forEach(this::addCategory);
        builder.sections.forEach(this::addSection);
    }

    public static class Builder {

        private String title;
        private String author;
        private Difficulty difficulty;
        private final Set<String> categories = new LinkedHashSet<>();
        private final List<Section> sections = new ArrayList<>();

        private Builder() {
        }

        public static Builder adventureBook() {
            return new Builder();
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder categories(Set<String> categories) {
            this.categories.clear();
            if (categories != null) {
                categories.forEach(category -> this.categories.add(normalizeCategory(category)));
            }
            return this;
        }

        public Builder sections(List<Section> sections) {
            this.sections.clear();
            if (sections != null) {
                this.sections.addAll(sections);
            }
            return this;
        }

        public AdventureBook build() {
            return new AdventureBook(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Set<String> getCategories() {
        return new HashSet<>(categories);
    }

    public List<Section> getSections() {
        return new ArrayList<>(sections);
    }

    public void addSection(Section section) {
        Objects.requireNonNull(section, "section must not be null");
        if (!sections.contains(section)) {
            sections.add(section);
        }
    }

    public void removeSection(Section section) {
        sections.remove(section);
    }

    public boolean addCategory(String category) {
        return categories.add(normalizeCategory(category));
    }

    public void removeCategory(String category) {
        if (category != null) {
            categories.remove(normalizeCategory(category));
        }
    }

    public void replaceCategories(Collection<String> replacement) {
        if (replacement == null) {
            throw new InvalidAdventureBookException("Categories must not be null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        replacement.forEach(category -> normalized.add(normalizeCategory(category)));
        categories.clear();
        categories.addAll(normalized);
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new InvalidAdventureBookException("Category must not be blank");
        }

        String normalized = category.trim();
        if (normalized.length() > 100) {
            throw new InvalidAdventureBookException("Category must not be longer than 100 characters");
        }
        return normalized;
    }
}
