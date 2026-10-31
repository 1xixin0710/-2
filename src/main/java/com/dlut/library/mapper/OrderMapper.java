package com.dlut.library.mapper;

import com.dlut.library.model.Order;
import com.dlut.library.model.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderMapper {

    int insert(Order order);

    int insertItem(OrderItem item);

    Order findById(Long id);

    List<Order> findByUsername(String username);

    List<Order> findAll(@Param("status") String status);

    List<Order> findRecent(@Param("limit") int limit);

    List<OrderItem> findItems(Long orderId);

    int updateStatus(@Param("id") Long id,
                     @Param("currentStatus") String currentStatus,
                     @Param("newStatus") String newStatus);

    int updateCustomerStatus(@Param("id") Long id,
                             @Param("username") String username,
                             @Param("currentStatus") String currentStatus,
                             @Param("newStatus") String newStatus);

    int countAll();

    int countByStatus(String status);

    BigDecimal sumRevenue();
}
