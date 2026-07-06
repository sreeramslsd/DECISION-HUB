package com.decisionhub.service.impl;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.Category;
import com.decisionhub.entity.Community;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionHistory;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.Tag;
import com.decisionhub.entity.User;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.ComparisonMapper;
import com.decisionhub.mapper.DecisionMapper;
import com.decisionhub.repository.CategoryRepository;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionHistoryRepository;
import com.decisionhub.repository.TagRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.AuditService;
import com.decisionhub.service.DecisionService;
import com.decisionhub.validator.DecisionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing the DecisionBoard aggregate lifecycle, transactions, and search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionServiceImpl implements DecisionService {

    private final DecisionBoardRepository decisionBoardRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final CategoryRepository categoryRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final DecisionHistoryRepository decisionHistoryRepository;
    
    private final DecisionMapper decisionMapper;
    private final ComparisonMapper comparisonMapper;
    private final DecisionValidator decisionValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public DecisionResponse createDecision(DecisionRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to create decision board: {}", request.title());
        
        UUID currentUserId = getCurrentUserIdOrThrow();
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 1. Authorization
        if (!decisionAuthorizationService.canCreateDecision(request.communityId(), currentUserId)) {
            throw new ForbiddenException("Not authorized to create decisions in this workspace");
        }

        // 2. Business Validation
        decisionValidator.validateCreate(request);

        Community community = null;
        if (request.communityId() != null) {
            community = communityRepository.findById(request.communityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + request.communityId()));
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.categoryId()));
        }

        // 3. Map Core Entity
        DecisionBoard decisionBoard = decisionMapper.toEntity(request);
        decisionBoard.setCreator(creator);
        decisionBoard.setCommunity(community);
        decisionBoard.setCategory(category);
        decisionBoard.setStatus(DecisionStatus.DRAFT);
        decisionBoard.setPublic(request.isPublic());

        // 4. Map Options (at least 2 guaranteed by validator)
        request.options().forEach(optionDto -> {
            DecisionOption option = decisionMapper.toEntity(optionDto);
            decisionBoard.addOption(option);
        });

        // 5. Map Comparison Factors (if provided)
        if (request.factors() != null) {
            request.factors().forEach(factorDto -> {
                ComparisonFactor factor = comparisonMapper.toEntity(factorDto);
                decisionBoard.addComparisonFactor(factor);
            });
        }

        // 6. Map Tags (lookup existing or create)
        if (request.tags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByName(tagName.trim().toLowerCase())
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName.trim().toLowerCase()).build()));
                tags.add(tag);
            }
            decisionBoard.setTags(tags);
        }

        // 7. Save Aggregate Root (cascades to options and factors)
        DecisionBoard savedBoard = decisionBoardRepository.save(decisionBoard);

        // 8. Log History
        saveHistory(savedBoard, creator, "CREATE", "{\"status\":\"DRAFT\"}");

        // 9. Audit Logging
        auditService.log(creator, "DECISION_CREATED", "decision_boards", savedBoard.getId(), null, null, ipAddress, userAgent);

        log.info("Decision board '{}' created successfully with ID '{}'", savedBoard.getTitle(), savedBoard.getId());
        return mapToResponseWithCollections(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(UUID id) {
        log.debug("Fetching decision by ID: {}", id);
        
        UUID currentUserId = getCurrentUserId().orElse(null);

        // Authorization
        if (!decisionAuthorizationService.canViewDecision(id, currentUserId)) {
            throw new ForbiddenException("Not authorized to view this decision board");
        }

        DecisionBoard board = getActiveBoardOrThrow(id);
        return mapToResponseWithCollections(board);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DecisionResponse> getDecisions(Pageable pageable) {
        log.debug("Fetching visible decisions list");
        
        UUID currentUserId = getCurrentUserId().orElse(null);
        Page<DecisionBoard> page;
        
        if (currentUserId == null) {
            page = decisionBoardRepository.findPublicDecisions(pageable);
        } else {
            page = decisionBoardRepository.findVisibleDecisions(currentUserId, pageable);
        }

        page.forEach(this::initializeLazyCollections);
        Page<DecisionResponse> mappedPage = page.map(decisionMapper::toResponse);
        return PageResponse.from(mappedPage);
    }

    @Override
    @Transactional
    public DecisionResponse updateDecision(UUID id, DecisionUpdateRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to update decision board: {}", id);

        UUID currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 1. Authorization
        if (!decisionAuthorizationService.canEditDecision(id, currentUserId)) {
            throw new ForbiddenException("Not authorized to edit this decision board");
        }

        DecisionBoard board = getActiveBoardOrThrow(id);

        // 2. Validation
        decisionValidator.validateUpdate(board, request);

        // Log old values for history serialization
        String oldValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", board.getTitle(), board.getDescription());

        // 3. Map changes
        decisionMapper.updateEntity(request, board);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.categoryId()));
            board.setCategory(category);
        }

        if (request.tags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByName(tagName.trim().toLowerCase())
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName.trim().toLowerCase()).build()));
                tags.add(tag);
            }
            board.setTags(tags);
        }

        // 4. Save
        DecisionBoard savedBoard = decisionBoardRepository.save(board);

        // 5. Log History
        String newValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", savedBoard.getTitle(), savedBoard.getDescription());
        saveHistory(savedBoard, currentUser, "UPDATE", String.format("{\"old\":%s,\"new\":%s}", oldValueJson, newValueJson));

        // 6. Audit Logging
        auditService.log(currentUser, "DECISION_UPDATED", "decision_boards", id, oldValueJson, newValueJson, ipAddress, userAgent);

        log.info("Decision board '{}' updated successfully", savedBoard.getTitle());
        return mapToResponseWithCollections(savedBoard);
    }

    @Override
    @Transactional
    public void deleteDecision(UUID id, String ipAddress, String userAgent) {
        log.info("Attempting to soft delete decision board: {}", id);

        UUID currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // Authorization
        if (!decisionAuthorizationService.canDeleteDecision(id, currentUserId)) {
            throw new ForbiddenException("Not authorized to delete this decision board");
        }

        DecisionBoard board = getActiveBoardOrThrow(id);

        // Soft delete (handled natively by Hibernate SQLDelete via repository.delete())
        board.setDeletedAt(Instant.now());
        decisionBoardRepository.saveAndFlush(board);
        decisionBoardRepository.delete(board);

        // Audit Logging
        auditService.log(currentUser, "DECISION_DELETED", "decision_boards", id, null, null, ipAddress, userAgent);

        log.info("Decision board with ID '{}' soft deleted successfully", id);
    }

    @Override
    @Transactional
    public DecisionResponse transitionStatus(UUID id, DecisionStatus status, String ipAddress, String userAgent) {
        log.info("Attempting to transition decision board '{}' status to: {}", id, status);

        UUID currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // Authorization
        if (status == DecisionStatus.ACTIVE && !decisionAuthorizationService.canActivateDecision(id, currentUserId)) {
            throw new ForbiddenException("Not authorized to activate this decision board");
        }
        if (status == DecisionStatus.CLOSED && !decisionAuthorizationService.canCloseDecision(id, currentUserId)) {
            throw new ForbiddenException("Not authorized to close this decision board");
        }

        DecisionBoard board = getActiveBoardOrThrow(id);

        // Validation
        decisionValidator.validateStatusTransition(board, status);

        DecisionStatus oldStatus = board.getStatus();
        board.setStatus(status);
        DecisionBoard savedBoard = decisionBoardRepository.save(board);

        // History
        saveHistory(savedBoard, currentUser, "STATUS_CHANGE", String.format("{\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}", oldStatus, status));

        // Audit Logging
        String action = status == DecisionStatus.ACTIVE ? "DECISION_ACTIVATED" : "DECISION_CLOSED";
        String oldVal = String.format("{\"status\":\"%s\"}", oldStatus.name());
        String newVal = String.format("{\"status\":\"%s\"}", status.name());
        auditService.log(currentUser, action, "decision_boards", id, oldVal, newVal, ipAddress, userAgent);

        log.info("Decision board '{}' transitioned to status '{}'", id, status);
        return mapToResponseWithCollections(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DecisionResponse> searchDecisions(String query, Pageable pageable) {
        log.debug("Searching decisions with query: '{}'", query);

        UUID currentUserId = getCurrentUserId().orElse(null);
        Page<DecisionBoard> page;

        if (currentUserId == null) {
            page = decisionBoardRepository.searchPublicFullText(query, pageable);
        } else {
            page = decisionBoardRepository.searchVisibleFullText(query, currentUserId, pageable);
        }

        page.forEach(this::initializeLazyCollections);
        Page<DecisionResponse> mappedPage = page.map(decisionMapper::toResponse);
        return PageResponse.from(mappedPage);
    }

    private void saveHistory(DecisionBoard board, User editor, String action, String changesJson) {
        DecisionHistory history = DecisionHistory.builder()
                .decision(board)
                .editor(editor)
                .actionType(action)
                .changesJson(changesJson)
                .build();
        decisionHistoryRepository.save(history);
    }

    private void initializeLazyCollections(DecisionBoard board) {
        Hibernate.initialize(board.getOptions());
        Hibernate.initialize(board.getComparisonFactors());
        Hibernate.initialize(board.getTags());
    }

    private DecisionResponse mapToResponseWithCollections(DecisionBoard board) {
        initializeLazyCollections(board);
        return decisionMapper.toResponse(board);
    }

    private DecisionBoard getActiveBoardOrThrow(UUID id) {
        DecisionBoard board = decisionBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found with ID: " + id));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("Decision board not found with ID: " + id);
        }
        return board;
    }

    private Optional<UUID> getCurrentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    private UUID getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }
}
