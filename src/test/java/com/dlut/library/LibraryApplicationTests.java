package com.dlut.library;

import com.dlut.library.mapper.ProductMapper;
import com.dlut.library.mapper.UserMapper;
import com.dlut.library.model.Product;
import com.dlut.library.model.ProductQuery;
import com.dlut.library.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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
    private ProductMapper productMapper;

    @Test
    void registrationAdminProductCartOrderAndStatusFlowWork() throws Exception {
        MvcResult catalog = mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn();
        assertThat(catalog.getResponse().getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        assertThat(catalog.getResponse().getContentAsString()).contains("云序书集");

        register("shop_admin", "测试管理员", "ADM90001", "商学院");
        register("shop_customer", "测试顾客", "20269999", "软件学院");

        User admin = userMapper.findByUsername("shop_admin");
        User customer = userMapper.findByUsername("shop_customer");
        assertThat(admin.getRole()).isEqualTo("ADMIN");
        assertThat(customer.getRole()).isEqualTo("CUSTOMER");
        assertThat(admin.getPasswordHash()).startsWith("$2");

        MockHttpSession adminSession = login("shop_admin");
        MockHttpSession customerSession = login("shop_customer");

        mockMvc.perform(get("/admin").session(adminSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin").session(customerSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/products")
                        .session(adminSession)
                        .param("name", "测试电商教材")
                        .param("author", "课程编写组")
                        .param("isbn", "9787300000002")
                        .param("publisher", "测试出版社")
                        .param("publishYear", "2026")
                        .param("category", "电子商务")
                        .param("language", "中文")
                        .param("price", "59.80")
                        .param("originalPrice", "79.80")
                        .param("stock", "5")
                        .param("coverUrl", "/images/covers/default-book.jpg")
                        .param("summary", "用于自动化测试的电子商务课程教材。")
                        .param("tagChoices", "课程教材")
                        .param("listedAt", LocalDateTime.now().minusDays(1).withNano(0).toString())
                        .param("status", "上架"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        ProductQuery query = new ProductQuery();
        query.setKeyword("测试电商教材");
        Product product = productMapper.searchAdmin(query).get(0);
        assertThat(product.getStock()).isEqualTo(5);

        mockMvc.perform(post("/cart/add")
                        .session(customerSession)
                        .param("productId", product.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        MvcResult checkout = mockMvc.perform(post("/checkout")
                        .session(customerSession)
                        .param("recipient", "测试顾客")
                        .param("phone", "13800000000")
                        .param("deliveryType", "校园配送")
                        .param("address", "软件学院测试楼")
                        .param("remark", "自动化测试订单"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(checkout.getResponse().getRedirectedUrl()).startsWith("/orders/");
        assertThat(productMapper.findById(product.getId()).getStock()).isEqualTo(3);
        assertThat(productMapper.findById(product.getId()).getSoldCount()).isEqualTo(2);

        String orderId = checkout.getResponse().getRedirectedUrl().replace("/orders/", "");
        mockMvc.perform(get("/orders/{id}", orderId).session(customerSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/orders").session(customerSession))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/orders/{id}/status", orderId)
                        .session(adminSession)
                        .param("status", "待收货"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/orders/{id}/receive", orderId).session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderId));
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
