package com.decisionhub.mapper;

import com.decisionhub.dto.CategoryResponse;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.Community;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CommunityMapperImpl implements CommunityMapper {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public CommunityResponse toResponse(Community community) {
        if ( community == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String description = null;
        String slug = null;
        CategoryResponse category = null;
        UserSummaryDto creator = null;
        Instant createdAt = null;

        id = community.getId();
        name = community.getName();
        description = community.getDescription();
        slug = community.getSlug();
        category = categoryMapper.toResponse( community.getCategory() );
        creator = userMapper.toSummary( community.getCreator() );
        createdAt = community.getCreatedAt();

        CommunityResponse communityResponse = new CommunityResponse( id, name, description, slug, category, creator, createdAt );

        return communityResponse;
    }

    @Override
    public Community toEntity(CommunityRequest request) {
        if ( request == null ) {
            return null;
        }

        Community community = new Community();

        community.setName( request.name() );
        community.setDescription( request.description() );
        community.setSlug( request.slug() );

        return community;
    }

    @Override
    public void updateEntity(CommunityRequest request, Community community) {
        if ( request == null ) {
            return;
        }

        if ( request.name() != null ) {
            community.setName( request.name() );
        }
        if ( request.description() != null ) {
            community.setDescription( request.description() );
        }
        if ( request.slug() != null ) {
            community.setSlug( request.slug() );
        }
    }
}
