package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.InvalidAdventureBookException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdventureBookTest {

    @Test
    void rejectsBlankCategory() {
        AdventureBook book = book();

        InvalidAdventureBookException exception = assertThrows(
                InvalidAdventureBookException.class, () -> book.addCategory("   "));

        assertEquals("Category must not be blank", exception.getMessage());
    }

    @Test
    void rejectsNullCategory() {
        InvalidAdventureBookException exception = assertThrows(
                InvalidAdventureBookException.class, () -> book().addCategory(null));

        assertEquals("Category must not be blank", exception.getMessage());
    }

    @Test
    void trimsAndAddsValidCategory() {
        AdventureBook book = book();

        book.addCategory(" fantasy ");

        assertEquals(java.util.Set.of("fantasy"), book.getCategories());
    }

    @Test
    void replacesCategoriesAfterNormalizingAndCollapsingDuplicates() {
        AdventureBook book = book();
        book.addCategory("old");

        book.replaceCategories(java.util.List.of(" fantasy ", "fantasy", "Mystery"));

        assertEquals(java.util.Set.of("fantasy", "Mystery"), book.getCategories());
    }

    @Test
    void invalidReplacementDoesNotModifyExistingCategories() {
        AdventureBook book = book();
        book.addCategory("old");

        assertThrows(InvalidAdventureBookException.class,
                () -> book.replaceCategories(java.util.List.of("new", " ")));

        assertEquals(java.util.Set.of("old"), book.getCategories());
    }

    @Test
    void rejectsCategoriesLongerThanThePersistenceColumn() {
        assertThrows(InvalidAdventureBookException.class,
                () -> book().addCategory("a".repeat(101)));
    }

    @Test
    void keepsCategoryCaseSensitive() {
        AdventureBook book = book();

        book.addCategory("Fantasy");
        book.addCategory("fantasy");

        assertEquals(java.util.Set.of("Fantasy", "fantasy"), book.getCategories());
    }

    private AdventureBook book() {
        return AdventureBook.Builder.adventureBook()
                .title("Book").author("Author").difficulty(Difficulty.EASY).build();
    }
}
