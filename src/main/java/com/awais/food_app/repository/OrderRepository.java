package com.awais.food_app.repository;

import com.awais.food_app.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {

    // All orders of a user
    List<OrderEntity> findByUserId(String userId);

    // Pending orders of a user
    List<OrderEntity> findByUserIdAndPaymentStatus(String userId, String paymentStatus);

    // Find order by Stripe session / payment intent id
    Optional<OrderEntity> findByStripePaymentIntentId(String stripePaymentIntentId);

}
