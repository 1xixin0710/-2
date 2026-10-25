package com.dlut.library.controller;

import com.dlut.library.model.Book;
import com.dlut.library.model.BookQuery;
import com.dlut.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class BookController {

    public static final List<String> CATEGORIES = List.of(
            "计算机", "人工智能", "电子商务", "数学", "文学", "历史",
            "经济管理", "外语", "自然科学", "社会科学");
    public static final List<String> LOCATIONS = List.of(
            "伯川图书馆三层", "令希图书馆二层", "开发区馆中文书库", "盘锦馆综合阅览区");
    public static final List<String> LANGUAGES = List.of("中文", "英文");
    public static final List<String> TAGS = List.of(
            "新到馆藏", "课程参考", "考研复习", "经典著作", "热门借阅");
    public static final List<String> STATUSES = List.of("在馆", "下架");

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/catalog";
    }

    @GetMapping("/catalog")
    public String catalog(@ModelAttribute("query") BookQuery query, Model model) {
        model.addAttribute("books", bookService.search(query));
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("titleCount", bookService.countTitles());
        model.addAttribute("availableCount", bookService.countAvailableTitles());
        model.addAttribute("copyCount", bookService.sumCopies());
        model.addAttribute("activeLoanCount", bookService.countActiveLoans());
        return "books/catalog";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        model.addAttribute("book", book);
        return "books/detail";
    }
}
