package ca.georgebrown.orderservice.service;

import ca.georgebrown.orderservice.dto.OrderRequest;

public interface OrderService {
    String placeOrder(OrderRequest orderRequest);
}
