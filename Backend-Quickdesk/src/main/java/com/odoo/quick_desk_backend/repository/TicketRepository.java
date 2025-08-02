package com.odoo.quick_desk_backend.repository;

import com.odoo.quick_desk_backend.entity.Ticket;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.utility.TicketCategory;
import com.odoo.quick_desk_backend.utility.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User createdBy);
    Page<Ticket> findByCreatedBy(User createdBy, Pageable pageable);

    List<Ticket> findByStatus(TicketStatus status);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    List<Ticket> findByCategory(TicketCategory category);
    Page<Ticket> findByCategory(TicketCategory category, Pageable pageable);

    Page<Ticket> findByCreatedByAndStatus(User createdBy, TicketStatus status, Pageable pageable);
    Page<Ticket> findByCreatedByAndCategory(User createdBy, TicketCategory category, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.createdBy = :user AND " +
            "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> searchUserTickets(@Param("user") User user,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);

    Page<Ticket> findByAssignedTo(User assignedTo, Pageable pageable);
    Page<Ticket> findByAssignedToIsNull(Pageable pageable);

    // FIXED: Removed invalid CAST operations
    @Query("SELECT t FROM Ticket t WHERE " +
            "(:createdBy IS NULL OR t.createdBy = :createdBy) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:category IS NULL OR t.category = :category)")
    Page<Ticket> findTicketsWithFilters(@Param("createdBy") User createdBy,
                                        @Param("status") TicketStatus status,
                                        @Param("category") TicketCategory category,
                                        Pageable pageable);
}
