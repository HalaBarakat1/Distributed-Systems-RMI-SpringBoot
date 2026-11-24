package org.ds.orderservice.service;

import java.util.List;

import org.ds.orderservice.client.UserGrpcClient;
import org.ds.orderservice.client.UserInfo;
import org.ds.orderservice.dto.CreateOrderRequest;
import org.ds.orderservice.dto.OrderResponse;
import org.ds.orderservice.entity.Order;
import org.ds.orderservice.exception.OrderNotFoundException;
import org.ds.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic: an order is saved only after user-service confirms the user exists.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserGrpcClient userClient;

    public OrderService(OrderRepository orderRepository, UserGrpcClient userClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        // Throws UserNotFoundException (404) or UserServiceUnavailableException (503).
        // Either way we stop here, so nothing is saved.
        UserInfo user = userClient.getUser(request.userId());

        Order order = new Order(user.id(), user.name(), request.productName(), request.quantity());
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
