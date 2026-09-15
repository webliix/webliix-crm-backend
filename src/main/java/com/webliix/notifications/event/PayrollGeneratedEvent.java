package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PayrollGeneratedEvent extends ApplicationEvent {

    private final Long payrollId;
    private final Long employeeId;
    private final String employeeEmail;
    private final String amount;

    public PayrollGeneratedEvent(Object source, Long payrollId, Long employeeId, String employeeEmail, String amount) {
        super(source);
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.employeeEmail = employeeEmail;
        this.amount = amount;
    }
}
