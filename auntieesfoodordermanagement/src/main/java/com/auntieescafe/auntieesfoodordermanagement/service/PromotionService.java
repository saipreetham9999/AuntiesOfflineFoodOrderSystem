package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Promotion;
import com.auntieescafe.auntieesfoodordermanagement.mapper.PromotionMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.PromotionRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public List<Promotion> getAllActivePromotions() {
        return promotionRepository.findByIsActiveTrue();
    }

    public Optional<Promotion> getPromotionById(UUID id) {
        return promotionRepository.findById(id);
    }

    @Transactional
    public Promotion createPromotion(PromotionRequest request, MultipartFile image) throws IOException {
        Promotion promotion = promotionMapper.toPromotion(request);

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path path = Paths.get(uploadDir + "/promotions/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());
            promotion.setImageUrl("/images/promotions/" + fileName);
        }

        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion updatePromotion(UUID id, PromotionRequest request, MultipartFile image) throws IOException {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        promotionMapper.updatePromotionFromRequest(request, promotion);

        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (promotion.getImageUrl() != null && !promotion.getImageUrl().isEmpty()) {
                try {
                    Path oldImagePath = Paths.get(uploadDir + promotion.getImageUrl().replace("/images", ""));
                    Files.deleteIfExists(oldImagePath);
                } catch (IOException e) {
                    // Log error but don't fail the update
                    System.err.println("Could not delete old image: " + e.getMessage());
                }
            }

            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path path = Paths.get(uploadDir + "/promotions/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());
            promotion.setImageUrl("/images/promotions/" + fileName);
        }

        return promotionRepository.save(promotion);
    }

    @Transactional
    public void deletePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        // Delete image file
        if (promotion.getImageUrl() != null && !promotion.getImageUrl().isEmpty()) {
            try {
                Path imagePath = Paths.get(uploadDir + promotion.getImageUrl().replace("/images", ""));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Could not delete image file for promotion " + id + ": " + e.getMessage());
            }
        }
        promotionRepository.delete(promotion);
    }
}
