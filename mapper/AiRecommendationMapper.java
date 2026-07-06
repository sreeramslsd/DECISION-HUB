package com.decisionhub.mapper;

import com.decisionhub.dto.AiRecommendationResponse;
import com.decisionhub.entity.AIRecommendation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AiRecommendationMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    AiRecommendationResponse toResponse(AIRecommendation recommendation);
}
