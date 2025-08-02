package com.odoo.quick_desk_backend.repository;

import com.odoo.quick_desk_backend.entity.Ticket;
import com.odoo.quick_desk_backend.entity.TicketVote;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.utility.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketVoteRepository extends JpaRepository<TicketVote, Long> {

    Optional<TicketVote> findByTicketAndUser(Ticket ticket, User user);

    long countByTicketAndVoteType(Ticket ticket, VoteType voteType);

    void deleteByTicketAndUser(Ticket ticket, User user);
}
