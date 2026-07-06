package com.decisionhub.mapper;

import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.entity.CommunityMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting CommunityMember entity instances to CommunityMemberResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface CommunityMemberMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    CommunityMemberResponse toResponse(CommunityMember member);
}
