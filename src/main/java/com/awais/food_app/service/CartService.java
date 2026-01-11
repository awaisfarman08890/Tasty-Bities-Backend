package com.awais.food_app.service;

import com.awais.food_app.io.CartRequest;
import com.awais.food_app.io.CartResponse;

public interface CartService {
    CartResponse addToCart(CartRequest request);
    CartResponse getCart(String foodId);
    void clearCart(String foodId);
    CartResponse removeFromCart(CartRequest cartRequest, String foodId);
}
