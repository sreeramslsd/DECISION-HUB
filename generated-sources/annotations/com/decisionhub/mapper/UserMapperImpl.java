package com.decisionhub.mapper;

import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.dto.UserUpdateRequest;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.RoleName;
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
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        String roleName = null;
        UUID id = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String status = null;
        String avatarUrl = null;
        Instant createdAt = null;

        RoleName name = userRoleName( user );
        if ( name != null ) {
            roleName = name.name();
        }
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        if ( user.getStatus() != null ) {
            status = user.getStatus().name();
        }
        avatarUrl = user.getAvatarUrl();
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, username, email, firstName, lastName, roleName, status, avatarUrl, createdAt );

        return userResponse;
    }

    @Override
    public UserSummaryDto toSummary(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String username = null;
        String avatarUrl = null;

        id = user.getId();
        username = user.getUsername();
        avatarUrl = user.getAvatarUrl();

        UserSummaryDto userSummaryDto = new UserSummaryDto( id, username, avatarUrl );

        return userSummaryDto;
    }

    @Override
    public User toEntity(UserRegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( request.username() );
        user.setEmail( request.email() );
        user.setFirstName( request.firstName() );
        user.setLastName( request.lastName() );

        return user;
    }

    @Override
    public void updateEntity(UserUpdateRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.email() != null ) {
            user.setEmail( request.email() );
        }
        if ( request.firstName() != null ) {
            user.setFirstName( request.firstName() );
        }
        if ( request.lastName() != null ) {
            user.setLastName( request.lastName() );
        }
        if ( request.avatarUrl() != null ) {
            user.setAvatarUrl( request.avatarUrl() );
        }
    }

    private RoleName userRoleName(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getName();
    }
}
