package com.gpomares.adventurebook.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdventureBookRepository extends JpaRepository<AdventureBook, Long>, JpaSpecificationExecutor<AdventureBook> {
}
