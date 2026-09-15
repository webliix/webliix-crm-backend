package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TicketAssignedEvent extends ApplicationEvent {

    private final Long ticketId;
    private final String ticketNumber;
    private final Long employeeId;
    private final String employeeEmail;

    public TicketAssignedEvent(Object source, Long ticketId, String ticketNumber, Long employeeId, String employeeEmail) {
        super(source);
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.employeeId = employeeId;
        this.employeeEmail = employeeEmail;
    }
}
