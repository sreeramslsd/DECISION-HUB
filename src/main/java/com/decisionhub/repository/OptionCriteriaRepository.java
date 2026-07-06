package com.decisionhub.repository;

import com.decisionhub.entity.OptionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OptionCriteriaRepository extends JpaRepository<OptionCriteria, UUID> {
    List<OptionCriteria> findByOptionId(UUID optionId);
}
