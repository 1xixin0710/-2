package com.dlut.library.controller;

import com.dlut.library.model.CartLine;
import com.dlut.library.model.CheckoutForm;
import com.dlut.library.model.Order;
import com.dlut.library.model.User;
import com.dlut.library.service.CartService;
import com.dlut.library.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        List<CartLine> lines = cartService.lines(session);
        if (lines.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "购物车为空，请先选择商品");
            return "redirect:/cart";
        }
        if (!model.containsAttribute("checkoutForm")) {
            User user = ShopController.currentUser(session);
            CheckoutForm form = new CheckoutForm();
            form.setRecipient(user.getDisplayName());
            form.setDeliveryType("校园配送");
            form.setAddress("大学生活动中心一层云序书集");
            model.addAttribute("checkoutForm", form);
        }
        addCart(model, lines);
        return "shop/checkout";
    }

    @PostMapping("/checkout")
    public String submit(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        List<CartLine> lines = cartService.lines(session);
        if (lines.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "购物车为空，无法提交订单");
            return "redirect:/cart";
        }
        if (bindingResult.hasErrors()) {
            addCart(model, lines);
            return "shop/checkout";
        }
        User user = ShopController.currentUser(session);
        try {
            Order order = orderService.create(user, form, lines);
            cartService.clear(session);
            redirectAttributes.addFlashAttribute("success",
                    "订单提交成功，货到付款，订单号 " + order.getOrderNo());
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("checkout", exception.getMessage());
            addCart(model, lines);
            return "shop/checkout";
        }
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User user = ShopController.currentUser(session);
        model.addAttribute("orders", orderService.findByCustomer(user.getUsername()));
        return "shop/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        User user = ShopController.currentUser(session);
        try {
            model.addAttribute("order", orderService.findForCustomer(id, user.getUsername()));
            return "shop/order-detail";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/orders";
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = ShopController.currentUser(session);
        try {
            orderService.cancelByCustomer(id, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "订单已取消，库存已恢复");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/receive")
    public String receive(@PathVariable Long id, HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = ShopController.currentUser(session);
        try {
            orderService.confirmReceipt(id, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "已确认收货，感谢购买");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    private void addCart(Model model, List<CartLine> lines) {
        model.addAttribute("lines", lines);
        model.addAttribute("total", cartService.total(lines));
        model.addAttribute("deliveryTypes", List.of("校园配送", "到店自提"));
        model.addAttribute("paymentMethod", "货到付款");
    }
}
