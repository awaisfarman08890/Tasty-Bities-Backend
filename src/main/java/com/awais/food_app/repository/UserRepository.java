package com.awais.food_app.repository;

import com.awais.food_app.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository  extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

}
