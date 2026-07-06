package com.decisionhub.mapper;

import com.decisionhub.dto.CommentRequest;
import com.decisionhub.dto.CommentResponse;
import com.decisionhub.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommentMapper {

    @Mapping(target = "decisionId", source = "decision.id")
    @Mapping(target = "author", source = "user")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Comment toEntity(CommentRequest request);
}
