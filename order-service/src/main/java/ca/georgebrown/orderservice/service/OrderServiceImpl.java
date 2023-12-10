package ca.georgebrown.orderservice.service;

import ca.georgebrown.orderservice.dto.InventoryRequest;
import ca.georgebrown.orderservice.dto.InventoryResponse;
import ca.georgebrown.orderservice.dto.OrderLineItemDto;
import ca.georgebrown.orderservice.dto.OrderRequest;
import ca.georgebrown.orderservice.model.Order;
import ca.georgebrown.orderservice.model.OrderLineItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ca.georgebrown.orderservice.repository.OrderRepository;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Value("${inventory.service.url}")
    private String inventoryApiUrl;

    @Override
    public String placeOrder(OrderRequest orderRequest) {

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> orderLineItems = orderRequest
                .getOrderLineItems()
                .stream()
                .map(this::mapToModel)
                .toList();

        order.setOrderLineItem(orderLineItems);

        List<InventoryRequest> inventoryRequests = orderLineItems
                .stream()
                .map(lineItem -> new InventoryRequest(lineItem.getSkuCode(), lineItem.getQuantity()))
                .collect(Collectors.toList());

        List<InventoryResponse> inventoryResponseList = webClient
                .post()
                .uri(inventoryApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequests)
                .retrieve()
                .bodyToFlux(InventoryResponse.class)
                .collectList()
                .block();

        assert inventoryResponseList != null;

        boolean allProductsInStock = inventoryResponseList
                .stream()
                .allMatch(InventoryResponse::isSufficientStock);
        if (allProductsInStock) {
            orderRepository.save(order);
            return "Order Placed Successfully";
        } else {
            throw new RuntimeException("Insufficient stock, order not placed.");
        }
    }

    private OrderLineItem mapToModel(OrderLineItemDto orderLineItemDto) {
        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setPrice(orderLineItemDto.getPrice());
        orderLineItem.setSkuCode(orderLineItemDto.getSkuCode());
        orderLineItem.setQuantity(orderLineItemDto.getQuantity());
        return orderLineItem;
    }
}
