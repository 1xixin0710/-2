package com.dlut.library.service;

import com.dlut.library.mapper.OrderMapper;
import com.dlut.library.mapper.ProductMapper;
import com.dlut.library.model.CartLine;
import com.dlut.library.model.CheckoutForm;
import com.dlut.library.model.Order;
import com.dlut.library.model.OrderItem;
import com.dlut.library.model.Product;
import com.dlut.library.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @Transactional
    public Order create(User user, CheckoutForm form, List<CartLine> lines) {
        if (lines.isEmpty()) {
            throw new IllegalStateException("购物车为空，无法提交订单");
        }
        for (CartLine line : lines) {
            Product product = productMapper.findById(line.getProduct().getId());
            if (product == null || !product.isAvailable()
                    || product.getStock() < line.getQuantity()) {
                throw new IllegalStateException("商品“" + line.getProduct().getName()
                        + "”库存不足，请返回购物车调整数量");
            }
            if (productMapper.decrementStock(product.getId(), line.getQuantity()) != 1) {
                throw new IllegalStateException("商品库存发生变化，请重新提交订单");
            }
        }

        BigDecimal total = lines.stream()
                .map(CartLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        Order order = new Order();
        order.setOrderNo(nextOrderNo());
        order.setUsername(user.getUsername());
        order.setCustomerName(user.getDisplayName());
        order.setRecipient(form.getRecipient().trim());
        order.setPhone(form.getPhone().trim());
        order.setDeliveryType(form.getDeliveryType());
        order.setAddress(form.getAddress().trim());
        order.setTotalAmount(total);
        order.setStatus("待发货");
        order.setRemark(form.getRemark() == null ? "" : form.getRemark().trim());
        orderMapper.insert(order);

        for (CartLine line : lines) {
            Product product = line.getProduct();
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setCoverUrl(product.getCoverUrl());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(line.getQuantity());
            item.setSubtotal(line.getSubtotal());
            orderMapper.insertItem(item);
            order.getItems().add(item);
        }
        return order;
    }

    public List<Order> findByCustomer(String username) {
        List<Order> orders = orderMapper.findByUsername(username);
        orders.forEach(order -> order.setItems(orderMapper.findItems(order.getId())));
        return orders;
    }

    public Order findForCustomer(Long id, String username) {
        Order order = requireOrder(id);
        if (!username.equals(order.getUsername())) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setItems(orderMapper.findItems(order.getId()));
        return order;
    }

    public List<Order> findAll(String status) {
        List<Order> orders = orderMapper.findAll(normalizeStatus(status));
        orders.forEach(order -> order.setItems(orderMapper.findItems(order.getId())));
        return orders;
    }

    public Order findForAdmin(Long id) {
        Order order = requireOrder(id);
        order.setItems(orderMapper.findItems(order.getId()));
        return order;
    }

    public List<Order> findRecent(int limit) {
        List<Order> orders = orderMapper.findRecent(limit);
        orders.forEach(order -> order.setItems(orderMapper.findItems(order.getId())));
        return orders;
    }

    @Transactional
    public void cancelByCustomer(Long id, String username) {
        Order order = findForCustomer(id, username);
        if (orderMapper.updateCustomerStatus(id, username, "待发货", "已取消") != 1) {
            throw new IllegalStateException("当前订单已进入配送流程，不能取消");
        }
        restoreStock(order);
    }

    @Transactional
    public void confirmReceipt(Long id, String username) {
        if (orderMapper.updateCustomerStatus(id, username, "待收货", "已完成") != 1) {
            throw new IllegalStateException("当前订单不能确认收货");
        }
    }

    @Transactional
    public void updateStatusByAdmin(Long id, String newStatus) {
        Order order = requireOrder(id);
        String current = order.getStatus();
        boolean allowed = switch (current) {
            case "待发货" -> "待收货".equals(newStatus) || "已取消".equals(newStatus);
            case "待收货" -> "已完成".equals(newStatus) || "已取消".equals(newStatus);
            default -> false;
        };
        if (!allowed || orderMapper.updateStatus(id, current, newStatus) != 1) {
            throw new IllegalStateException("订单状态不能按所选方式变更");
        }
        if ("已取消".equals(newStatus)) {
            order.setItems(orderMapper.findItems(id));
            restoreStock(order);
        }
    }

    public int countAll() {
        return orderMapper.countAll();
    }

    public int countByStatus(String status) {
        return orderMapper.countByStatus(status);
    }

    public BigDecimal sumRevenue() {
        return orderMapper.sumRevenue();
    }

    public List<String> statuses() {
        return List.of("待发货", "待收货", "已完成", "已取消");
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productMapper.incrementStock(item.getProductId(), item.getQuantity());
            productMapper.decrementSold(item.getProductId(), item.getQuantity());
        }
    }

    private Order requireOrder(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private String nextOrderNo() {
        return "YX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim();
    }
}
