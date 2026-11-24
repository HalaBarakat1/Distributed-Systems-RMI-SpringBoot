package org.ds.orderservice.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(long orderId) {
        super("Order " + orderId + " not found");
    }
}
