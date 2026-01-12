package com.awais.food_app.service.Impl;

import com.awais.food_app.entity.OrderEntity;
import com.awais.food_app.io.OrderRequest;
import com.awais.food_app.io.OrderResponse;
import com.awais.food_app.repository.CartRepository;
import com.awais.food_app.repository.OrderRepository;
import com.awais.food_app.service.OrderService;
import com.awais.food_app.service.UserService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartRepository cartRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) {
        // 1️⃣ Save order first with PENDING payment
        OrderEntity order = OrderEntity.builder()
                .userId(userService.findByUserId(request.getEmail()))
                .amount(request.getAmount())
                .orderItems(request.getOrderedItems())
                .orderStatus("CREATED")
                .paymentStatus("PENDING")
                .userAddress(request.getUserAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        // 2️⃣ Create Stripe session
        return createStripeSession(savedOrder);
    }

    private OrderResponse createStripeSession(OrderEntity order) {
        Stripe.apiKey = stripeSecretKey;

        try {
            List<SessionCreateParams.LineItem> lineItems =
                    order.getOrderItems().stream().map(item ->
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity((long) item.getQuantity())
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount((long) (item.getPrice() * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(item.getName())
                                                                    .build()
                                                    ).build()
                                    ).build()
                    ).toList();

            Session session = Session.create(
                    SessionCreateParams.builder()
                            .addAllLineItem(lineItems)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("https://tasty-bites-frontend-sable.vercel.app/checkout-success?success=true&session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl("https://tasty-bites-frontend-sable.vercel.app/cart?canceled=true")
                            .build()
            );

            // Save Stripe session id to DB
            order.setStripePaymentIntentId(session.getId());
            orderRepository.save(order);

            // Return full response to frontend
            return OrderResponse.builder()
                    .id(order.getId())
                    .amount(order.getAmount())
                    .orderItems(order.getOrderItems())
                    .orderStatus(order.getOrderStatus())
                    .paymentStatus(order.getPaymentStatus())
                    .stripePaymentIntentId(order.getStripePaymentIntentId())
                    .userAddress(order.getUserAddress())
                    .phoneNumber(order.getPhoneNumber())
                    .email(order.getEmail())
                    .clientSecret(session.getUrl())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Stripe session creation failed", e);
        }
    }

    @Override
    public void verifyPayment(Map<String, String> paymentData) {
        String sessionId = paymentData.get("stripePaymentIntentId");
        OrderEntity order = orderRepository
                .findByStripePaymentIntentId(sessionId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus("PAID");
        order.setOrderStatus("CONFIRMED");

        orderRepository.save(order);
        cartRepository.deleteByUserId(order.getUserId());
    }

    @Override
    public List<OrderResponse> getPaidOrders(String userId) {
        String dbUserId = userService.findByUserId(userId);
        return orderRepository
                .findByUserIdAndPaymentStatus(dbUserId, "PAID")
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getPendingOrders(String userId) {
        String dbUserId = userService.findByUserId(userId);
        return orderRepository
                .findByUserIdAndPaymentStatus(dbUserId, "PENDING")
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public OrderResponse retryPayment(String orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus("PENDING");
        orderRepository.save(order);

        return createStripeSession(order);
    }

    @Override
    public void removeOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderResponse> getOrdersAllUsers() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse convertToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .orderItems(order.getOrderItems())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .stripePaymentIntentId(order.getStripePaymentIntentId())
                .userAddress(order.getUserAddress())
                .phoneNumber(order.getPhoneNumber())
                .email(order.getEmail())
                .clientSecret(null)
                .build();
    }
}
