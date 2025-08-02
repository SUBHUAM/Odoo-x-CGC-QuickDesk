package com.odoo.quick_desk_backend.service;


import com.odoo.quick_desk_backend.dto.request.CreateTicketDto;
import com.odoo.quick_desk_backend.dto.request.VoteDto;
import com.odoo.quick_desk_backend.dto.response.TicketReplyDto;
import com.odoo.quick_desk_backend.dto.response.TicketResponseDto;
import com.odoo.quick_desk_backend.entity.Ticket;
import com.odoo.quick_desk_backend.entity.TicketReply;
import com.odoo.quick_desk_backend.entity.TicketVote;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.repository.TicketReplyRepository;
import com.odoo.quick_desk_backend.repository.TicketRepository;
import com.odoo.quick_desk_backend.repository.TicketVoteRepository;
import com.odoo.quick_desk_backend.repository.UserRepository;
import com.odoo.quick_desk_backend.utility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketVoteRepository ticketVoteRepository;
    private final UserRepository userRepository;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                         TicketReplyRepository ticketReplyRepository,
                         TicketVoteRepository ticketVoteRepository,
                         UserRepository userRepository,
                         InputSanitizer inputSanitizer) {
        this.ticketRepository = ticketRepository;
        this.ticketReplyRepository = ticketReplyRepository;
        this.ticketVoteRepository = ticketVoteRepository;
        this.userRepository = userRepository;
        this.inputSanitizer = inputSanitizer;
    }

    // Create ticket
    public TicketResponseDto createTicket(CreateTicketDto createTicketDto, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        // Sanitize inputs
        String subject = inputSanitizer.sanitize(createTicketDto.getSubject());
        String description = inputSanitizer.sanitize(createTicketDto.getDescription());
        String attachmentUrl = createTicketDto.getAttachmentUrl() != null ?
                inputSanitizer.sanitize(createTicketDto.getAttachmentUrl()) : null;

        Ticket ticket = new Ticket(subject, description, createTicketDto.getCategory(), user);
        ticket.setAttachmentUrl(attachmentUrl);

        Ticket savedTicket = ticketRepository.save(ticket);
        logger.info("Ticket created: {} by user: {}", savedTicket.getTicketId(), user.getUsername());

        return convertToTicketResponseDto(savedTicket);
    }

    // Get user's own tickets with filters
    public Page<TicketResponseDto> getUserTickets(Long userId,
                                                  TicketStatus status,
                                                  TicketCategory category,
                                                  String searchTerm,
                                                  String sortBy,
                                                  String sortDirection,
                                                  int page,
                                                  int size) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Call simplified query without searchTerm
        Page<Ticket> tickets = ticketRepository.findTicketsWithFilters(
                user, status, category, pageable);

        return tickets.map(this::convertToTicketResponseDto);
    }

    // Get all tickets (for support agents)
    public Page<TicketResponseDto> getAllTickets(TicketStatus status,
                                                 TicketCategory category,
                                                 String searchTerm, // Keep this for future use
                                                 String sortBy,
                                                 String sortDirection,
                                                 int page,
                                                 int size) {
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Call simplified query without searchTerm parameter
        Page<Ticket> tickets = ticketRepository.findTicketsWithFilters(
                null, status, category, pageable);

        return tickets.map(this::convertToTicketResponseDto);
    }


    // Get ticket by ID
    public TicketResponseDto getTicketById(Long ticketId, Long userId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Ticket ticket = ticketOptional.get();

        // Check if user can access this ticket
        if (!canUserAccessTicket(ticket, userId)) {
            throw new RuntimeException("Access denied. You can only view your own tickets.");
        }

        return convertToTicketResponseDto(ticket);
    }

    // Update ticket status (for support agents)
    public TicketResponseDto updateTicketStatus(Long ticketId, TicketStatus newStatus, Long agentId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Optional<User> agentOptional = userRepository.findById(agentId);
        if (agentOptional.isEmpty()) {
            throw new RuntimeException("Agent not found");
        }

        User agent = agentOptional.get();
//        if (!agent.getRole().equals(Role.ADMIN) && !agent.getRole().equals(Role.AGENT)) {
//            throw new RuntimeException("Access denied. Only support agents can update ticket status.");
//        }

        Ticket ticket = ticketOptional.get();
        ticket.setStatus(newStatus);

        // Assign ticket to agent if not already assigned
        if (ticket.getAssignedTo() == null) {
            ticket.setAssignedTo(agent);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        logger.info("Ticket {} status updated to {} by agent: {}",
                ticketId, newStatus, agent.getUsername());

        return convertToTicketResponseDto(updatedTicket);
    }

    // Assign ticket to agent
    public TicketResponseDto assignTicket(Long ticketId, Long agentId, Long assignerId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Optional<User> agentOptional = userRepository.findById(agentId);
        Optional<User> assignerOptional = userRepository.findById(assignerId);

        if (agentOptional.isEmpty() || assignerOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User agent = agentOptional.get();
        User assigner = assignerOptional.get();

        // Check permissions
        if (!assigner.getRole().equals(Role.ADMIN) && !assigner.getRole().equals(Role.AGENT)) {
            throw new RuntimeException("Access denied. Only support agents can assign tickets.");
        }

        Ticket ticket = ticketOptional.get();
        ticket.setAssignedTo(agent);

        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        logger.info("Ticket {} assigned to {} by {}",
                ticketId, agent.getUsername(), assigner.getUsername());

        return convertToTicketResponseDto(updatedTicket);
    }

    // Add reply to ticket
    public void addReply(Long ticketId, TicketReplyDto replyDto, Long userId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Ticket ticket = ticketOptional.get();
        User user = userOptional.get();

        // Check if user can reply to this ticket
        if (!canUserReplyToTicket(ticket, userId)) {
            throw new RuntimeException("Access denied. You can only reply to your own tickets or assigned tickets.");
        }

        String sanitizedMessage = inputSanitizer.sanitize(replyDto.getMessage());
        TicketReply reply = new TicketReply(sanitizedMessage, ticket, user);

        ticketReplyRepository.save(reply);

        // Update reply count
        ticket.setReplyCount(ticket.getReplyCount() + 1);
        ticketRepository.save(ticket);

        logger.info("Reply added to ticket {} by user: {}", ticketId, user.getUsername());
    }

    // Get ticket replies
    public List<TicketReplyDto> getTicketReplies(Long ticketId, Long userId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Ticket ticket = ticketOptional.get();

        // Check if user can access this ticket
        if (!canUserAccessTicket(ticket, userId)) {
            throw new RuntimeException("Access denied. You can only view replies to your own tickets.");
        }

        List<TicketReply> replies = ticketReplyRepository.findByTicketOrderByCreateTimeAsc(ticket);

        return replies.stream()
                .map(reply -> {
                    TicketReplyDto dto = new TicketReplyDto();
                    dto.setMessage(reply.getMessage());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Vote on ticket
    public void voteOnTicket(Long ticketId, VoteDto voteDto, Long userId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Ticket ticket = ticketOptional.get();
        User user = userOptional.get();

        // Check if user already voted
        Optional<TicketVote> existingVote = ticketVoteRepository.findByTicketAndUser(ticket, user);

        if (existingVote.isPresent()) {
            TicketVote vote = existingVote.get();

            // If same vote type, remove vote
            if (vote.getVoteType() == voteDto.getVoteType()) {
                ticketVoteRepository.delete(vote);
                updateVoteCounts(ticket);
                logger.info("Vote removed from ticket {} by user: {}", ticketId, user.getUsername());
                return;
            }

            // If different vote type, update vote
            vote.setVoteType(voteDto.getVoteType());
            ticketVoteRepository.save(vote);
        } else {
            // Create new vote
            TicketVote vote = new TicketVote(voteDto.getVoteType(), ticket, user);
            ticketVoteRepository.save(vote);
        }

        updateVoteCounts(ticket);
        logger.info("Vote {} added to ticket {} by user: {}",
                voteDto.getVoteType(), ticketId, user.getUsername());
    }

    // Helper methods
    private void updateVoteCounts(Ticket ticket) {
        long upvotes = ticketVoteRepository.countByTicketAndVoteType(ticket, VoteType.UPVOTE);
        long downvotes = ticketVoteRepository.countByTicketAndVoteType(ticket, VoteType.DOWNVOTE);

        ticket.setUpvotes((int) upvotes);
        ticket.setDownvotes((int) downvotes);

        ticketRepository.save(ticket);
    }

    private boolean canUserAccessTicket(Ticket ticket, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();

        // Admin/Moderator can access all tickets
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AGENT) {
            return true;
        }

        // Users can only access their own tickets
        return ticket.getCreatedBy().getUserId().equals(userId);
    }

    private boolean canUserReplyToTicket(Ticket ticket, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();

        // Admin/Moderator can reply to any ticket
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AGENT) {
            return true;
        }

        // Users can only reply to their own tickets
        return ticket.getCreatedBy().getUserId().equals(userId);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        switch (sortBy != null ? sortBy.toLowerCase() : "createtime") {
            case "subject":
                return Sort.by(direction, "subject");
            case "status":
                return Sort.by(direction, "status");
            case "category":
                return Sort.by(direction, "category");
            case "upvotes":
                return Sort.by(direction, "upvotes");
            case "replycount":
                return Sort.by(direction, "replyCount");
            case "updatetime":
                return Sort.by(direction, "updateTime");
            case "createtime":
            default:
                return Sort.by(direction, "createTime");
        }
    }

    private TicketResponseDto convertToTicketResponseDto(Ticket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setTicketId(ticket.getTicketId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setCategory(ticket.getCategory());
        dto.setStatus(ticket.getStatus());
        dto.setAttachmentUrl(ticket.getAttachmentUrl());
        dto.setUpvotes(ticket.getUpvotes());
        dto.setDownvotes(ticket.getDownvotes());
        dto.setReplyCount(ticket.getReplyCount());
        dto.setCreatedById(ticket.getCreatedBy().getUserId());
        dto.setCreatedByName(ticket.getCreatedBy().getName());

        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getUserId());
            dto.setAssignedToName(ticket.getAssignedTo().getName());
        }

        dto.setCreateTime(ticket.getCreateTime());
        dto.setUpdateTime(ticket.getUpdateTime());

        return dto;
    }
}
