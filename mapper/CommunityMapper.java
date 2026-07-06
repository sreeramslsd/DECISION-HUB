package com.decisionhub.mapper;

import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.entity.Community;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {CategoryMapper.class, UserMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommunityMapper {

    CommunityResponse toResponse(Community community);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Community toEntity(CommunityRequest request);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(CommunityRequest request, @MappingTarget Community community);
}
