package com.gpomares.adventurebook.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdventureBookSpecifications {

    private AdventureBookSpecifications() {
    }

    public static Specification<AdventureBook> matching(AdventureBookFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addContains(predicates, criteriaBuilder, root.get("title"), filter.title());
            addContains(predicates, criteriaBuilder, root.get("author"), filter.author());

            if (filter.category() != null && !filter.category().isBlank()) {
                Join<AdventureBook, String> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(categoryJoin), normalized(filter.category())));
                query.distinct(true);
            }

            if (filter.difficulty() != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), filter.difficulty()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addContains(List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb,
                                    jakarta.persistence.criteria.Expression<String> field, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(field), "%" + escapedLikeValue(value) + "%", '\\'));
        }
    }

    private static String normalized(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String escapedLikeValue(String value) {
        return normalized(value).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
