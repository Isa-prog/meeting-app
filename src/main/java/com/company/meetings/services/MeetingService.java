package com.company.meetings.services;

import com.company.meetings.entities.Meeting;
import com.company.meetings.entities.User;
import com.company.meetings.repositories.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    
    @Transactional
    public Meeting createMeeting(String title, String description, LocalDateTime startAt, 
                                 LocalDateTime endAt, String location, 
                                 Meeting.Visibility visibility, User createdBy) {
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setStartAt(startAt);
        meeting.setEndAt(endAt);
        meeting.setLocation(location);
        meeting.setVisibility(visibility);
        meeting.setCreatedBy(createdBy);
        meeting.setStatus(Meeting.Status.PLANNED);
        
        return meetingRepository.save(meeting);
    }
    
    @Transactional
    public Meeting updateMeeting(Long id, String title, String description, 
                                 LocalDateTime startAt, LocalDateTime endAt, String location) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setStartAt(startAt);
        meeting.setEndAt(endAt);
        meeting.setLocation(location);
        
        return meetingRepository.save(meeting);
    }
    
    @Transactional
    public void closeMeeting(Long id) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        meeting.setStatus(Meeting.Status.CLOSED);
        meetingRepository.save(meeting);
    }
    
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }
    
    public List<Meeting> getPublicMeetings() {
        return meetingRepository.findByVisibility(Meeting.Visibility.PUBLIC);
    }
    
    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id).orElse(null);
    }
    
    public List<Meeting> getMeetingsByUser(Long userId) {
        return meetingRepository.findByCreatedById(userId);
    }
    
    @Transactional
    public void deleteMeeting(Long id) {
        meetingRepository.deleteById(id);
    }
}
