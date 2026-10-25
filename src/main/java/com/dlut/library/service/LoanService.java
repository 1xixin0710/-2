package com.dlut.library.service;

import com.dlut.library.mapper.BookMapper;
import com.dlut.library.mapper.LoanMapper;
import com.dlut.library.model.Book;
import com.dlut.library.model.Loan;
import com.dlut.library.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanMapper loanMapper;
    private final BookMapper bookMapper;

    public LoanService(LoanMapper loanMapper, BookMapper bookMapper) {
        this.loanMapper = loanMapper;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public void borrow(User reader, Book book, Loan form) {
        if (form.getDueDate().isBefore(LocalDate.now())
                || form.getScheduledPickupAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("预约时间或应还日期已经过期");
        }
        if (loanMapper.countActiveForBook(book.getId(), reader.getUsername()) > 0) {
            throw new IllegalStateException("该图书已有未结束的借阅记录");
        }
        if (bookMapper.decrementAvailable(book.getId()) != 1) {
            throw new IllegalStateException("该图书当前已无可借馆藏");
        }
        form.setLoanNo("L" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        form.setBookId(book.getId());
        form.setBookTitle(book.getTitle());
        form.setBookCoverUrl(book.getCoverUrl());
        form.setCallNumber(book.getCallNumber());
        form.setReaderUsername(reader.getUsername());
        form.setReaderName(reader.getDisplayName());
        form.setContact(form.getContact().trim());
        form.setNote(form.getNote() == null ? "" : form.getNote().trim());
        form.setStatus("待取书");
        loanMapper.insert(form);
    }

    public List<Loan> findByReader(String username) {
        return loanMapper.findByReader(username);
    }

    public List<Loan> findAll() {
        return loanMapper.findAll();
    }

    @Transactional
    public void cancel(Long loanId, User reader) {
        Loan loan = requireLoan(loanId);
        if (loanMapper.cancel(loanId, reader.getUsername()) != 1) {
            throw new IllegalStateException("该预约不能取消");
        }
        bookMapper.incrementAvailable(loan.getBookId());
    }

    @Transactional
    public void confirmPickup(Long loanId) {
        Loan loan = requireLoan(loanId);
        if (loanMapper.confirmPickup(loanId) != 1) {
            throw new IllegalStateException("该预约已失效，无法确认取书");
        }
    }

    @Transactional
    public void returnLoan(Long loanId, User user) {
        Loan loan = requireLoan(loanId);
        boolean librarian = user.isLibrarian();
        if (loanMapper.markReturned(loanId, user.getUsername(), librarian) != 1) {
            throw new IllegalStateException("该图书当前不能归还");
        }
        bookMapper.incrementAvailable(loan.getBookId());
    }

    private Loan requireLoan(Long id) {
        Loan loan = loanMapper.findById(id);
        if (loan == null) {
            throw new IllegalArgumentException("借阅记录不存在");
        }
        return loan;
    }
}
