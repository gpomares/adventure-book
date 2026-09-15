package com.gpomares.adventurebook.domain;

import java.util.ArrayList;
import java.util.List;

public class AdventureBook {

    private String title;
    private String author;
    private Difficulty difficulty;
    private List<Section> sections = new ArrayList<>();

    public AdventureBook() {
    }

    public AdventureBook(String title, String author, Difficulty difficulty, List<Section> sections) {
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
        this.sections = sections;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
