package com.decisionhub.mapper;

import com.decisionhub.dto.VoteRequest;
import com.decisionhub.dto.VoteResponse;
import com.decisionhub.entity.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VoteMapper {

    @Mapping(target = "pollId", source = "poll.id")
    @Mapping(target = "optionId", source = "option.id")
    @Mapping(target = "userId", source = "user.id")
    VoteResponse toResponse(Vote vote);

    @Mapping(target = "poll", ignore = true)
    @Mapping(target = "option", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Vote toEntity(VoteRequest request);
}
