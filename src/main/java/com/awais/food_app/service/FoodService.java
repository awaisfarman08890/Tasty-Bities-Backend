package com.awais.food_app.service;

import com.awais.food_app.io.FoodRequest;
import com.awais.food_app.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {
    String uploadFile(MultipartFile file);
    FoodResponse addFood(FoodRequest request, MultipartFile file);
    List<FoodResponse> getAllFoods();
    FoodResponse getFoodById(String id);
    boolean deleteFile(String filename);
    void deleteFood(String id);
}
