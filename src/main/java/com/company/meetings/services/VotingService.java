package com.company.meetings.services;

import com.company.meetings.entities.*;
import com.company.meetings.repositories.MeetingRepository;
import com.company.meetings.repositories.VoteRepository;
import com.company.meetings.repositories.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VotingService {
    
    private final VotingSessionRepository votingSessionRepository;
    private final VoteRepository voteRepository;
    private final MeetingRepository meetingRepository;
    
    @Transactional
    public VotingSession createSession(Long meetingId, String title, List<String> optionTexts, User createdBy) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        
        VotingSession session = new VotingSession();
        session.setMeeting(meeting);
        session.setTitle(title);
        session.setStatus(VotingSession.Status.OPEN);
        session.setCreatedBy(createdBy);
        
        session = votingSessionRepository.save(session);
        
        // Create options
        for (int i = 0; i < optionTexts.size(); i++) {
            VoteOption option = new VoteOption();
            option.setSession(session);
            option.setText(optionTexts.get(i));
            option.setPosition(i);
            session.getOptions().add(option);
        }
        
        return votingSessionRepository.save(session);
    }
    
    @Transactional
    public void closeSession(Long sessionId) {
        VotingSession session = votingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Voting session not found"));
        session.setStatus(VotingSession.Status.CLOSED);
        votingSessionRepository.save(session);
    }
    
    @Transactional
    public void submitVote(Long sessionId, Long optionId, User user, String anonymousToken) {
        VotingSession session = votingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Voting session not found"));
        
        if (session.getStatus() != VotingSession.Status.OPEN) {
            throw new IllegalStateException("Voting session is closed");
        }
        
        VoteOption option = session.getOptions().stream()
            .filter(o -> o.getId().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid option"));
        
        // Check for duplicate votes
        if (user != null && voteRepository.existsBySessionIdAndUserId(sessionId, user.getId())) {
            throw new IllegalStateException("You have already voted in this session");
        }
        
        if (anonymousToken != null && voteRepository.existsBySessionIdAndAnonymousToken(sessionId, anonymousToken)) {
            throw new IllegalStateException("You have already voted in this session");
        }
        
        Vote vote = new Vote();
        vote.setSession(session);
        vote.setOption(option);
        vote.setUser(user);
        vote.setAnonymousToken(anonymousToken);
        
        voteRepository.save(vote);
    }
    
    public Map<Long, Long> getResults(Long sessionId) {
        List<VoteRepository.VoteCount> counts = voteRepository.countVotesBySession(sessionId);
        return counts.stream()
            .collect(Collectors.toMap(
                VoteRepository.VoteCount::getOptionId,
                VoteRepository.VoteCount::getCount
            ));
    }
    
    public VotingSession getSessionById(Long id) {
        return votingSessionRepository.findById(id).orElse(null);
    }
    
    public List<VotingSession> getSessionsByMeeting(Long meetingId) {
        return votingSessionRepository.findByMeetingId(meetingId);
    }
    
    public boolean hasUserVoted(Long sessionId, Long userId) {
        return voteRepository.existsBySessionIdAndUserId(sessionId, userId);
    }
    
    @Transactional
    public void deleteSession(Long sessionId) {
        votingSessionRepository.deleteById(sessionId);
    }
    
    @Transactional
    public void updateSession(Long sessionId, String title) {
        VotingSession session = votingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Voting session not found"));
        session.setTitle(title);
        votingSessionRepository.save(session);
    }
    
    @Transactional
    public void addOption(Long sessionId, String optionText) {
        VotingSession session = votingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Voting session not found"));
        
        int maxPosition = session.getOptions().stream()
            .mapToInt(VoteOption::getPosition)
            .max()
            .orElse(-1);
        
        VoteOption option = new VoteOption();
        option.setSession(session);
        option.setText(optionText);
        option.setPosition(maxPosition + 1);
        session.getOptions().add(option);
        
        votingSessionRepository.save(session);
    }
    
    @Transactional
    public void deleteOption(Long optionId) {
        VotingSession session = votingSessionRepository.findAll().stream()
            .filter(s -> s.getOptions().stream().anyMatch(o -> o.getId().equals(optionId)))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        
        session.getOptions().removeIf(o -> o.getId().equals(optionId));
        votingSessionRepository.save(session);
    }
}
