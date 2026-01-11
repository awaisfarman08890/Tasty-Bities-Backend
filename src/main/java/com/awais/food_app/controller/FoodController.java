package com.awais.food_app.controller;

import com.awais.food_app.io.FoodRequest;
import com.awais.food_app.io.FoodResponse;
import com.awais.food_app.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/foods")
@CrossOrigin("*")
public class FoodController {

    private final FoodService foodService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public FoodResponse addFood(
            @RequestPart("food") String foodString,
            @RequestPart("file") MultipartFile file) {

        FoodRequest request;

        try {
            request = objectMapper.readValue(foodString, FoodRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid JSON format for food"
            );
        }

        return foodService.addFood(request, file);
    }
    @GetMapping
    public List<FoodResponse> getAllFoods() {
        return foodService.getAllFoods();
    }
    @GetMapping("/{id}")
    public FoodResponse getFoodById(@PathVariable("id") String id) {
        return foodService.getFoodById(id);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFoodById(@PathVariable("id") String id) {
          foodService.deleteFood(id);

    }
}

