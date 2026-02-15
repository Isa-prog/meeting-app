package com.company.meetings.repositories;

import com.company.meetings.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
    boolean existsBySessionIdAndAnonymousToken(Long sessionId, String anonymousToken);
    Optional<Vote> findBySessionIdAndUserId(Long sessionId, Long userId);
    List<Vote> findBySessionId(Long sessionId);
    
    @Query("SELECT v.option.id as optionId, COUNT(v) as count FROM Vote v WHERE v.session.id = :sessionId GROUP BY v.option.id")
    List<VoteCount> countVotesBySession(Long sessionId);
    
    interface VoteCount {
        Long getOptionId();
        Long getCount();
    }
}
