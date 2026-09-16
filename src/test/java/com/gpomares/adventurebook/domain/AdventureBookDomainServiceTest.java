package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdventureBookDomainServiceTest {

    private Optional<AdventureBook> result = Optional.empty();
    private Long lookedUpId;
    private final AdventureBookRepository repository = (AdventureBookRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{AdventureBookRepository.class},
            (proxy, method, args) -> {
                if (method.getName().equals("findById")) {
                    lookedUpId = (Long) args[0];
                    return result;
                }
                return null;
            });
    private final AdventureBookDomainService service = new AdventureBookDomainServiceImpl(repository);

    @Test
    void throwsTheDomainNotFoundExceptionWhenLookupIsEmpty() {
        assertThrows(AdventureBookNotFoundException.class, () -> service.findById(42L));
        org.junit.jupiter.api.Assertions.assertEquals(42L, lookedUpId);
    }

    @Test
    void returnsTheBookFoundByTheRepository() {
        AdventureBook book = AdventureBook.Builder.adventureBook().title("Book").author("Author")
                .difficulty(Difficulty.EASY).build();
        result = Optional.of(book);

        assertSame(book, service.findById(1L));
    }
}
