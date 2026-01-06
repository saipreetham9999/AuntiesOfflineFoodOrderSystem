package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Promotion;
import com.auntieescafe.auntieesfoodordermanagement.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final String UPLOAD_DIR = "uploads/promotions/";

    public List<Promotion> getAllActivePromotions() {
        return promotionRepository.findByIsActiveTrue();
    }

    public Promotion createPromotion(String title, String description, MultipartFile image) throws IOException {
        Promotion promotion = new Promotion();
        promotion.setTitle(title);
        promotion.setDescription(description);

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());
            promotion.setImageUrl("/images/promotions/" + fileName);
        }

        return promotionRepository.save(promotion);
    }
}
