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

    private AdventureBook book() {
        return AdventureBook.Builder.adventureBook()
                .title("Book").author("Author").difficulty(Difficulty.EASY).build();
    }
}
