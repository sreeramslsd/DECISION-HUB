package com.decisionhub.mapper;

import com.decisionhub.dto.CommentRequest;
import com.decisionhub.dto.CommentResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.Comment;
import com.decisionhub.entity.CommentStatus;
import com.decisionhub.entity.DecisionBoard;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:09+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public CommentResponse toResponse(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        UUID decisionId = null;
        UserSummaryDto author = null;
        UUID parentCommentId = null;
        UUID id = null;
        String content = null;
        CommentStatus status = null;
        Instant createdAt = null;

        decisionId = commentDecisionId( comment );
        author = userMapper.toSummary( comment.getUser() );
        parentCommentId = commentParentCommentId( comment );
        id = comment.getId();
        content = comment.getContent();
        status = comment.getStatus();
        createdAt = comment.getCreatedAt();

        CommentResponse commentResponse = new CommentResponse( id, decisionId, author, parentCommentId, content, status, createdAt );

        return commentResponse;
    }

    @Override
    public Comment toEntity(CommentRequest request) {
        if ( request == null ) {
            return null;
        }

        Comment comment = new Comment();

        comment.setContent( request.content() );

        return comment;
    }

    private UUID commentDecisionId(Comment comment) {
        DecisionBoard decision = comment.getDecision();
        if ( decision == null ) {
            return null;
        }
        return decision.getId();
    }

    private UUID commentParentCommentId(Comment comment) {
        Comment parentComment = comment.getParentComment();
        if ( parentComment == null ) {
            return null;
        }
        return parentComment.getId();
    }
}
