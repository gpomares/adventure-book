package com.gpomares.adventurebook.domain;

import com.gpomares.adventurebook.exception.AdventureBookNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdventureBookDomainServiceImpl implements AdventureBookDomainService {

    private final AdventureBookRepository adventureBookRepository;

    @Autowired
    public AdventureBookDomainServiceImpl(AdventureBookRepository adventureBookRepository) {
        this.adventureBookRepository = adventureBookRepository;
    }

    @Override
    public AdventureBook findById(Long id) {
        return adventureBookRepository.findById(id).orElseThrow(() -> new AdventureBookNotFoundException(id));
    }

    @Override
    public List<AdventureBook> search(AdventureBookFilter filter) {
        return adventureBookRepository.findAll(AdventureBookSpecifications.matching(filter),
                Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id")));
    }

    @Override
    @Transactional
    public boolean addCategory(Long id, String category) {
        AdventureBook book = findById(id);

        boolean added = book.addCategory(category);
        if (added) {
            adventureBookRepository.save(book);
        }
        return added;
    }

    @Override
    @Transactional
    public void replaceCategories(Long id, List<String> categories) {
        AdventureBook book = findById(id);

        book.replaceCategories(categories);
        adventureBookRepository.save(book);
    }

    @Override
    @Transactional
    public void removeCategory(Long id, String category) {
        AdventureBook book = findById(id);

        book.removeCategory(category);
        adventureBookRepository.save(book);
    }
}
