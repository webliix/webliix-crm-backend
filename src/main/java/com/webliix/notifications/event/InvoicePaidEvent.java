package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvoicePaidEvent extends ApplicationEvent {

    private final Long invoiceId;
    private final String invoiceNumber;
    private final String customerEmail;
    private final String amount;

    public InvoicePaidEvent(Object source, Long invoiceId, String invoiceNumber, String customerEmail, String amount) {
        super(source);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerEmail = customerEmail;
        this.amount = amount;
    }
}
