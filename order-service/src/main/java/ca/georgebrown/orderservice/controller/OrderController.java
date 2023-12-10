package ca.georgebrown.orderservice.controller;

import ca.georgebrown.orderservice.dto.OrderRequest;
import ca.georgebrown.orderservice.service.OrderServiceImpl;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
@TimeLimiter(name = "inventory")
@Retry(name = "inventory")
public class OrderController {

    private final OrderServiceImpl orderService;
    private final WebClient.Builder webClientBuilder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "placeOrderFallback")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request) {
        orderService.placeOrder(request);
        //return "Order Placed Successfully";
        return CompletableFuture.supplyAsync(() ->  orderService.placeOrder(request));
    }

    public CompletableFuture<String> placeOrderFallback(OrderRequest request, RuntimeException e) {
        log.error("Exception is: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> "Insufficient stock, order not placed.");
    }
}
