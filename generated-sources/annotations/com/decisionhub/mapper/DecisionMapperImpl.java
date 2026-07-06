package com.decisionhub.mapper;

import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.dto.CriteriaDto;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionSummaryDto;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.dto.PollResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.Category;
import com.decisionhub.entity.Community;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.ComparisonScore;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.OptionCriteria;
import com.decisionhub.entity.Poll;
import com.decisionhub.entity.VotingType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class DecisionMapperImpl implements DecisionMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PollMapper pollMapper;
    @Autowired
    private ComparisonMapper comparisonMapper;

    @Override
    public DecisionResponse toResponse(DecisionBoard decisionBoard) {
        if ( decisionBoard == null ) {
            return null;
        }

        String categoryName = null;
        String communityName = null;
        Set<String> tags = null;
        List<ComparisonFactorResponse> factors = null;
        UUID id = null;
        String title = null;
        String description = null;
        UserSummaryDto creator = null;
        VotingType votingType = null;
        AnonymityType anonymityType = null;
        DecisionStatus status = null;
        Instant deadline = null;
        Long version = null;
        List<OptionResponseDto> options = null;
        List<PollResponse> polls = null;
        Instant createdAt = null;

        categoryName = decisionBoardCategoryName( decisionBoard );
        communityName = decisionBoardCommunityName( decisionBoard );
        tags = mapTags( decisionBoard.getTags() );
        factors = comparisonFactorListToComparisonFactorResponseList( decisionBoard.getComparisonFactors() );
        id = decisionBoard.getId();
        title = decisionBoard.getTitle();
        description = decisionBoard.getDescription();
        creator = userMapper.toSummary( decisionBoard.getCreator() );
        votingType = decisionBoard.getVotingType();
        anonymityType = decisionBoard.getAnonymityType();
        status = decisionBoard.getStatus();
        deadline = decisionBoard.getDeadline();
        version = decisionBoard.getVersion();
        options = decisionOptionListToOptionResponseDtoList( decisionBoard.getOptions() );
        polls = pollListToPollResponseList( decisionBoard.getPolls() );
        createdAt = decisionBoard.getCreatedAt();

        DecisionResponse decisionResponse = new DecisionResponse( id, title, description, creator, categoryName, communityName, votingType, anonymityType, status, deadline, version, options, polls, factors, tags, createdAt );

        return decisionResponse;
    }

    @Override
    public DecisionSummaryDto toSummary(DecisionBoard decisionBoard) {
        if ( decisionBoard == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        DecisionStatus status = null;
        Instant deadline = null;
        Instant createdAt = null;

        id = decisionBoard.getId();
        title = decisionBoard.getTitle();
        status = decisionBoard.getStatus();
        deadline = decisionBoard.getDeadline();
        createdAt = decisionBoard.getCreatedAt();

        DecisionSummaryDto decisionSummaryDto = new DecisionSummaryDto( id, title, status, deadline, createdAt );

        return decisionSummaryDto;
    }

    @Override
    public DecisionBoard toEntity(DecisionRequest request) {
        if ( request == null ) {
            return null;
        }

        DecisionBoard decisionBoard = new DecisionBoard();

        decisionBoard.setTitle( request.title() );
        decisionBoard.setDescription( request.description() );
        decisionBoard.setVotingType( request.votingType() );
        decisionBoard.setAnonymityType( request.anonymityType() );
        decisionBoard.setDeadline( request.deadline() );

        return decisionBoard;
    }

    @Override
    public void updateEntity(DecisionUpdateRequest request, DecisionBoard decisionBoard) {
        if ( request == null ) {
            return;
        }

        if ( request.title() != null ) {
            decisionBoard.setTitle( request.title() );
        }
        if ( request.description() != null ) {
            decisionBoard.setDescription( request.description() );
        }
        if ( request.deadline() != null ) {
            decisionBoard.setDeadline( request.deadline() );
        }
    }

    @Override
    public DecisionOption toEntity(OptionCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        DecisionOption decisionOption = new DecisionOption();

        decisionOption.setTitle( dto.title() );
        decisionOption.setDescription( dto.description() );

        return decisionOption;
    }

    @Override
    public OptionResponseDto toResponseDto(DecisionOption option) {
        if ( option == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        String description = null;
        List<CriteriaDto> criteria = null;
        List<ComparisonScoreResponse> comparisonScores = null;

        id = option.getId();
        title = option.getTitle();
        description = option.getDescription();
        criteria = optionCriteriaListToCriteriaDtoList( option.getCriteria() );
        comparisonScores = comparisonScoreListToComparisonScoreResponseList( option.getComparisonScores() );

        OptionResponseDto optionResponseDto = new OptionResponseDto( id, title, description, criteria, comparisonScores );

        return optionResponseDto;
    }

    @Override
    public OptionCriteria toEntity(CriteriaDto dto) {
        if ( dto == null ) {
            return null;
        }

        OptionCriteria optionCriteria = new OptionCriteria();

        optionCriteria.setCriterionName( dto.criterionName() );
        optionCriteria.setScore( dto.score() );
        optionCriteria.setRemarks( dto.remarks() );

        return optionCriteria;
    }

    @Override
    public CriteriaDto toResponseDto(OptionCriteria criteria) {
        if ( criteria == null ) {
            return null;
        }

        String criterionName = null;
        int score = 0;
        String remarks = null;

        criterionName = criteria.getCriterionName();
        score = criteria.getScore();
        remarks = criteria.getRemarks();

        CriteriaDto criteriaDto = new CriteriaDto( criterionName, score, remarks );

        return criteriaDto;
    }

    private String decisionBoardCategoryName(DecisionBoard decisionBoard) {
        Category category = decisionBoard.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getName();
    }

    private String decisionBoardCommunityName(DecisionBoard decisionBoard) {
        Community community = decisionBoard.getCommunity();
        if ( community == null ) {
            return null;
        }
        return community.getName();
    }

    protected List<ComparisonFactorResponse> comparisonFactorListToComparisonFactorResponseList(List<ComparisonFactor> list) {
        if ( list == null ) {
            return null;
        }

        List<ComparisonFactorResponse> list1 = new ArrayList<ComparisonFactorResponse>( list.size() );
        for ( ComparisonFactor comparisonFactor : list ) {
            list1.add( comparisonMapper.toResponse( comparisonFactor ) );
        }

        return list1;
    }

    protected List<OptionResponseDto> decisionOptionListToOptionResponseDtoList(List<DecisionOption> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionResponseDto> list1 = new ArrayList<OptionResponseDto>( list.size() );
        for ( DecisionOption decisionOption : list ) {
            list1.add( toResponseDto( decisionOption ) );
        }

        return list1;
    }

    protected List<PollResponse> pollListToPollResponseList(List<Poll> list) {
        if ( list == null ) {
            return null;
        }

        List<PollResponse> list1 = new ArrayList<PollResponse>( list.size() );
        for ( Poll poll : list ) {
            list1.add( pollMapper.toResponse( poll ) );
        }

        return list1;
    }

    protected List<CriteriaDto> optionCriteriaListToCriteriaDtoList(List<OptionCriteria> list) {
        if ( list == null ) {
            return null;
        }

        List<CriteriaDto> list1 = new ArrayList<CriteriaDto>( list.size() );
        for ( OptionCriteria optionCriteria : list ) {
            list1.add( toResponseDto( optionCriteria ) );
        }

        return list1;
    }

    protected List<ComparisonScoreResponse> comparisonScoreListToComparisonScoreResponseList(List<ComparisonScore> list) {
        if ( list == null ) {
            return null;
        }

        List<ComparisonScoreResponse> list1 = new ArrayList<ComparisonScoreResponse>( list.size() );
        for ( ComparisonScore comparisonScore : list ) {
            list1.add( comparisonMapper.toResponse( comparisonScore ) );
        }

        return list1;
    }
}
