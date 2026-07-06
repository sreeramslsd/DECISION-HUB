package com.decisionhub.dto;

import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.VotingType;

import java.util.UUID;

public record DecisionSearchDto(
    String searchTerm,
    DecisionStatus status,
    VotingType votingType,
    UUID categoryId,
    UUID communityId
) {}
