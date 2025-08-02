package com.odoo.quick_desk_backend.dto.response;


import com.odoo.quick_desk_backend.utility.TicketCategory;
import com.odoo.quick_desk_backend.utility.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDto {

    private Long ticketId;
    private String subject;
    private String description;
    private TicketCategory category;
    private TicketStatus status;
    private String attachmentUrl;
    private Integer upvotes;
    private Integer downvotes;
    private Integer replyCount;
    private Long createdById;
    private String createdByName;
    private Long assignedToId;
    private String assignedToName;
    private Instant createTime;
    private Instant updateTime;
}
