package com.decisionhub.mapper;

import com.decisionhub.dto.PollRequest;
import com.decisionhub.dto.PollResponse;
import com.decisionhub.entity.Poll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PollMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    PollResponse toResponse(Poll poll);

    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Poll toEntity(PollRequest request);
}
