package com.gpomares.adventurebook.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdventureBookRepository extends JpaRepository<AdventureBook, Long> {
}
