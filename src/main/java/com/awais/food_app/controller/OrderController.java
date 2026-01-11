package com.awais.food_app.controller;

import com.awais.food_app.io.OrderRequest;
import com.awais.food_app.io.OrderResponse;
import com.awais.food_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public OrderResponse create(@RequestBody OrderRequest request) {
        return orderService.createOrderWithPayment(request);
    }

    @PostMapping("/verify")
    public void verify(@RequestBody Map<String, String> data) {
        orderService.verifyPayment(data);
    }

    // Paid orders for a user
    @GetMapping
    public List<OrderResponse> paidOrders(@RequestParam String userId) {
        return orderService.getPaidOrders(userId);
    }

    // Pending orders for a user
    @GetMapping("/pending")
    public List<OrderResponse> pendingOrders(@RequestParam String userId) {
        return orderService.getPendingOrders(userId);
    }

    // Retry payment
    @PatchMapping("/retry-payment/{orderId}")
    public OrderResponse retry(@PathVariable String orderId) {
        return orderService.retryPayment(orderId);
    }

    // Admin: all orders
    @GetMapping("/all")
    public List<OrderResponse> allOrders() {
        return orderService.getOrdersAllUsers();
    }

    // Admin: update order status
    @PatchMapping("/status/{orderId}")
    public void updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
    }
}
