package com.awais.food_app.service.Impl;

import com.awais.food_app.entity.CartEntity;
import com.awais.food_app.io.CartRequest;
import com.awais.food_app.io.CartResponse;
import com.awais.food_app.repository.CartRepository;
import com.awais.food_app.service.CartService;
import com.awais.food_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;

    @Override
    public CartResponse addToCart(CartRequest request) {
        String loggedInUserId = getCurrentUserId();

        // Always fetch the single cart for the user
        CartEntity cartEntity = cartRepository.findByUserId(loggedInUserId)
                .orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));

        Map<String, Integer> items = cartEntity.getItems();
        items.put(request.getFoodId(), items.getOrDefault(request.getFoodId(), 0) + 1);
        cartEntity.setItems(items);

        // Remove any accidental duplicate carts for the user
        cartRepository.findAllByUserId(loggedInUserId).stream()
                .filter(c -> !c.getId().equals(cartEntity.getId()))
                .forEach(cartRepository::delete);

        CartEntity save = cartRepository.save(cartEntity);
        return convertToCartResponse(save);
    }

    @Override
    public CartResponse getCart(String ignored) {
        String loggedInUserId = getCurrentUserId();
        CartEntity cartEntity = cartRepository.findByUserId(loggedInUserId)
                .orElse(new CartEntity(loggedInUserId, new HashMap<>()));

        return convertToCartResponse(cartEntity);
    }

    @Override
    public void clearCart(String ignored) {
        String loggedInUserId = getCurrentUserId();
        cartRepository.deleteByUserId(loggedInUserId);
    }

    @Override
    public CartResponse removeFromCart(CartRequest cartRequest, String ignored) {
        String loggedInUserId = getCurrentUserId();

        CartEntity entity = cartRepository.findByUserId(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Map<String, Integer> cartItems = entity.getItems();
        if (cartItems.containsKey(cartRequest.getFoodId())) {
            Integer currentQty = cartItems.get(cartRequest.getFoodId());
            if (currentQty > 1) {
                cartItems.put(cartRequest.getFoodId(), currentQty - 1);
            } else {
                cartItems.remove(cartRequest.getFoodId());
            }
        }

        CartEntity saveEntity = cartRepository.save(entity);
        return convertToCartResponse(saveEntity);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return auth.getName();
    }

    private CartResponse convertToCartResponse(CartEntity cartEntity) {
        return CartResponse.builder()
                .id(cartEntity.getId())
                .userId(cartEntity.getUserId())
                .items(cartEntity.getItems())
                .build();
    }
}
