package com.auntieescafe.auntieesfoodordermanagement.mapper;

import com.auntieescafe.auntieesfoodordermanagement.entity.Promotion;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.PromotionRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.PromotionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromotionMapper {

    public PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getTitle(),
                promotion.getDescription(),
                promotion.getImageUrl(),
                promotion.isActive()
        );
    }

    public List<PromotionResponse> toPromotionResponseList(List<Promotion> promotions) {
        return promotions.stream()
                .map(this::toPromotionResponse)
                .collect(Collectors.toList());
    }

    public Promotion toPromotion(PromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setActive(request.isActive());
        return promotion;
    }

    public void updatePromotionFromRequest(PromotionRequest request, Promotion promotion) {
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setActive(request.isActive());
    }
}
