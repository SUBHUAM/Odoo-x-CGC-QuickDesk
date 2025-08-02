package com.odoo.quick_desk_backend.controller;

import com.odoo.quick_desk_backend.dto.request.CreateTicketDto;
import com.odoo.quick_desk_backend.dto.request.UpdateTicketStatusDto;
import com.odoo.quick_desk_backend.dto.request.VoteDto;
import com.odoo.quick_desk_backend.dto.response.TicketReplyDto;
import com.odoo.quick_desk_backend.dto.response.TicketResponseDto;
import com.odoo.quick_desk_backend.service.TicketService;
import com.odoo.quick_desk_backend.utility.SessionValidator;
import com.odoo.quick_desk_backend.utility.TicketCategory;
import com.odoo.quick_desk_backend.utility.TicketStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final SessionValidator sessionValidator;

    @Autowired
    public TicketController(TicketService ticketService, SessionValidator sessionValidator) {
        this.ticketService = ticketService;
        this.sessionValidator = sessionValidator;
    }

    // Create ticket
    @PostMapping("/save")
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateTicketDto createTicketDto,
                                          HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
                        Long userId = sessionValidator.getCurrentUserId(request);

//            Long userId = 1L;
            TicketResponseDto ticket = ticketService.createTicket(createTicketDto, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket created successfully");
            response.put("ticket", ticket);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get user's own tickets
    @GetMapping("/my")
    public ResponseEntity<?> getMyTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long userId = sessionValidator.getCurrentUserId(request);
//            Long userId=1L;
            Page<TicketResponseDto> tickets = ticketService.getUserTickets(
                    userId, status, category, search, sortBy, sortDirection, page, size);

            return ResponseEntity.ok(tickets);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all tickets (for support agents)
    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            String userRole = sessionValidator.getCurrentUserRole(request);
            if (!"ADMIN".equals(userRole) && !"MODERATOR".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Support agents only."));
            }

            Page<TicketResponseDto> tickets = ticketService.getAllTickets(
                    status, category, search, sortBy, sortDirection, page, size);

            return ResponseEntity.ok(tickets);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get specific ticket
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable Long ticketId, HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long userId = sessionValidator.getCurrentUserId(request);
//            Long userId=2L;

            TicketResponseDto ticket = ticketService.getTicketById(ticketId, userId);

            return ResponseEntity.ok(ticket);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Update ticket status (support agents only)
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long ticketId,
                                                @RequestBody UpdateTicketStatusDto status,
                                                HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long agentId = sessionValidator.getCurrentUserId(request);

            TicketResponseDto ticket = ticketService.updateTicketStatus(ticketId, status.getStatus(), agentId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket status updated successfully");
            response.put("ticket", ticket);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Assign ticket (support agents only)
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(@PathVariable Long ticketId,
                                          @RequestParam Long agentId,
                                          HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long assignerId = sessionValidator.getCurrentUserId(request);
            TicketResponseDto ticket = ticketService.assignTicket(ticketId, agentId, assignerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket assigned successfully");
            response.put("ticket", ticket);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add reply to ticket
    @PostMapping("/{ticketId}/replies")
    public ResponseEntity<?> addReply(@PathVariable Long ticketId,
                                      @Valid @RequestBody TicketReplyDto replyDto,
                                      HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long userId = sessionValidator.getCurrentUserId(request);
            ticketService.addReply(ticketId, replyDto, userId);

            return ResponseEntity.ok(Map.of("message", "Reply added successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get ticket replies
    @GetMapping("/{ticketId}/replies")
    public ResponseEntity<?> getTicketReplies(@PathVariable Long ticketId,
                                              HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long userId = sessionValidator.getCurrentUserId(request);
            List<TicketReplyDto> replies = ticketService.getTicketReplies(ticketId, userId);

            return ResponseEntity.ok(replies);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Vote on ticket
    @PostMapping("/{ticketId}/vote")
    public ResponseEntity<?> voteOnTicket(@PathVariable Long ticketId,
                                          @Valid @RequestBody VoteDto voteDto,
                                          HttpServletRequest request) {
        if (!sessionValidator.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Long userId = sessionValidator.getCurrentUserId(request);
            ticketService.voteOnTicket(ticketId, voteDto, userId);

            return ResponseEntity.ok(Map.of("message", "Vote recorded successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
