package com.awais.food_app.service.Impl;

import com.awais.food_app.entity.CartEntity;
import com.awais.food_app.io.CartRequest;
import com.awais.food_app.io.CartResponse;
import com.awais.food_app.repository.CartRepository;
import com.awais.food_app.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    public CartResponse addToCart(CartRequest request) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElse(
                        CartEntity.builder()
                                .userId(userId)
                                .items(new HashMap<>())
                                .build()
                );

        Map<String, Integer> items = cart.getItems();
        items.put(request.getFoodId(), items.getOrDefault(request.getFoodId(), 0) + 1);

        cart.setItems(items);
        CartEntity saved = cartRepository.save(cart);

        return toResponse(saved);
    }

    @Override
    public CartResponse getCart() {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElse(
                        CartEntity.builder()
                                .userId(userId)
                                .items(new HashMap<>())
                                .build()
                );

        return toResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(CartRequest request) {
        String userId = getCurrentUserId();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Map<String, Integer> items = cart.getItems();

        if (items.containsKey(request.getFoodId())) {
            int qty = items.get(request.getFoodId());
            if (qty > 1) {
                items.put(request.getFoodId(), qty - 1);
            } else {
                items.remove(request.getFoodId());
            }
        }

        CartEntity saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    @Override
    public void clearCart() {
        String userId = getCurrentUserId();
        cartRepository.deleteByUserId(userId);
    }

    // ================== HELPERS ==================

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return auth.getName(); // email
    }

    private CartResponse toResponse(CartEntity entity) {
        return CartResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .items(entity.getItems())
                .build();
    }
}
