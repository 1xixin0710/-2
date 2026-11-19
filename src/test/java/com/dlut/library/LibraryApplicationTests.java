package com.dlut.library;

import com.dlut.library.mapper.BookMapper;
import com.dlut.library.mapper.UserMapper;
import com.dlut.library.model.Book;
import com.dlut.library.model.BookQuery;
import com.dlut.library.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Test
    void registrationBookManagementAndLoanFlowWork() throws Exception {
        MvcResult catalog = mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn();
        assertThat(catalog.getResponse().getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        assertThat(catalog.getResponse().getContentAsString()).contains("大工云图书馆");
        assertThat(catalog.getResponse().getContentAsString())
                .contains("/css/app.css?v=20261102-library");

        register("test_librarian", "测试馆员", "LIB90001", "图书馆");
        register("test_reader", "测试读者", "20269999", "软件学院");

        User librarian = userMapper.findByUsername("test_librarian");
        User reader = userMapper.findByUsername("test_reader");
        assertThat(librarian.getRole()).isEqualTo("LIBRARIAN");
        assertThat(reader.getRole()).isEqualTo("READER");
        assertThat(librarian.getPasswordHash()).startsWith("$2");

        MockHttpSession librarianSession = login("test_librarian");
        mockMvc.perform(bookForm("/librarian/books", "9787300000001").session(librarianSession))
                .andExpect(status().is3xxRedirection());

        BookQuery query = new BookQuery();
        query.setKeyword("测试课程教材");
        Book book = bookMapper.search(query).get(0);
        assertThat(book.getAvailableCopies()).isEqualTo(2);

        MockHttpSession readerSession = login("test_reader");
        mockMvc.perform(post("/loans/borrow/{id}", book.getId())
                        .session(readerSession)
                        .param("borrowType", "课程参考")
                        .param("pickupLocation", "伯川图书馆三层")
                        .param("contact", "13800000000")
                        .param("note", "自动化测试借阅")
                        .param("scheduledPickupAt", LocalDateTime.now().plusDays(1).withNano(0).toString())
                        .param("dueDate", LocalDate.now().plusDays(30).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loans"));

        assertThat(bookMapper.findById(book.getId()).getAvailableCopies()).isEqualTo(1);
        mockMvc.perform(get("/loans").session(readerSession))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateIsbnOnCreateShowsFieldError() throws Exception {
        Book existing = bookMapper.search(new BookQuery()).get(0);
        int originalCount = bookMapper.countTitles();

        mockMvc.perform(bookForm("/librarian/books", existing.getIsbn()).session(librarianSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("该 ISBN 已存在")));

        assertThat(bookMapper.countTitles()).isEqualTo(originalCount);
    }

    @Test
    void duplicateIsbnOnEditKeepsTheOriginalBookAndEditTarget() throws Exception {
        List<Book> books = bookMapper.search(new BookQuery());
        Book existing = books.get(0);
        String editPath = "/librarian/books/" + existing.getId();

        mockMvc.perform(bookForm(editPath, books.get(1).getIsbn()).session(librarianSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("该 ISBN 已存在")))
                .andExpect(content().string(containsString("action=\"" + editPath + "\"")));

        assertThat(bookMapper.findById(existing.getId()).getIsbn()).isEqualTo(existing.getIsbn());
    }

    @Test
    @Transactional
    void editingBookCanKeepItsOwnIsbn() throws Exception {
        Book existing = bookMapper.search(new BookQuery()).get(0);

        mockMvc.perform(bookForm("/librarian/books/" + existing.getId(), existing.getIsbn())
                        .session(librarianSession()))
                .andExpect(redirectedUrl("/books/" + existing.getId()));
    }

    private MockHttpSession librarianSession() {
        User librarian = new User();
        librarian.setUsername("librarian");
        librarian.setDisplayName("测试馆员");
        librarian.setRole("LIBRARIAN");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authUser", librarian);
        return session;
    }

    private MockHttpServletRequestBuilder bookForm(String path, String isbn) {
        return post(path)
                .param("title", "测试课程教材")
                .param("author", "课程组")
                .param("isbn", isbn)
                .param("publisher", "测试出版社")
                .param("publishYear", "2026")
                .param("category", "计算机")
                .param("language", "中文")
                .param("coverUrl", "/images/covers/default-book.jpg")
                .param("summary", "用于自动化测试的课程教材记录。")
                .param("tagChoices", "课程参考")
                .param("callNumber", "TP311/T001")
                .param("location", "伯川图书馆三层")
                .param("totalCopies", "2")
                .param("acquisitionDate", LocalDateTime.now().minusDays(1).withNano(0).toString())
                .param("status", "在馆");
    }

    private void register(String username, String name, String studentNo, String department)
            throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", name)
                        .param("password", "Reader2026!")
                        .param("studentNo", studentNo)
                        .param("department", department))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private MockHttpSession login(String username) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(session)
                        .param("username", username)
                        .param("password", "Reader2026!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"));
        return session;
    }
}
