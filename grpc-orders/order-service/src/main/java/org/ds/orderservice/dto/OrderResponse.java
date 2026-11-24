package org.ds.orderservice.dto;

import org.ds.orderservice.entity.Order;

/**
 * What the client receives after creating or reading an order.
 */
public record OrderResponse(
        Long orderId,
        Long userId,
        String userName,
        String productName,
        Integer quantity,
        String status) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getUserName(),
                order.getProductName(),
                order.getQuantity(),
                order.getStatus().name());
    }
}
