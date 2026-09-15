package com.webliix.automation.trigger;

import com.webliix.automation.enums.TriggerType;
import com.webliix.notifications.event.InvoicePaidEvent;
import com.webliix.notifications.event.LeadCreatedEvent;
import com.webliix.notifications.event.ProjectCreatedEvent;
import com.webliix.notifications.event.TicketAssignedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SyntheticAutomationEvents {

    private final AutomationTriggerPublisher triggerPublisher;

    public SyntheticAutomationEvents(AutomationTriggerPublisher triggerPublisher) {
        this.triggerPublisher = triggerPublisher;
    }

    @EventListener
    public void onLeadCreated(LeadCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", event.getLeadId());
        payload.put("leadName", event.getLeadName());
        payload.put("email", event.getEmail());
        triggerPublisher.publish(TriggerType.LEAD_CREATED, payload);
    }

    @EventListener
    public void onTicketAssigned(TicketAssignedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ticketId", event.getTicketId());
        payload.put("ticketNumber", event.getTicketNumber());
        payload.put("employeeEmail", event.getEmployeeEmail());
        triggerPublisher.publish(TriggerType.TICKET_ASSIGNED, payload);
    }

    @EventListener
    public void onInvoicePaid(InvoicePaidEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("invoiceId", event.getInvoiceId());
        payload.put("invoiceNumber", event.getInvoiceNumber());
        payload.put("amount", event.getAmount());
        payload.put("customerEmail", event.getCustomerEmail());
        triggerPublisher.publish(TriggerType.INVOICE_PAID, payload);
    }

    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", event.getProjectId());
        payload.put("projectName", event.getProjectName());
        payload.put("customerEmail", event.getCustomerEmail());
        triggerPublisher.publish(TriggerType.PROJECT_CREATED, payload);
    }
}
