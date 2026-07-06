package com.decisionhub.repository;

import com.decisionhub.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findByPollId(UUID pollId);
    List<Vote> findByOptionId(UUID optionId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.poll.id = :pollId")
    long countByPollId(@Param("pollId") UUID pollId);
}
