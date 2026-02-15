package com.company.meetings.controllers;

import com.company.meetings.services.UserService;
import com.company.meetings.services.VotingService;
import com.company.meetings.entities.User;
import com.company.meetings.entities.VotingSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class VotingController {
    
    private final VotingService votingService;
    private final UserService userService;
    
    @GetMapping("/meetings/{meetingId}/voting/new")
    public String newVotingForm(@PathVariable Long meetingId, Model model) {
        model.addAttribute("meetingId", meetingId);
        return "voting/new";
    }
    
    @PostMapping("/meetings/{meetingId}/voting/new")
    public String createVotingSession(@PathVariable Long meetingId,
                                     @RequestParam String title,
                                     @RequestParam String options,
                                     Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        List<String> optionList = Arrays.stream(options.split("\n"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        
        votingService.createSession(meetingId, title, optionList, user);
        return "redirect:/meetings/" + meetingId;
    }
    
    @GetMapping("/voting/{sessionId}")
    public String votingPage(@PathVariable Long sessionId, 
                            Authentication auth,
                            HttpServletRequest request,
                            Model model) {
        VotingSession session = votingService.getSessionById(sessionId);
        if (session == null) {
            return "redirect:/meetings";
        }
        
        User user = null;
        boolean hasVoted = false;
        
        if (auth != null) {
            user = userService.findByUsername(auth.getName());
            hasVoted = votingService.hasUserVoted(sessionId, user.getId());
        } else {
            // Anonymous voting - check cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("vote_token_" + sessionId)) {
                        hasVoted = true;
                        break;
                    }
                }
            }
        }
        
        model.addAttribute("votingSession", session);
        model.addAttribute("hasVoted", hasVoted);
        
        return "voting/vote";
    }
    
    @PostMapping("/voting/{sessionId}")
    public String submitVote(@PathVariable Long sessionId,
                            @RequestParam Long optionId,
                            Authentication auth,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            User user = null;
            String anonymousToken = null;
            
            if (auth != null) {
                user = userService.findByUsername(auth.getName());
            } else {
                // Generate anonymous token
                anonymousToken = UUID.randomUUID().toString();
                // Set cookie to prevent multiple votes
                Cookie cookie = new Cookie("vote_token_" + sessionId, anonymousToken);
                cookie.setMaxAge(365 * 24 * 60 * 60); // 1 year
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            
            votingService.submitVote(sessionId, optionId, user, anonymousToken);
            return "redirect:/voting/" + sessionId + "/results";
        } catch (IllegalStateException e) {
            return "redirect:/voting/" + sessionId + "?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/voting/{sessionId}/results")
    public String viewResults(@PathVariable Long sessionId, Model model) {
        VotingSession session = votingService.getSessionById(sessionId);
        if (session == null) {
            return "redirect:/meetings";
        }
        
        Map<Long, Long> results = votingService.getResults(sessionId);
        long totalVotes = results.values().stream().mapToLong(Long::longValue).sum();
        
        model.addAttribute("votingSession", session);
        model.addAttribute("results", results);
        model.addAttribute("totalVotes", totalVotes);
        
        return "voting/results";
    }
    
    @PostMapping("/voting/{sessionId}/close")
    public String closeSession(@PathVariable Long sessionId) {
        VotingSession session = votingService.getSessionById(sessionId);
        votingService.closeSession(sessionId);
        return "redirect:/meetings/" + session.getMeeting().getId();
    }
    
    @PostMapping("/voting/{sessionId}/delete")
    public String deleteSession(@PathVariable Long sessionId) {
        VotingSession session = votingService.getSessionById(sessionId);
        Long meetingId = session.getMeeting().getId();
        votingService.deleteSession(sessionId);
        return "redirect:/meetings/" + meetingId;
    }
    
    @GetMapping("/voting/{sessionId}/edit")
    public String editSessionForm(@PathVariable Long sessionId, Model model) {
        VotingSession session = votingService.getSessionById(sessionId);
        if (session == null) {
            return "redirect:/meetings";
        }
        model.addAttribute("votingSession", session);
        return "voting/edit";
    }
    
    @PostMapping("/voting/{sessionId}/edit")
    public String updateSession(@PathVariable Long sessionId,
                                @RequestParam String title) {
        votingService.updateSession(sessionId, title);
        VotingSession session = votingService.getSessionById(sessionId);
        return "redirect:/meetings/" + session.getMeeting().getId();
    }
    
    @PostMapping("/voting/{sessionId}/add-option")
    public String addOption(@PathVariable Long sessionId,
                           @RequestParam String optionText) {
        votingService.addOption(sessionId, optionText);
        return "redirect:/voting/" + sessionId + "/edit";
    }
    
    @PostMapping("/voting/option/{optionId}/delete")
    public String deleteOption(@PathVariable Long optionId,
                              @RequestParam Long sessionId) {
        votingService.deleteOption(optionId);
        return "redirect:/voting/" + sessionId + "/edit";
    }
}
