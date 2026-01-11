package com.awais.food_app.repository;

import com.awais.food_app.entity.FoodEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FoodRepository extends MongoRepository<FoodEntity, String> {
}
