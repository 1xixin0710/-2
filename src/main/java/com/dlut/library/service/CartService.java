package com.dlut.library.service;

import com.dlut.library.mapper.ProductMapper;
import com.dlut.library.model.CartLine;
import com.dlut.library.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_KEY = "shopCart";

    private final ProductMapper productMapper;

    public CartService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public void add(HttpSession session, Long productId, int quantity) {
        Product product = requireAvailable(productId);
        int amount = Math.max(1, Math.min(quantity, 99));
        Map<Long, Integer> cart = cart(session);
        int next = cart.getOrDefault(productId, 0) + amount;
        if (next > product.getStock()) {
            throw new IllegalStateException("加入数量超过当前库存");
        }
        cart.put(productId, next);
        syncCount(session);
    }

    public void update(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = cart(session);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            Product product = requireAvailable(productId);
            cart.put(productId, Math.min(quantity, Math.min(99, product.getStock())));
        }
        syncCount(session);
    }

    public void remove(HttpSession session, Long productId) {
        cart(session).remove(productId);
        syncCount(session);
    }

    public void clear(HttpSession session) {
        cart(session).clear();
        syncCount(session);
    }

    public Map<Long, Integer> quantities(HttpSession session) {
        return new LinkedHashMap<>(cart(session));
    }

    public List<CartLine> lines(HttpSession session) {
        List<CartLine> lines = new ArrayList<>();
        cart(session).forEach((productId, quantity) -> {
            Product product = productMapper.findById(productId);
            if (product != null && "上架".equals(product.getStatus()) && product.getStock() > 0) {
                lines.add(new CartLine(product, Math.min(quantity, product.getStock())));
            }
        });
        return lines;
    }

    public BigDecimal total(List<CartLine> lines) {
        return lines.stream()
                .map(CartLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int count(HttpSession session) {
        return cart(session).values().stream().mapToInt(Integer::intValue).sum();
    }

    private Product requireAvailable(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null || !product.isAvailable()) {
            throw new IllegalStateException("商品已下架或暂时缺货");
        }
        return product;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> cart(HttpSession session) {
        Object existing = session.getAttribute(CART_KEY);
        if (existing instanceof Map<?, ?>) {
            return (Map<Long, Integer>) existing;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(CART_KEY, cart);
        return cart;
    }

    private void syncCount(HttpSession session) {
        session.setAttribute("cartCount", count(session));
    }
}
