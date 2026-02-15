package com.company.meetings.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "user_id"}),
    @UniqueConstraint(columnNames = {"session_id", "anonymous_token"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private VotingSession session;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private VoteOption option;
    
    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt = LocalDateTime.now();
    
    @Column(name = "anonymous_token")
    private String anonymousToken;
    
    @PrePersist
    protected void onCreate() {
        if (votedAt == null) {
            votedAt = LocalDateTime.now();
        }
    }
}
