package com.awais.food_app.controller;

import com.awais.food_app.io.CartRequest;
import com.awais.food_app.io.CartResponse;
import com.awais.food_app.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public CartResponse addToCart(@RequestBody CartRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

        if (request.getFoodId() == null || request.getFoodId().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid foodId");

        return cartService.addToCart(request);
    }

    @GetMapping
    public CartResponse getCart(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

        return cartService.getCart(null);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

        cartService.clearCart(null);
    }

    @PostMapping("/remove")
    public CartResponse removeFromCart(@RequestBody CartRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

        if (request.getFoodId() == null || request.getFoodId().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid foodId");

        return cartService.removeFromCart(request, null);
    }
}
