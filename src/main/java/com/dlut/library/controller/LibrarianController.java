package com.dlut.library.controller;

import com.dlut.library.model.Book;
import com.dlut.library.model.User;
import com.dlut.library.service.BookService;
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

import java.time.LocalDateTime;
import java.util.Arrays;

@Controller
@RequestMapping("/librarian/books")
public class LibrarianController {

    private final BookService bookService;

    public LibrarianController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/new")
    public String createPage(Model model, HttpSession session) {
        requireLibrarian(session);
        Book book = new Book();
        book.setCategory("计算机");
        book.setLanguage("中文");
        book.setLocation("伯川图书馆三层");
        book.setStatus("在馆");
        book.setPublishYear(2026);
        book.setTotalCopies(3);
        book.setAcquisitionDate(LocalDateTime.now().withSecond(0).withNano(0));
        model.addAttribute("book", book);
        model.addAttribute("mode", "create");
        addOptions(model);
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        requireLibrarian(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            addOptions(model);
            return "books/form";
        }
        bookService.create(book);
        redirectAttributes.addFlashAttribute("success", "图书已入藏");
        return "redirect:/books/" + book.getId();
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, HttpSession session) {
        requireLibrarian(session);
        Book book = bookService.findById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        book.setTagChoices(book.getTags() == null || book.getTags().isBlank()
                ? java.util.List.of()
                : Arrays.asList(book.getTags().split(",")));
        model.addAttribute("book", book);
        model.addAttribute("mode", "edit");
        addOptions(model);
        return "books/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("book") Book book,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        requireLibrarian(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            addOptions(model);
            return "books/form";
        }
        try {
            bookService.update(id, book);
            redirectAttributes.addFlashAttribute("success", "馆藏信息已更新");
            return "redirect:/books/" + id;
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("update", exception.getMessage());
            model.addAttribute("mode", "edit");
            addOptions(model);
            return "books/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        requireLibrarian(session);
        try {
            bookService.delete(id);
            redirectAttributes.addFlashAttribute("success", "图书已从馆藏中删除");
            return "redirect:/catalog";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/books/" + id;
        }
    }

    private void addOptions(Model model) {
        model.addAttribute("categories", BookController.CATEGORIES);
        model.addAttribute("locations", BookController.LOCATIONS);
        model.addAttribute("languages", BookController.LANGUAGES);
        model.addAttribute("tagOptions", BookController.TAGS);
        model.addAttribute("statuses", BookController.STATUSES);
    }

    private void requireLibrarian(HttpSession session) {
        User user = (User) session.getAttribute("authUser");
        if (user == null || !user.isLibrarian()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅馆员可管理馆藏");
        }
    }
}
