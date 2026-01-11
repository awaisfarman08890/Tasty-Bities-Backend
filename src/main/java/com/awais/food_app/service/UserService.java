package com.awais.food_app.service;

import com.awais.food_app.io.UserRequest;
import com.awais.food_app.io.UserResponse;

import java.util.Map;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);
    String findByUserId(String email);
}
