package com.company.meetings.controllers;

import com.company.meetings.services.MeetingService;
import com.company.meetings.services.UserService;
import com.company.meetings.services.VotingService;
import com.company.meetings.entities.Meeting;
import com.company.meetings.entities.User;
import com.company.meetings.entities.VotingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    
    private final MeetingService meetingService;
    private final VotingService votingService;
    private final UserService userService;
    
    @GetMapping
    public String listMeetings(Authentication auth, Model model) {
        List<Meeting> meetings = meetingService.getAllMeetings();
        model.addAttribute("meetings", meetings);
        model.addAttribute("username", auth.getName());
        return "meetings/list";
    }
    
    @GetMapping("/new")
    public String newMeetingForm(Model model) {
        model.addAttribute("visibilities", Meeting.Visibility.values());
        return "meetings/new";
    }
    
    @PostMapping("/new")
    public String createMeeting(@RequestParam String title,
                               @RequestParam String description,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
                               @RequestParam(required = false) String location,
                               @RequestParam Meeting.Visibility visibility,
                               Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        meetingService.createMeeting(title, description, startAt, endAt, location, visibility, user);
        return "redirect:/meetings";
    }
    
    @GetMapping("/{id}")
    public String viewMeeting(@PathVariable Long id, Authentication auth, Model model) {
        Meeting meeting = meetingService.getMeetingById(id);
        if (meeting == null) {
            return "redirect:/meetings";
        }
        
        List<VotingSession> sessions = votingService.getSessionsByMeeting(id);
        
        model.addAttribute("meeting", meeting);
        model.addAttribute("sessions", sessions);
        model.addAttribute("username", auth.getName());
        
        return "meetings/view";
    }
    
    @PostMapping("/{id}/close")
    public String closeMeeting(@PathVariable Long id) {
        meetingService.closeMeeting(id);
        return "redirect:/meetings/" + id;
    }
    
    @GetMapping("/{id}/edit")
    public String editMeetingForm(@PathVariable Long id, Model model) {
        Meeting meeting = meetingService.getMeetingById(id);
        if (meeting == null) {
            return "redirect:/meetings";
        }
        model.addAttribute("meeting", meeting);
        model.addAttribute("visibilities", Meeting.Visibility.values());
        return "meetings/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateMeeting(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
                               @RequestParam(required = false) String location) {
        meetingService.updateMeeting(id, title, description, startAt, endAt, location);
        return "redirect:/meetings/" + id;
    }
    
    @PostMapping("/{id}/delete")
    public String deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return "redirect:/meetings";
    }
}
