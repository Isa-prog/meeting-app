package com.company.meetings.repositories;

import com.company.meetings.entities.Meeting;
import com.company.meetings.entities.Meeting.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByVisibility(Visibility visibility);
    List<Meeting> findByCreatedById(Long userId);
    List<Meeting> findByVisibilityOrCreatedById(Visibility visibility, Long userId);
}
