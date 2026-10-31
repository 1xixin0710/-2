package com.dlut.library.controller;

import com.dlut.library.model.Order;
import com.dlut.library.model.Product;
import com.dlut.library.model.ProductQuery;
import com.dlut.library.model.User;
import com.dlut.library.service.OrderService;
import com.dlut.library.service.ProductService;
import com.dlut.library.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminController(ProductService productService,
                           OrderService orderService,
                           UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.countOnSale());
        model.addAttribute("stockCount", productService.sumStock());
        model.addAttribute("lowStockCount", productService.countLowStock());
        model.addAttribute("outOfStockCount", productService.countOutOfStock());
        model.addAttribute("orderCount", orderService.countAll());
        model.addAttribute("pendingOrders", orderService.countByStatus("待发货"));
        model.addAttribute("shippingOrders", orderService.countByStatus("待收货"));
        model.addAttribute("revenue", orderService.sumRevenue());
        model.addAttribute("recentOrders", orderService.findRecent(6));
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(@ModelAttribute("query") ProductQuery query, Model model) {
        model.addAttribute("products", productService.searchAdmin(query));
        model.addAttribute("categories", ShopController.CATEGORIES);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        Product product = new Product();
        product.setCategory("文学");
        product.setLanguage("中文");
        product.setStatus("上架");
        product.setPublishYear(2026);
        product.setPrice(java.math.BigDecimal.valueOf(59.00));
        product.setOriginalPrice(java.math.BigDecimal.valueOf(79.00));
        product.setStock(20);
        product.setListedAt(LocalDateTime.now().withSecond(0).withNano(0));
        model.addAttribute("product", product);
        model.addAttribute("mode", "create");
        addProductOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute("product") Product product,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            addProductOptions(model);
            return "admin/product-form";
        }
        try {
            productService.create(product);
            redirectAttributes.addFlashAttribute("success", "商品已上架");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("product", exception.getMessage());
            model.addAttribute("mode", "create");
            addProductOptions(model);
            return "admin/product-form";
        }
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        product.setTagChoices(product.getTags() == null || product.getTags().isBlank()
                ? List.of() : Arrays.asList(product.getTags().split(",")));
        model.addAttribute("product", product);
        model.addAttribute("mode", "edit");
        addProductOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            addProductOptions(model);
            return "admin/product-form";
        }
        try {
            productService.update(id, product);
            redirectAttributes.addFlashAttribute("success", "商品信息已更新");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("product", exception.getMessage());
            model.addAttribute("mode", "edit");
            addProductOptions(model);
            return "admin/product-form";
        }
    }

    @PostMapping("/products/{id}/deactivate")
    public String deactivateProduct(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            productService.deactivate(id);
            redirectAttributes.addFlashAttribute("success", "商品已下架，历史订单不受影响");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders = orderService.findAll(status);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status == null ? "" : status);
        model.addAttribute("statuses", orderService.statuses());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    @RequestParam(required = false) String returnStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatusByAdmin(id, status);
            redirectAttributes.addFlashAttribute("success", "订单状态已更新为“" + status + "”");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/orders"
                + (returnStatus == null || returnStatus.isBlank() ? "" : "?status=" + returnStatus);
    }

    @GetMapping("/users")
    public String users(Model model, HttpSession session) {
        User admin = ShopController.currentUser(session);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUsername", admin.getUsername());
        model.addAttribute("adminCount", userService.countAdmins());
        return "admin/users";
    }

    @PostMapping("/users/{username}/role")
    public String updateRole(@PathVariable String username,
                             @RequestParam String role,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.changeRole(ShopController.currentUser(session), username, role);
            redirectAttributes.addFlashAttribute("success", "用户角色已更新");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void addProductOptions(Model model) {
        model.addAttribute("categories", ShopController.CATEGORIES);
        model.addAttribute("languages", ShopController.LANGUAGES);
        model.addAttribute("tagOptions", ShopController.TAGS);
        model.addAttribute("statuses", ShopController.STATUSES);
    }
}
