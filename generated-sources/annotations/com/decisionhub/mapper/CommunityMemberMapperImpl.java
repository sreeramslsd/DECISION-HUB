package com.decisionhub.mapper;

import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.entity.CommunityMember;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.entity.User;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CommunityMemberMapperImpl implements CommunityMemberMapper {

    @Override
    public CommunityMemberResponse toResponse(CommunityMember member) {
        if ( member == null ) {
            return null;
        }

        UUID userId = null;
        String username = null;
        String avatarUrl = null;
        CommunityRole role = null;
        CommunityStatus status = null;
        Instant joinedAt = null;

        userId = memberUserId( member );
        username = memberUserUsername( member );
        avatarUrl = memberUserAvatarUrl( member );
        role = member.getRole();
        status = member.getStatus();
        joinedAt = member.getJoinedAt();

        CommunityMemberResponse communityMemberResponse = new CommunityMemberResponse( userId, username, avatarUrl, role, status, joinedAt );

        return communityMemberResponse;
    }

    private UUID memberUserId(CommunityMember communityMember) {
        User user = communityMember.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private String memberUserUsername(CommunityMember communityMember) {
        User user = communityMember.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUsername();
    }

    private String memberUserAvatarUrl(CommunityMember communityMember) {
        User user = communityMember.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getAvatarUrl();
    }
}
