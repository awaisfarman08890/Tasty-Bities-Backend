package com.awais.food_app.service.Impl;

import com.awais.food_app.entity.FoodEntity;
import com.awais.food_app.io.FoodRequest;
import com.awais.food_app.io.FoodResponse;
import com.awais.food_app.repository.FoodRepository;
import com.awais.food_app.service.FoodService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class FoodServiceImpl implements FoodService {
    @Autowired
    private FoodRepository foodRepository;
    @Value("${aws.s3.bucketname}")
    private String bucketName;
    @Autowired
    private S3Client s3Client;
    @Override
    public String uploadFile(MultipartFile file) {
        String fileNameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID().toString()+"."+fileNameExtension;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            if(response.sdkHttpResponse().isSuccessful()){
                return "http://"+bucketName+".s3.amazonaws.com/"+key;
            }else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
            }

        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while uploading the file");

        }

    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToEntity(request);
        String imageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        newFoodEntity = foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);

    }

    @Override
    public List<FoodResponse> getAllFoods() {
        List<FoodEntity> databaseEntries = foodRepository.findAll();
        return  databaseEntries.stream().map(
                this::convertToResponse
        ).toList();

    }

    @Override
    public FoodResponse getFoodById(String id) {
        FoodEntity foodNotFound = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found for the id: "+ id));
        return convertToResponse(foodNotFound);
    }

    @Override
    public boolean deleteFile(String filename) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        return true;
    }

    @Override
    public void deleteFood(String id) {
        FoodResponse response = getFoodById(id);
        String imageUrl = response.getImageUrl();
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        boolean deleted = deleteFile(filename);
        if(deleted) {
            foodRepository.deleteById(id);
        }else {
            throw new RuntimeException("Food not found for the id: "+ id);
        }


    }


    private FoodEntity convertToEntity(FoodRequest foodRequest) {
        return FoodEntity.builder()
                .name(foodRequest.getName())
                .description(foodRequest.getDescription())
                .price(foodRequest.getPrice())
                .category(foodRequest.getCategory())
                .build();
    }
    private FoodResponse convertToResponse(FoodEntity foodEntity) {
        return FoodResponse.builder()
                .id(foodEntity.getId())
                .name(foodEntity.getName())
                .description(foodEntity.getDescription())
                .price(foodEntity.getPrice())
                .category(foodEntity.getCategory())
                .imageUrl(foodEntity.getImageUrl())
                .build();
    }
}
