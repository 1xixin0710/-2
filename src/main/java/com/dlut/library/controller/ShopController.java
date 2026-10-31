package com.dlut.library.controller;

import com.dlut.library.model.Product;
import com.dlut.library.model.ProductQuery;
import com.dlut.library.model.User;
import com.dlut.library.service.CartService;
import com.dlut.library.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ShopController {

    public static final List<String> CATEGORIES = List.of(
            "文学", "人工智能", "电子商务", "计算机", "经济管理",
            "数学", "自然科学", "历史", "外语", "社会科学");
    public static final List<String> TAGS = List.of(
            "课程教材", "经典名著", "人工智能", "电子商务", "考研复习", "精选推荐");
    public static final List<String> STATUSES = List.of("上架", "下架");
    public static final List<String> LANGUAGES = List.of("中文", "英文");
    public static final List<String> PRICE_RANGES = List.of("50以下", "50至100", "100以上");

    private final ProductService productService;
    private final CartService cartService;

    public ShopController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/catalog";
    }

    @GetMapping("/catalog")
    public String catalog(@ModelAttribute("query") ProductQuery query, Model model) {
        model.addAttribute("products", productService.searchStore(query));
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("priceRanges", PRICE_RANGES);
        model.addAttribute("productCount", productService.countOnSale());
        model.addAttribute("stockCount", productService.sumStock());
        model.addAttribute("lowStockCount", productService.countLowStock());
        return "shop/catalog";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Product product = productService.findById(id);
        if (product == null || !"上架".equals(product.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在或已下架");
        }
        model.addAttribute("product", product);
        model.addAttribute("cartCount", cartService.count(session));
        return "shop/detail";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(defaultValue = "false") boolean buyNow,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.add(session, productId, quantity);
            if (buyNow) {
                if (session.getAttribute("authUser") == null) {
                    redirectAttributes.addFlashAttribute("error", "登录后即可结算");
                    return "redirect:/login";
                }
                return "redirect:/checkout";
            }
            redirectAttributes.addFlashAttribute("success", "商品已加入购物车");
            return "redirect:/cart";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        var lines = cartService.lines(session);
        model.addAttribute("lines", lines);
        model.addAttribute("total", cartService.total(lines));
        model.addAttribute("itemCount", lines.stream().mapToInt(line -> line.getQuantity()).sum());
        return "shop/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.update(session, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "购物车数量已更新");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.remove(session, productId);
        redirectAttributes.addFlashAttribute("success", "商品已从购物车移除");
        return "redirect:/cart";
    }

    public static User currentUser(HttpSession session) {
        return (User) session.getAttribute("authUser");
    }
}
