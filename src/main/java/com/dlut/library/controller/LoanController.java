package com.dlut.library.controller;

import com.dlut.library.model.Book;
import com.dlut.library.model.Loan;
import com.dlut.library.model.User;
import com.dlut.library.service.BookService;
import com.dlut.library.service.LoanService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;

    public LoanController(LoanService loanService, BookService bookService) {
        this.loanService = loanService;
        this.bookService = bookService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User reader = currentUser(session);
        model.addAttribute("librarianMode", reader.isLibrarian());
        model.addAttribute("loans", reader.isLibrarian()
                ? loanService.findAll()
                : loanService.findByReader(reader.getUsername()));
        return "loans/list";
    }

    @GetMapping("/borrow/{bookId}")
    public String borrowPage(@PathVariable Long bookId, HttpSession session, Model model) {
        Book book = requireBook(bookId);
        if (!book.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该图书当前不可借");
        }
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setBorrowType("个人借阅");
        loan.setPickupLocation(book.getLocation());
        loan.setDueDate(LocalDate.now().plusDays(30));
        model.addAttribute("book", book);
        model.addAttribute("loan", loan);
        model.addAttribute("borrowTypes", java.util.List.of("个人借阅", "课程参考", "科研用书"));
        model.addAttribute("pickupLocations", BookController.LOCATIONS);
        return "loans/borrow";
    }

    @PostMapping("/borrow/{bookId}")
    public String borrow(@PathVariable Long bookId,
                         @Valid @ModelAttribute("loan") Loan loan,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User reader = currentUser(session);
        Book book = requireBook(bookId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("borrowTypes", java.util.List.of("个人借阅", "课程参考", "科研用书"));
            model.addAttribute("pickupLocations", BookController.LOCATIONS);
            return "loans/borrow";
        }
        try {
            loanService.borrow(reader, book, loan);
            redirectAttributes.addFlashAttribute("success", "借阅申请已提交，请按预约时间取书");
            return "redirect:/loans";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("borrow", exception.getMessage());
            model.addAttribute("book", book);
            model.addAttribute("borrowTypes", java.util.List.of("个人借阅", "课程参考", "科研用书"));
            model.addAttribute("pickupLocations", BookController.LOCATIONS);
            return "loans/borrow";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            loanService.cancel(id, currentUser(session));
            redirectAttributes.addFlashAttribute("success", "预约已取消，馆藏数量已恢复");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/pickup")
    public String pickup(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        requireLibrarian(session);
        try {
            loanService.confirmPickup(id);
            redirectAttributes.addFlashAttribute("success", "已确认取书，借阅期开始计算");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnLoan(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            loanService.returnLoan(id, currentUser(session));
            redirectAttributes.addFlashAttribute("success", "图书已归还，馆藏数量已恢复");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/loans";
    }

    private Book requireBook(Long id) {
        Book book = bookService.findById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        return book;
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("authUser");
    }

    private void requireLibrarian(HttpSession session) {
        User user = currentUser(session);
        if (user == null || !user.isLibrarian()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅馆员可执行此操作");
        }
    }
}
