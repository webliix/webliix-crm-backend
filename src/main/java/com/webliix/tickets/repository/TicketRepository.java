package com.webliix.tickets.repository;

import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findTopByOrderByIdDesc();

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    long countByStatus(TicketStatus status);

    long countByPriority(TicketPriority priority);

    long countByDueDateBeforeAndStatusNot(LocalDate date, TicketStatus status);
}
