package com.company.meetings.repositories;

import com.company.meetings.entities.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {
    List<VotingSession> findByMeetingId(Long meetingId);
}
