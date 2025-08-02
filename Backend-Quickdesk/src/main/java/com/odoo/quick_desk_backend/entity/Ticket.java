package com.odoo.quick_desk_backend.entity;


import com.odoo.quick_desk_backend.utility.TicketCategory;
import com.odoo.quick_desk_backend.utility.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "upvotes", nullable = false)
    private Integer upvotes = 0;

    @Column(name = "downvotes", nullable = false)
    private Integer downvotes = 0;

    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketReply> replies;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketVote> votes;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Instant updateTime;

    public Ticket(String subject, String description, TicketCategory category, User createdBy) {
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.createdBy = createdBy;
    }

}
