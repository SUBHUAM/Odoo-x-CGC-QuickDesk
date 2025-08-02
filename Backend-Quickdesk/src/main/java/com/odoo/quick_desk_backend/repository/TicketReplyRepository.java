package com.odoo.quick_desk_backend.repository;

import com.odoo.quick_desk_backend.entity.Ticket;
import com.odoo.quick_desk_backend.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {

    List<TicketReply> findByTicketOrderByCreateTimeAsc(Ticket ticket);

    long countByTicket(Ticket ticket);
}
