package com.dlut.library.service;

import com.dlut.library.mapper.BookMapper;
import com.dlut.library.model.Book;
import com.dlut.library.model.BookQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<Book> search(BookQuery query) {
        query.setKeyword(blankToNull(query.getKeyword()));
        query.setCategory(blankToNull(query.getCategory()));
        query.setAvailability(blankToNull(query.getAvailability()));
        return bookMapper.search(query);
    }

    public Book findById(Long id) {
        return bookMapper.findById(id);
    }

    @Transactional
    public void create(Book book) {
        normalize(book);
        book.setAvailableCopies(book.getTotalCopies());
        bookMapper.insert(book);
    }

    @Transactional
    public void update(Long id, Book book) {
        Book existing = bookMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        int loanedCopies = existing.getTotalCopies() - existing.getAvailableCopies();
        if (book.getTotalCopies() < loanedCopies) {
            throw new IllegalArgumentException("馆藏册数不能少于当前借出册数");
        }
        normalize(book);
        book.setId(id);
        book.setAvailableCopies(book.getTotalCopies() - loanedCopies);
        bookMapper.update(book);
    }

    @Transactional
    public void delete(Long id) {
        Book existing = bookMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (existing.getTotalCopies() > existing.getAvailableCopies()) {
            throw new IllegalArgumentException("仍有在借或待取记录，不能删除该书");
        }
        bookMapper.deleteById(id);
    }

    public int countTitles() {
        return bookMapper.countTitles();
    }

    public int countAvailableTitles() {
        return bookMapper.countAvailableTitles();
    }

    public int sumCopies() {
        return bookMapper.sumCopies();
    }

    public int countActiveLoans() {
        return bookMapper.countActiveLoans();
    }

    private void normalize(Book book) {
        book.setIsbn(book.getIsbn().trim());
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setPublisher(book.getPublisher().trim());
        book.setCallNumber(book.getCallNumber().trim().toUpperCase());
        book.setSummary(book.getSummary().trim());
        book.setTags(String.join(",", book.getTagChoices()));
        if (book.getCoverUrl() == null || book.getCoverUrl().isBlank()) {
            book.setCoverUrl("/images/covers/default-book.jpg");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
