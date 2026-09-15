package com.webliix.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webliix.tickets.entity.TicketComment;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicketId(Long ticketId);

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
