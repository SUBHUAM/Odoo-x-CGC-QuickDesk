package com.odoo.quick_desk_backend.entity;


import com.odoo.quick_desk_backend.utility.VoteType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ticket_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id", "user_id"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    public TicketVote(VoteType voteType, Ticket ticket, User user) {
        this.voteType = voteType;
        this.ticket = ticket;
        this.user = user;
    }
}
