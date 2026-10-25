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
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        register("test_librarian", "测试馆员", "LIB90001", "图书馆");
        register("test_reader", "测试读者", "20269999", "软件学院");

        User librarian = userMapper.findByUsername("test_librarian");
        User reader = userMapper.findByUsername("test_reader");
        assertThat(librarian.getRole()).isEqualTo("LIBRARIAN");
        assertThat(reader.getRole()).isEqualTo("READER");
        assertThat(librarian.getPasswordHash()).startsWith("$2");

        MockHttpSession librarianSession = login("test_librarian");
        mockMvc.perform(post("/librarian/books")
                        .session(librarianSession)
                        .param("title", "测试课程教材")
                        .param("author", "课程组")
                        .param("isbn", "9787300000001")
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
                        .param("acquisitionDate", "2026-10-25T09:00")
                        .param("status", "在馆"))
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
                        .param("scheduledPickupAt", "2026-10-26T10:00")
                        .param("dueDate", "2026-11-25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loans"));

        assertThat(bookMapper.findById(book.getId()).getAvailableCopies()).isEqualTo(1);
        mockMvc.perform(get("/loans").session(readerSession))
                .andExpect(status().isOk());
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
