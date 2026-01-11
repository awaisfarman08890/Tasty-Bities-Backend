package com.awais.food_app.service;

import com.awais.food_app.io.OrderRequest;
import com.awais.food_app.io.OrderResponse;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrderWithPayment(OrderRequest request);

    void verifyPayment(Map<String, String> paymentData);

    List<OrderResponse> getPaidOrders(String userId);

    List<OrderResponse> getPendingOrders(String userId);

    OrderResponse retryPayment(String orderId);

    void removeOrder(String orderId);

    List<OrderResponse> getOrdersAllUsers();

    void updateOrderStatus(String orderId, String status);
}
