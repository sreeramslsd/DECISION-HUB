package com.decisionhub.mapper;

import com.decisionhub.dto.InvitationRequest;
import com.decisionhub.dto.InvitationResponse;
import com.decisionhub.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InvitationMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    @Mapping(target = "communityId", source = "community.id")
    InvitationResponse toResponse(Invitation invitation);

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Invitation toEntity(InvitationRequest request);
}
