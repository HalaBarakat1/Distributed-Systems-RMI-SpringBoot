package org.ds.orderservice.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.ds.orderservice.dto.CreateOrderRequest;
import org.ds.orderservice.dto.OrderResponse;
import org.ds.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for clients. Only handles HTTP: the work is done in OrderService.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + created.orderId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
}
